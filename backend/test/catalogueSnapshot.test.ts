import { describe, expect, it } from 'vitest';
import { CatalogueSnapshot } from '../src/catalogue/catalogueSnapshot.js';
import type { Product } from '../src/catalogue/product.js';

function product(id: number, title: string, slug: string, name: string): Product {
  return {
    id,
    title,
    description: '',
    price: { amount: 1, currency: 'AUD' },
    category: { slug, name },
    imageUrl: 'https://example.test/x.png',
    rating: { average: 4, count: 1 },
  };
}

describe('CatalogueSnapshot', () => {
  const snapshot = new CatalogueSnapshot([
    product(1, 'Mens Cotton Jacket', 'mens-clothing', "Men's clothing"),
    product(2, 'Rain Jacket', 'womens-clothing', "Women's clothing"),
    product(3, 'Gaming Drive', 'electronics', 'Electronics'),
  ]);

  it('indexes products by id and category, and lists categories by name', () => {
    expect(snapshot.findById(2)?.title).toBe('Rain Jacket');
    expect(snapshot.findById(99)).toBeUndefined();
    expect(snapshot.findByIds([3, 99, 1]).map((p) => p.id)).toEqual([3, 1]);
    expect(snapshot.findByCategory('electronics').map((p) => p.id)).toEqual([3]);
    expect(snapshot.findByCategory('garden')).toEqual([]);
    expect(snapshot.categories.map((c) => c.slug)).toEqual([
      'electronics',
      'mens-clothing',
      'womens-clothing',
    ]);
  });

  it('searches the prepared index across title and category name', () => {
    expect(snapshot.search('jacket').map((p) => p.id)).toEqual([1, 2]);
    expect(snapshot.search('mens').map((p) => p.id)).toEqual([1]);
    expect(snapshot.search('womens jack').map((p) => p.id)).toEqual([2]);
    expect(snapshot.search('electronics').map((p) => p.id)).toEqual([3]);
    expect(snapshot.search('!!')).toEqual([]);
  });
});
