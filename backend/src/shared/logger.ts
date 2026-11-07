/**
 * The logging the application needs, satisfied by pino (and by Fastify's request logger).
 * Depending on this small shape rather than pino directly keeps services easy to test.
 */
export interface Logger {
  info(details: object, message: string): void;
  warn(details: object, message: string): void;
  error(details: object, message: string): void;
}
