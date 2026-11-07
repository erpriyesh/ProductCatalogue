import { pino } from 'pino';
import { ConfigError, loadConfig } from './config/config.js';
import { createContainer } from './container.js';
import { buildApp } from './http/app.js';

/**
 * Entry point: load and validate config, build the object graph, start HTTP, shut down cleanly.
 * Everything else is in container.ts (wiring) and http/app.ts (routes).
 */
async function main(): Promise<void> {
  const config = loadConfig();
  const log = pino({
    level: config.server.logLevel,
    redact: ['req.headers.authorization', 'req.headers.cookie'],
    ...(config.env === 'development' ? { transport: { target: 'pino-pretty' } } : {}),
  });

  const app = await buildApp({
    ...createContainer(config, log),
    enquiryRateLimitPerMinute: config.enquiry.rateLimitPerMinute,
    trustProxy: config.server.trustProxy,
    logger: log,
  });

  const shutdown = async (signal: string) => {
    log.info({ signal }, 'Shutting down');
    await app.close();
    process.exit(0);
  };
  process.once('SIGTERM', () => void shutdown('SIGTERM'));
  process.once('SIGINT', () => void shutdown('SIGINT'));

  await app.listen({ host: config.server.host, port: config.server.port });
  log.info(
    {
      productSource: config.catalogue.source,
      emailTransport: config.email.transport,
      salesInbox: config.email.salesInbox,
      featured: config.catalogue.featuredProductIds,
    },
    'Catalogue API ready',
  );
}

main().catch((error: unknown) => {
  console.error(error instanceof ConfigError ? error.message : error);
  process.exit(1);
});
