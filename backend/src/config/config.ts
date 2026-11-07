import { z } from 'zod';

/**
 * All runtime configuration comes from environment variables and is validated once at start-up,
 * so a misconfigured server fails immediately with a clear message instead of on the first request.
 * `.env.example` at the repository root documents every variable.
 */

/** "11, 12,3" -> [11, 12, 3]; an empty string -> [] (used to switch featured products to automatic). */
const csvOfPositiveInts = z
  .string()
  .transform((value) =>
    value
      .split(',')
      .map((part) => part.trim())
      .filter((part) => part.length > 0)
      .map(Number),
  )
  .pipe(z.array(z.number().int().positive()));

const booleanString = z
  .enum(['true', 'false'])
  .default('false')
  .transform((value) => value === 'true');

const emptyToUndefined = (value: unknown) => (value === '' ? undefined : value);

const envSchema = z.object({
  NODE_ENV: z.enum(['development', 'test', 'production']).default('development'),
  HOST: z.string().default('0.0.0.0'),
  PORT: z.coerce.number().int().min(1).max(65535).default(3000),
  LOG_LEVEL: z.enum(['fatal', 'error', 'warn', 'info', 'debug', 'trace']).default('info'),
  // true behind a load balancer (AWS ALB) so rate limiting sees the client IP, not the balancer's.
  TRUST_PROXY: booleanString,

  // Which product data source adapter to use. New sources are added to this list and to
  // src/catalogue/productSourceFactory.ts; nothing else changes.
  PRODUCT_SOURCE: z.enum(['fakestore']).default('fakestore'),
  FAKE_STORE_BASE_URL: z.url().default('https://fakestoreapi.com'),
  FAKE_STORE_TIMEOUT_MS: z.coerce.number().int().positive().default(5000),
  CATALOGUE_CACHE_TTL_SECONDS: z.coerce.number().int().nonnegative().default(300),
  CATALOGUE_CURRENCY: z
    .string()
    .regex(/^[A-Z]{3}$/, 'must be an ISO 4217 code such as AUD')
    .default('AUD'),

  // The products the business has chosen to feature on the home page, in order.
  // Leave it empty (FEATURED_PRODUCT_IDS=) to show the top 5 by weighted rating instead.
  FEATURED_PRODUCT_IDS: csvOfPositiveInts.default([11, 12, 3, 18, 5]),
  CATEGORY_TOP_RATED_COUNT: z.coerce.number().int().min(1).max(20).default(3),
  // Reviews needed before a product's own average counts half; stops "5.0 from 1 review" ranking first.
  RATING_PRIOR_WEIGHT: z.coerce.number().int().min(0).max(10_000).default(25),

  // Which email adapter to use (src/enquiry/email/emailSenderFactory.ts).
  EMAIL_TRANSPORT: z.enum(['smtp', 'log']).default('smtp'),
  SALES_INBOX_EMAIL: z.email().default('sales@example.com'),
  EMAIL_FROM: z.string().min(3).default('Product Catalogue <no-reply@example.com>'),
  SMTP_HOST: z.string().default('localhost'),
  SMTP_PORT: z.coerce.number().int().min(1).max(65535).default(1025),
  SMTP_SECURE: booleanString,
  SMTP_USER: z.preprocess(emptyToUndefined, z.string().optional()),
  SMTP_PASS: z.preprocess(emptyToUndefined, z.string().optional()),

  ENQUIRY_RATE_LIMIT_PER_MINUTE: z.coerce.number().int().positive().default(5),
});

export type Env = z.infer<typeof envSchema>;

export interface AppConfig {
  readonly env: Env['NODE_ENV'];
  readonly server: {
    readonly host: string;
    readonly port: number;
    readonly logLevel: string;
    readonly trustProxy: boolean;
  };
  readonly catalogue: {
    readonly source: Env['PRODUCT_SOURCE'];
    readonly fakeStore: { readonly baseUrl: string; readonly timeoutMs: number };
    readonly cacheTtlMs: number;
    readonly currency: string;
    readonly featuredProductIds: readonly number[];
    readonly categoryTopRatedCount: number;
    readonly ratingPriorWeight: number;
  };
  readonly email: {
    readonly transport: Env['EMAIL_TRANSPORT'];
    readonly salesInbox: string;
    readonly from: string;
    readonly smtp: {
      readonly host: string;
      readonly port: number;
      readonly secure: boolean;
      readonly user: string | undefined;
      readonly pass: string | undefined;
    };
  };
  readonly enquiry: { readonly rateLimitPerMinute: number };
}

export class ConfigError extends Error {
  override readonly name = 'ConfigError';
}

export function loadConfig(source: NodeJS.ProcessEnv = process.env): AppConfig {
  const parsed = envSchema.safeParse(source);
  if (!parsed.success) {
    const problems = parsed.error.issues
      .map((issue) => `  ${issue.path.join('.')}: ${issue.message}`)
      .join('\n');
    throw new ConfigError(`Invalid configuration:\n${problems}`);
  }
  const env = parsed.data;
  return {
    env: env.NODE_ENV,
    server: {
      host: env.HOST,
      port: env.PORT,
      logLevel: env.LOG_LEVEL,
      trustProxy: env.TRUST_PROXY,
    },
    catalogue: {
      source: env.PRODUCT_SOURCE,
      fakeStore: {
        baseUrl: env.FAKE_STORE_BASE_URL.replace(/\/+$/, ''),
        timeoutMs: env.FAKE_STORE_TIMEOUT_MS,
      },
      cacheTtlMs: env.CATALOGUE_CACHE_TTL_SECONDS * 1000,
      currency: env.CATALOGUE_CURRENCY,
      featuredProductIds: env.FEATURED_PRODUCT_IDS,
      categoryTopRatedCount: env.CATEGORY_TOP_RATED_COUNT,
      ratingPriorWeight: env.RATING_PRIOR_WEIGHT,
    },
    email: {
      transport: env.EMAIL_TRANSPORT,
      salesInbox: env.SALES_INBOX_EMAIL,
      from: env.EMAIL_FROM,
      smtp: {
        host: env.SMTP_HOST,
        port: env.SMTP_PORT,
        secure: env.SMTP_SECURE,
        user: env.SMTP_USER,
        pass: env.SMTP_PASS,
      },
    },
    enquiry: { rateLimitPerMinute: env.ENQUIRY_RATE_LIMIT_PER_MINUTE },
  };
}
