import { describe, expect, it, vi } from 'vitest';
import { CatalogueService } from '../src/catalogue/catalogueService.js';
import { NotFoundError } from '../src/shared/errors.js';
import { fakeFetch, fakeStoreProducts, jsonResponse, repository } from './support/fixtures.js';

function service(featuredProductIds: number[] = [5, 2, 6], onMissing = vi.fn()) {
  return new CatalogueService(
    repository(),
    { featuredProductIds, categoryTopRatedCount: 2, ratingPriorWeight: 25 },
    onMissing,
  );
}

describe('CatalogueService', () => {
  describe('featured', () => {
    it('with no chosen ids, shows the top 5 by weighted rating (stars and review count)', async () => {
      const featured = await service([]).featured();
      // 4.8 from 400 leads; 4.7 from 500 beats 4.7 from 259; 2.1 from 430 is left out.
      expect(featured.map((p) => p.id)).toEqual([5, 2, 4, 6, 1]);
    });

    it('with no chosen ids, a 5.0 from a single review does not take the top spots', async () => {
      const lonelyFiveStar = {
        id: 50,
        title: 'Brand new jacket',
        price: 10,
        description: 'Only one review so far',
        category: "men's clothing",
        image: 'https://fakestoreapi.com/img/50.png',
        rating: { rate: 5, count: 1 },
      };
      const repo = repository(
        fakeFetch(() => jsonResponse([...fakeStoreProducts, lonelyFiveStar])),
      );
      const featured = await new CatalogueService(repo, {
        featuredProductIds: [],
        categoryTopRatedCount: 2,
        ratingPriorWeight: 25,
      }).featured();

      expect(featured.slice(0, 3).map((p) => p.id)).toEqual([5, 2, 4]);
    });

    it('returns the chosen products in the chosen order', async () => {
      const featured = await service([6, 5, 1, 2, 3]).featured();
      expect(featured.map((p) => p.id)).toEqual([6, 5, 1, 2, 3]);
    });

    it('skips and reports chosen ids that no longer exist', async () => {
      const onMissing = vi.fn();
      const featured = await service([5, 404, 2], onMissing).featured();

      expect(featured.map((p) => p.id)).toEqual([5, 2]);
      expect(onMissing).toHaveBeenCalledWith([404]);
    });
  });

  describe('categoryPage', () => {
    it('splits a category into top rated and the rest, best rated first', async () => {
      const page = await service().categoryPage('mens-clothing');

      expect(page.category).toEqual({ slug: 'mens-clothing', name: "Men's clothing" });
      // 2 and 4 are both 4.7; 2 has more reviews so it leads.
      expect(page.topRated.map((p) => p.id)).toEqual([2, 4]);
      expect(page.others.map((p) => p.id)).toEqual([1, 3]);
    });

    it('returns everything as top rated when the category is small', async () => {
      const page = await service().categoryPage('electronics');
      expect(page.topRated.map((p) => p.id)).toEqual([5]);
      expect(page.others).toEqual([]);
    });

    it('keeps a 5.0 product with a single review out of top rated', async () => {
      const lonelyFiveStar = {
        id: 50,
        title: 'Brand new jacket',
        price: 10,
        description: 'Only one review so far',
        category: "men's clothing",
        image: 'https://fakestoreapi.com/img/50.png',
        rating: { rate: 5, count: 1 },
      };
      const repo = repository(
        fakeFetch(() => jsonResponse([...fakeStoreProducts, lonelyFiveStar])),
      );
      const page = await new CatalogueService(repo, {
        featuredProductIds: [],
        categoryTopRatedCount: 2,
        ratingPriorWeight: 25,
      }).categoryPage('mens-clothing');

      expect(page.topRated.map((p) => p.id)).toEqual([2, 4]);
      expect(page.others.map((p) => p.id)).toContain(50);
    });

    it('throws NotFoundError for an unknown category', async () => {
      await expect(service().categoryPage('garden')).rejects.toBeInstanceOf(NotFoundError);
    });
  });

  it('orders search results best rated first', async () => {
    const results = await service().search('mens');
    expect(results.map((p) => p.id)).toEqual([2, 4, 1, 3]);
  });

  it('throws NotFoundError for an unknown product', async () => {
    await expect(service().product(999)).rejects.toBeInstanceOf(NotFoundError);
  });
});
