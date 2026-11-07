import { z } from 'zod';
import { UpstreamError } from '../../shared/errors.js';
import { TtlCache } from '../../shared/ttlCache.js';
import { CatalogueSnapshot } from '../catalogueSnapshot.js';
import type { Category, Product } from '../product.js';
import type { ProductRepository } from '../productRepository.js';
import { toDisplayName, toSlug } from '../text.js';

/**
 * Fake Store's product shape. We validate it rather than trust it: if the upstream changes or
 * returns junk we fail with a 502 here, not with a confusing error deep in the app.
 */
const fakeStoreProductSchema = z.object({
  id: z.number().int().positive(),
  title: z.string(),
  price: z.number().nonnegative(),
  description: z.string(),
  category: z.string().min(1),
  image: z.url(),
  rating: z
    .object({ rate: z.number().min(0).max(5), count: z.number().int().nonnegative() })
    .default({ rate: 0, count: 0 }),
});
type FakeStoreProduct = z.infer<typeof fakeStoreProductSchema>;

export interface FakeStoreOptions {
  readonly baseUrl: string;
  readonly timeoutMs: number;
  readonly cacheTtlMs: number;
  readonly currency: string;
  readonly fetch?: typeof fetch;
  readonly onStaleServed?: (error: unknown) => void;
}

/**
 * Fake Store has 20 products and no search, so this adapter loads the whole catalogue, indexes
 * it once into a [CatalogueSnapshot] and answers every query from that, cached for the TTL.
 * That is a property of this adapter only: a larger source would implement the same interface
 * by pushing queries down to the source.
 */
export class FakeStoreProductRepository implements ProductRepository {
  private readonly catalogue: TtlCache<CatalogueSnapshot>;
  private readonly fetchFn: typeof fetch;

  constructor(private readonly options: FakeStoreOptions) {
    this.fetchFn = options.fetch ?? fetch;
    this.catalogue = new TtlCache(
      async () => new CatalogueSnapshot(await this.loadCatalogue()),
      options.cacheTtlMs,
      options.onStaleServed,
    );
  }

  async findAll(): Promise<readonly Product[]> {
    return (await this.catalogue.get()).products;
  }

  async findById(id: number): Promise<Product | undefined> {
    return (await this.catalogue.get()).findById(id);
  }

  async findByIds(ids: readonly number[]): Promise<readonly Product[]> {
    return (await this.catalogue.get()).findByIds(ids);
  }

  async findByCategory(slug: string): Promise<readonly Product[]> {
    return (await this.catalogue.get()).findByCategory(slug);
  }

  async listCategories(): Promise<readonly Category[]> {
    return (await this.catalogue.get()).categories;
  }

  async search(term: string): Promise<readonly Product[]> {
    return (await this.catalogue.get()).search(term);
  }

  private async loadCatalogue(): Promise<readonly Product[]> {
    const url = `${this.options.baseUrl}/products`;
    let response: Response;
    try {
      response = await this.fetchFn(url, {
        headers: { accept: 'application/json' },
        signal: AbortSignal.timeout(this.options.timeoutMs),
      });
    } catch (error) {
      throw new UpstreamError('Product data source is unreachable', { cause: error });
    }
    if (!response.ok) {
      throw new UpstreamError(`Product data source responded with HTTP ${response.status}`);
    }

    let body: unknown;
    try {
      body = await response.json();
    } catch (error) {
      throw new UpstreamError('Product data source returned invalid JSON', { cause: error });
    }

    const parsed = z.array(fakeStoreProductSchema).safeParse(body);
    if (!parsed.success) {
      throw new UpstreamError('Product data source returned an unexpected shape', {
        cause: parsed.error,
      });
    }
    return parsed.data.map((raw) => this.toProduct(raw));
  }

  private toProduct(raw: FakeStoreProduct): Product {
    return {
      id: raw.id,
      title: raw.title.trim(),
      description: raw.description.trim(),
      price: { amount: raw.price, currency: this.options.currency },
      category: { slug: toSlug(raw.category), name: toDisplayName(raw.category) },
      imageUrl: raw.image,
      rating: { average: raw.rating.rate, count: raw.rating.count },
    };
  }
}
