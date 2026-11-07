import type { Category, Product } from './product.js';

/**
 * The boundary to wherever product data lives. Routes and business rules depend on this
 * interface only; `FakeStoreProductRepository` is the first implementation.
 *
 * Query capabilities (search, by-category) sit here rather than in the service so a future
 * source with native search (Algolia, OpenSearch, a database) can do the work itself.
 */
export interface ProductRepository {
  findAll(): Promise<readonly Product[]>;
  findById(id: number): Promise<Product | undefined>;
  findByIds(ids: readonly number[]): Promise<readonly Product[]>;
  findByCategory(slug: string): Promise<readonly Product[]>;
  listCategories(): Promise<readonly Category[]>;
  search(term: string): Promise<readonly Product[]>;
}
