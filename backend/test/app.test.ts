import type { FastifyInstance } from 'fastify';
import { afterEach, describe, expect, it } from 'vitest';
import { fakeFetch, jsonResponse, testApp } from './support/fixtures.js';

/**
 * HTTP-level tests: real routes, services, validation and error handling, driven with
 * `app.inject()`. Only the upstream (fetch) and email provider are replaced.
 */
let app: FastifyInstance;
afterEach(async () => {
  await app?.close();
});

async function start(options?: Parameters<typeof testApp>[0]) {
  const built = await testApp(options);
  app = built.app;
  return built;
}

const validEnquiry = {
  productId: 2,
  name: 'Ada Lovelace',
  email: 'ada@example.test',
  phone: '+61 400 000 000',
  message: 'Is this available in navy?',
};

describe('GET /health', () => {
  it('reports ok', async () => {
    await start();
    const res = await app.inject({ method: 'GET', url: '/health' });
    expect(res.statusCode).toBe(200);
    expect(res.json()).toEqual({ status: 'ok' });
  });
});

describe('Home page: GET /api/products/featured', () => {
  it('returns the configured featured products in order', async () => {
    await start();
    const res = await app.inject({ method: 'GET', url: '/api/products/featured' });

    expect(res.statusCode).toBe(200);
    expect(res.headers['cache-control']).toBe('public, max-age=60');
    expect(res.json().products.map((p: { id: number }) => p.id)).toEqual([5, 2, 6]);
  });
});

describe('Category page', () => {
  it('lists categories with slugs for links', async () => {
    await start();
    const res = await app.inject({ method: 'GET', url: '/api/categories' });
    expect(res.json().categories).toContainEqual({ slug: 'mens-clothing', name: "Men's clothing" });
  });

  it('returns top rated and additional products for a category', async () => {
    await start();
    const res = await app.inject({ method: 'GET', url: '/api/categories/mens-clothing/products' });
    const body = res.json();

    expect(res.statusCode).toBe(200);
    expect(body.category.name).toBe("Men's clothing");
    expect(body.topRated.map((p: { id: number }) => p.id)).toEqual([2, 4]);
    expect(body.others.map((p: { id: number }) => p.id)).toEqual([1, 3]);
  });

  it('returns 404 for an unknown category', async () => {
    await start();
    const res = await app.inject({ method: 'GET', url: '/api/categories/garden/products' });
    expect(res.statusCode).toBe(404);
    expect(res.json().error.code).toBe('NOT_FOUND');
  });

  it('rejects a malformed slug', async () => {
    await start();
    const res = await app.inject({ method: 'GET', url: '/api/categories/DROP%20TABLE/products' });
    expect(res.statusCode).toBe(400);
  });
});

describe('Search: GET /api/products?q=', () => {
  it('returns only products matching the term', async () => {
    await start();
    const res = await app.inject({ method: 'GET', url: '/api/products?q=jacket' });

    expect(res.statusCode).toBe(200);
    expect(res.json()).toMatchObject({ query: 'jacket' });
    expect(res.json().products.map((p: { id: number }) => p.id)).toEqual([2]);
  });

  it('returns an empty list, not an error, when nothing matches', async () => {
    await start();
    const res = await app.inject({ method: 'GET', url: '/api/products?q=submarine' });
    expect(res.statusCode).toBe(200);
    expect(res.json().products).toEqual([]);
  });

  it('trims the term and rejects one that is too short', async () => {
    await start();
    const res = await app.inject({ method: 'GET', url: '/api/products?q=%20a%20' });

    expect(res.statusCode).toBe(400);
    expect(res.json().error).toMatchObject({
      code: 'VALIDATION_FAILED',
      details: [{ field: 'q', message: 'Search for at least 2 characters' }],
    });
  });

  it('lists the whole catalogue without a term', async () => {
    await start();
    const res = await app.inject({ method: 'GET', url: '/api/products' });
    expect(res.json().products).toHaveLength(6);
  });
});

describe('Product page: GET /api/products/:id', () => {
  it('returns the product', async () => {
    await start();
    const res = await app.inject({ method: 'GET', url: '/api/products/2' });
    expect(res.statusCode).toBe(200);
    expect(res.json()).toMatchObject({ id: 2, price: { amount: 55.99, currency: 'AUD' } });
  });

  it('returns 404 for a missing product and 400 for a bad id', async () => {
    await start();
    expect((await app.inject({ method: 'GET', url: '/api/products/999' })).statusCode).toBe(404);
    expect((await app.inject({ method: 'GET', url: '/api/products/abc' })).statusCode).toBe(400);
  });
});

