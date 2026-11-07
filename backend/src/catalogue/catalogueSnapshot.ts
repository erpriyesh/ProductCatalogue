import type { Category, Product } from './product.js';
import { matchesAllWords, toSearchWords } from './text.js';

/**
 * An indexed, read-only copy of a whole catalogue, built once each time the source is loaded.
 * Every request then reads from prepared structures instead of re-scanning and re-normalising:
 *
 * - product by id: Map lookup, O(1)
 * - products by category and the category list: precomputed
 * - search: each product's words are normalised once here; a query only normalises the term
 *
 * Suits catalogues held in memory (Fake Store has 20 products; this stays fast into the tens of
 * thousands). Beyond that, search moves to a search engine behind ProductRepository.search().
 */
export class CatalogueSnapshot {
  private readonly byId: ReadonlyMap<number, Product>;
  private readonly byCategory: ReadonlyMap<string, readonly Product[]>;
  private readonly searchIndex: readonly { product: Product; words: readonly string[] }[];
  readonly categories: readonly Category[];

  constructor(readonly products: readonly Product[]) {
    this.byId = new Map(products.map((product) => [product.id, product]));

    const byCategory = new Map<string, Product[]>();
    const categories = new Map<string, Category>();
    for (const product of products) {
      const list = byCategory.get(product.category.slug) ?? [];
      list.push(product);
      byCategory.set(product.category.slug, list);
      categories.set(product.category.slug, product.category);
    }
    this.byCategory = byCategory;
    this.categories = [...categories.values()].sort((a, b) => a.name.localeCompare(b.name));

    this.searchIndex = products.map((product) => ({
      product,
      words: toSearchWords(`${product.title} ${product.description} ${product.category.name}`),
    }));
  }

  findById(id: number): Product | undefined {
    return this.byId.get(id);
  }

  findByIds(ids: readonly number[]): Product[] {
    return ids.flatMap((id) => this.byId.get(id) ?? []);
  }

  findByCategory(slug: string): readonly Product[] {
    return this.byCategory.get(slug) ?? [];
  }

  search(term: string): Product[] {
    const termWords = toSearchWords(term);
    return this.searchIndex
      .filter((entry) => matchesAllWords(entry.words, termWords))
      .map((entry) => entry.product);
  }
}
