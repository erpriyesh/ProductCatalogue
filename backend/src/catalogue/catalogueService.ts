import { NotFoundError } from '../shared/errors.js';
import type { Category, Product } from './product.js';
import type { ProductRepository } from './productRepository.js';
import { rankByWeightedRating } from './ranking.js';

export interface CategoryPage {
  readonly category: Category;
  /** The best-rated products in the category (weighted by review count), best first. */
  readonly topRated: readonly Product[];
  /** Every other product in the category, best-rated first. */
  readonly others: readonly Product[];
}

/** How many products the home page features when none are chosen (the criterion's "5"). */
export const FEATURED_AUTO_COUNT = 5;

export interface CatalogueRules {
  /** The products the business has chosen to feature, in display order. Empty means automatic. */
  readonly featuredProductIds: readonly number[];
  readonly categoryTopRatedCount: number;
  /** How many reviews it takes for a product's own average to count half (see ranking.ts). */
  readonly ratingPriorWeight: number;
}

/**
 * Business rules for browsing: what is featured, how a category page is split, and what
 * search returns. Kept apart from the data source so the rules are tested without HTTP.
 */
export class CatalogueService {
  constructor(
    private readonly products: ProductRepository,
    private readonly rules: CatalogueRules,
    private readonly onMissingFeatured: (missingIds: readonly number[]) => void = () => {},
  ) {}

  /**
   * The home page's featured products.
   *
   * - Chosen (the default): the products the business picked, in their order, because the
   *   criterion says "GIVEN I have chosen 5 products". A chosen id that no longer exists is
   *   skipped and reported rather than failing the home page.
   * - Automatic (no ids configured): the top [FEATURED_AUTO_COUNT] across the whole catalogue
   *   by weighted rating, so stars and number of reviews both count.
   */
  async featured(): Promise<readonly Product[]> {
    if (this.rules.featuredProductIds.length === 0) {
      return this.rank(await this.products.findAll()).slice(0, FEATURED_AUTO_COUNT);
    }
    const found = await this.products.findByIds(this.rules.featuredProductIds);
    if (found.length < this.rules.featuredProductIds.length) {
      const foundIds = new Set(found.map((product) => product.id));
      this.onMissingFeatured(this.rules.featuredProductIds.filter((id) => !foundIds.has(id)));
    }
    return found;
  }

  listCategories(): Promise<readonly Category[]> {
    return this.products.listCategories();
  }

  async categoryPage(slug: string): Promise<CategoryPage> {
    const category = (await this.products.listCategories()).find((c) => c.slug === slug);
    if (!category) {
      throw new NotFoundError(`No category called "${slug}"`);
    }
    const ranked = this.rank(await this.products.findByCategory(slug));
    return {
      category,
      topRated: ranked.slice(0, this.rules.categoryTopRatedCount),
      others: ranked.slice(this.rules.categoryTopRatedCount),
    };
  }

  /** The whole catalogue, for browsing without a search term. */
  async all(): Promise<readonly Product[]> {
    return this.rank(await this.products.findAll());
  }

  /** Best-rated first (weighted by review count), so the most trusted results lead. */
  async search(term: string): Promise<readonly Product[]> {
    return this.rank(await this.products.search(term));
  }

  async product(id: number): Promise<Product> {
    const product = await this.products.findById(id);
    if (!product) {
      throw new NotFoundError(`No product with id ${id}`);
    }
    return product;
  }

  /** A product rated 5.0 by one person must not outrank 4.8 from hundreds: see ranking.ts. */
  private rank(products: readonly Product[]): Product[] {
    return rankByWeightedRating(products, this.rules.ratingPriorWeight);
  }
}
