import { describe, expect, it } from 'vitest';
import { UpstreamError } from '../src/shared/errors.js';
import { fakeFetch, fakeStoreProducts, jsonResponse, repository } from './support/fixtures.js';

describe('FakeStoreProductRepository', () => {
  it('maps Fake Store products to the catalogue model', async () => {
    const product = await repository().findById(2);

    expect(product).toEqual({
      id: 2,
      title: 'Mens Cotton Jacket',
      description: 'Great outerwear for spring',
      price: { amount: 55.99, currency: 'AUD' },
      category: { slug: 'mens-clothing', name: "Men's clothing" },
      imageUrl: 'https://fakestoreapi.com/img/2.png',
      rating: { average: 4.7, count: 500 },
    });
  });

  it('calls the upstream once and answers later queries from cache', async () => {
    const fetchFn = fakeFetch();
    const repo = repository(fetchFn);

    await repo.findAll();
    await repo.findById(1);
    await repo.listCategories();
    await repo.search('jacket');

    expect(fetchFn.calls).toEqual(['https://fake.store/products']);
  });

  it('lists distinct categories sorted by name', async () => {
    expect(await repository().listCategories()).toEqual([
      { slug: 'electronics', name: 'Electronics' },
      { slug: 'jewelery', name: 'Jewelery' },
      { slug: 'mens-clothing', name: "Men's clothing" },
    ]);
  });

  it('finds products by category slug and by ids in the requested order', async () => {
    const repo = repository();
    expect((await repo.findByCategory('mens-clothing')).map((p) => p.id)).toEqual([1, 2, 3, 4]);
    expect((await repo.findByIds([6, 99, 1])).map((p) => p.id)).toEqual([6, 1]);
  });

  it('searches title, description and category', async () => {
    const repo = repository();
    expect((await repo.search('jacket')).map((p) => p.id)).toEqual([2]);
    expect((await repo.search('playstation')).map((p) => p.id)).toEqual([5]);
    expect((await repo.search('electronics')).map((p) => p.id)).toEqual([5]);
    expect(await repo.search('submarine')).toEqual([]);
  });

  it('turns an HTTP error into an UpstreamError', async () => {
    const repo = repository(fakeFetch(() => jsonResponse({ message: 'nope' }, 503)));
    await expect(repo.findAll()).rejects.toBeInstanceOf(UpstreamError);
  });

  it('turns a network failure into an UpstreamError', async () => {
    const repo = repository(
      fakeFetch(() => {
        throw new TypeError('fetch failed');
      }),
    );
    await expect(repo.findAll()).rejects.toThrow('Product data source is unreachable');
  });

  it('rejects a response that does not match the expected shape', async () => {
    const broken = [{ ...fakeStoreProducts[0], price: 'free' }];
    const repo = repository(fakeFetch(() => jsonResponse(broken)));
    await expect(repo.findAll()).rejects.toThrow('unexpected shape');
  });
});
