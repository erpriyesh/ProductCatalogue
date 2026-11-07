import type { FastifyInstance } from 'fastify';
import { z } from 'zod';
import type { CatalogueService } from '../../catalogue/catalogueService.js';

const productIdParams = z.object({ id: z.coerce.number().int().positive() });
const categoryParams = z.object({ slug: z.string().regex(/^[a-z0-9-]{1,60}$/) });
const searchQuery = z.object({
  q: z.string().trim().min(2, 'Search for at least 2 characters').max(100).optional(),
});

/** Catalogue data changes rarely; let clients and any CDN reuse responses for a minute. */
const CACHEABLE = 'public, max-age=60';

export function catalogueRoutes(catalogue: CatalogueService) {
  return async (app: FastifyInstance) => {
    app.addHook('onSend', async (request, reply) => {
      if (request.method === 'GET' && reply.statusCode === 200) {
        reply.header('cache-control', CACHEABLE);
      }
    });

    app.get('/products/featured', async () => ({ products: await catalogue.featured() }));

    app.get('/categories', async () => ({ categories: await catalogue.listCategories() }));

    app.get('/categories/:slug/products', async (request) => {
      const { slug } = categoryParams.parse(request.params);
      return catalogue.categoryPage(slug);
    });

    // GET /api/products?q=term searches; without q it lists the whole catalogue.
    app.get('/products', async (request) => {
      const { q } = searchQuery.parse(request.query);
      if (q === undefined) {
        return { query: null, products: await catalogue.all() };
      }
      return { query: q, products: await catalogue.search(q) };
    });

    app.get('/products/:id', async (request) => {
      const { id } = productIdParams.parse(request.params);
      return catalogue.product(id);
    });
  };
}
