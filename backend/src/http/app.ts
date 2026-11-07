import helmet from '@fastify/helmet';
import rateLimit from '@fastify/rate-limit';
import Fastify, { type FastifyBaseLogger, type FastifyInstance } from 'fastify';
import type { CatalogueService } from '../catalogue/catalogueService.js';
import type { EnquiryService } from '../enquiry/enquiryService.js';
import { errorHandler, notFoundHandler } from './errorHandler.js';
import { catalogueRoutes } from './routes/catalogueRoutes.js';
import { enquiryRoutes } from './routes/enquiryRoutes.js';

export interface AppDependencies {
  readonly catalogue: CatalogueService;
  readonly enquiries: EnquiryService;
  readonly enquiryRateLimitPerMinute: number;
  /** Set when running behind a load balancer (e.g. AWS ALB) so rate limits see the real client IP. */
  readonly trustProxy?: boolean;
  /** Omitted in tests, which run silent. */
  readonly logger?: FastifyBaseLogger;
}

/**
 * Builds the HTTP app from its dependencies without starting it. `server.ts` wires real
 * adapters; tests pass fakes and drive it with `app.inject()`, no network needed.
 */
export async function buildApp(deps: AppDependencies): Promise<FastifyInstance> {
  const app = Fastify({
    ...(deps.logger ? { loggerInstance: deps.logger } : { logger: false }),
    trustProxy: deps.trustProxy ?? false,
    bodyLimit: 64 * 1024,
    // Request ids show up in every log line for a request; useful when tracing an enquiry.
    requestIdHeader: 'x-request-id',
  });

  // No CORS plugin: the only client is the mobile app, which is not subject to CORS. Leaving
  // it off means browsers on other origins cannot call the API. Add @fastify/cors with an
  // allow-list when a web client arrives.
  await app.register(helmet);
  await app.register(rateLimit, {
    global: false,
    errorResponseBuilder: (_request, context) => ({
      statusCode: 429,
      code: 'RATE_LIMITED',
      message: `Too many requests. Please try again in ${context.after}.`,
    }),
  });

  app.setErrorHandler(errorHandler);
  app.setNotFoundHandler(notFoundHandler);

  app.get('/health', async () => ({ status: 'ok' }));

  await app.register(
    async (api) => {
      await api.register(catalogueRoutes(deps.catalogue));
      await api.register(enquiryRoutes(deps.enquiries, deps.enquiryRateLimitPerMinute));
    },
    { prefix: '/api' },
  );

  return app;
}