describe('Upstream failure', () => {
  it('returns 502 with the standard error shape and no internal detail', async () => {
    await start({ fetch: fakeFetch(() => jsonResponse({}, 500)) });
    const res = await app.inject({ method: 'GET', url: '/api/products/featured' });

    expect(res.statusCode).toBe(502);
    expect(res.headers['cache-control']).toBeUndefined();
    expect(res.json()).toEqual({
      error: {
        code: 'UPSTREAM_UNAVAILABLE',
        message: 'Product data source responded with HTTP 500',
      },
    });
  });
});

describe('Enquiry: POST /api/enquiries', () => {
  it('emails the sales inbox and returns a reference', async () => {
    const { email } = await start();
    const res = await app.inject({ method: 'POST', url: '/api/enquiries', payload: validEnquiry });

    expect(res.statusCode).toBe(201);
    expect(res.json()).toEqual({ reference: 'ENQ-TEST0001' });
    expect(email.sent).toHaveLength(1);
    expect(email.sent[0]).toMatchObject({
      to: 'sales@example.test',
      replyTo: 'ada@example.test',
      subject: 'New enquiry ENQ-TEST0001: Mens Cotton Jacket',
    });
  });

  it('rejects invalid fields with per-field messages and sends nothing', async () => {
    const { email } = await start();
    const res = await app.inject({
      method: 'POST',
      url: '/api/enquiries',
      payload: { productId: 2, name: '  ', email: 'not-an-email', phone: 'call me' },
    });

    expect(res.statusCode).toBe(400);
    const fields = res.json().error.details.map((d: { field: string }) => d.field);
    expect(fields).toEqual(expect.arrayContaining(['name', 'email', 'phone']));
    expect(email.sent).toHaveLength(0);
  });

  it('rejects unknown fields so clients cannot smuggle in product titles or prices', async () => {
    const { email } = await start();
    const res = await app.inject({
      method: 'POST',
      url: '/api/enquiries',
      payload: { ...validEnquiry, productTitle: 'Free TV', price: 0 },
    });
    expect(res.statusCode).toBe(400);
    expect(email.sent).toHaveLength(0);
  });

  it('returns 404 for a product that does not exist', async () => {
    const { email } = await start();
    const res = await app.inject({
      method: 'POST',
      url: '/api/enquiries',
      payload: { ...validEnquiry, productId: 999 },
    });
    expect(res.statusCode).toBe(404);
    expect(email.sent).toHaveLength(0);
  });

  it('returns 502 when the email provider fails, so the app never shows a false thank you', async () => {
    const { email } = await start();
    email.failWith = new Error('SMTP connection refused');
    const res = await app.inject({ method: 'POST', url: '/api/enquiries', payload: validEnquiry });

    expect(res.statusCode).toBe(502);
    expect(res.json().error).toEqual({
      code: 'EMAIL_DELIVERY_FAILED',
      message: 'We could not send your enquiry. Please try again.',
    });
  });

  it('returns 400 for malformed JSON', async () => {
    await start();
    const res = await app.inject({
      method: 'POST',
      url: '/api/enquiries',
      headers: { 'content-type': 'application/json' },
      payload: '{"name":',
    });
    expect(res.statusCode).toBe(400);
    expect(res.json().error.code).toBeTypeOf('string');
  });

  it('rate limits repeated submissions', async () => {
    await start({ rateLimitPerMinute: 2 });
    const submit = () =>
      app.inject({ method: 'POST', url: '/api/enquiries', payload: validEnquiry });

    expect((await submit()).statusCode).toBe(201);
    expect((await submit()).statusCode).toBe(201);
    const limited = await submit();
    expect(limited.statusCode).toBe(429);
    expect(limited.json().error.code).toBe('RATE_LIMITED');
  });
});

describe('Unknown routes', () => {
  it('return 404 in the standard error shape', async () => {
    await start();
    const res = await app.inject({ method: 'GET', url: '/api/nope' });
    expect(res.statusCode).toBe(404);
    expect(res.json().error.code).toBe('NOT_FOUND');
  });
});
