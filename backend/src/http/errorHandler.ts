import type { FastifyError, FastifyReply, FastifyRequest } from 'fastify';
import { ZodError } from 'zod';
import { AppError } from '../shared/errors.js';

/** Every error response has this one shape, so clients handle errors in one place. */
export interface ErrorBody {
  error: { code: string; message: string; details?: unknown };
}

export function errorHandler(
  error: FastifyError | Error,
  request: FastifyRequest,
  reply: FastifyReply,
): FastifyReply {
  if (error instanceof ZodError) {
    return reply
      .status(400)
      .send(body('VALIDATION_FAILED', 'Some fields are invalid', fieldIssues(error)));
  }

  if (error instanceof AppError) {
    if (error.statusCode >= 500) {
      request.log.error({ err: error, cause: error.cause }, error.message);
    }
    return reply.status(error.statusCode).send(body(error.code, error.message, error.details));
  }

  // Errors Fastify and its plugins raise with a client status: malformed JSON, wrong content
  // type, body too large, rate limited. Their messages are safe to show.
  const statusCode = (error as { statusCode?: unknown }).statusCode;
  if (typeof statusCode === 'number' && statusCode >= 400 && statusCode < 500) {
    const code = (error as { code?: unknown }).code;
    return reply
      .status(statusCode)
      .send(body(typeof code === 'string' ? code : 'BAD_REQUEST', error.message));
  }

  // Anything else is a bug: log it in full, tell the client nothing internal.
  request.log.error({ err: error }, 'Unhandled error');
  return reply.status(500).send(body('INTERNAL_ERROR', 'Something went wrong on our side'));
}

export function notFoundHandler(request: FastifyRequest, reply: FastifyReply): FastifyReply {
  return reply.status(404).send(body('NOT_FOUND', `No route for ${request.method} ${request.url}`));
}

function body(code: string, message: string, details?: unknown): ErrorBody {
  return { error: details === undefined ? { code, message } : { code, message, details } };
}

function fieldIssues(error: ZodError): { field: string; message: string }[] {
  return error.issues.map((issue) => ({ field: issue.path.join('.'), message: issue.message }));
}
