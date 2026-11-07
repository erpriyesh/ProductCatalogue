import { describe, expect, it } from 'vitest';
import type { Product } from '../src/catalogue/product.js';
import { rankByWeightedRating, weightedRating } from '../src/catalogue/ranking.js';

function product(id: number, average: number, count: number): Product {
  return {
    id,
    title: `Product ${id}`,
    description: '',
    price: { amount: 1, currency: 'AUD' },
    category: { slug: 'electronics', name: 'Electronics' },
    imageUrl: 'https://example.test/x.png',
    rating: { average, count },
  };
}

describe('weightedRating', () => {
  it('pulls a product with few reviews towards the mean', () => {
    expect(weightedRating(5, 1, 3.5, 25)).toBeCloseTo(3.56, 2);
  });

  it('keeps a product with many reviews close to its own average', () => {
    expect(weightedRating(4.8, 400, 3.5, 25)).toBeCloseTo(4.72, 2);
  });

  it('is the plain average when the prior weight is 0', () => {
    expect(weightedRating(5, 1, 3.5, 0)).toBe(5);
  });
});

describe('rankByWeightedRating', () => {
  it('does not let 5.0 from one review outrank 4.8 from hundreds', () => {
    const ranked = rankByWeightedRating(
      [product(1, 5.0, 1), product(2, 4.8, 400), product(3, 3.9, 120), product(4, 2.1, 430)],
      25,
    );
    expect(ranked.map((p) => p.id)).toEqual([2, 3, 1, 4]);
  });

  it('lets a well-reviewed 5.0 lead', () => {
    const ranked = rankByWeightedRating([product(1, 5.0, 300), product(2, 4.8, 400)], 25);
    expect(ranked[0]?.id).toBe(1);
  });

  it('breaks ties by review count, then id, so the order is stable', () => {
    const ranked = rankByWeightedRating(
      [product(2, 4.0, 10), product(1, 4.0, 10), product(3, 4.0, 50)],
      25,
    );
    expect(ranked.map((p) => p.id)).toEqual([3, 1, 2]);
  });

  it('handles an empty list and products with no reviews', () => {
    expect(rankByWeightedRating([], 25)).toEqual([]);
    expect(rankByWeightedRating([product(1, 0, 0)], 25).map((p) => p.id)).toEqual([1]);
  });
});
