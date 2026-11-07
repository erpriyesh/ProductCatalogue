/**
 * Errors the HTTP layer knows how to turn into responses. Anything else becomes a generic 500
 * so internal details never leak to clients.
 */
export abstract class AppError extends Error {
  abstract readonly statusCode: number;
  abstract readonly code: string;
  readonly details: unknown;

  constructor(message: string, options?: { cause?: unknown; details?: unknown }) {
    super(message, options?.cause === undefined ? undefined : { cause: options.cause });
    this.name = new.target.name;
    this.details = options?.details;
  }
}

export class ValidationError extends AppError {
  readonly statusCode = 400;
  readonly code = 'VALIDATION_FAILED';
}

export class NotFoundError extends AppError {
  readonly statusCode = 404;
  readonly code = 'NOT_FOUND';
}

/** The product data source failed or returned something we could not understand. */
export class UpstreamError extends AppError {
  readonly statusCode = 502;
  readonly code = 'UPSTREAM_UNAVAILABLE';
}

/** The email provider did not accept the message. */
export class EmailDeliveryError extends AppError {
  readonly statusCode = 502;
  readonly code = 'EMAIL_DELIVERY_FAILED';
}
