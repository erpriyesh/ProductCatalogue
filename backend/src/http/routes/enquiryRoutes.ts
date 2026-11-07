import type { FastifyInstance } from 'fastify';
import { enquiryRequestSchema } from '../../enquiry/enquiry.js';
import type { EnquiryService } from '../../enquiry/enquiryService.js';

export function enquiryRoutes(enquiries: EnquiryService, rateLimitPerMinute: number) {
  return async (app: FastifyInstance) => {
    app.post(
      '/enquiries',
      {
        // Enquiries send email, so they are the endpoint worth abusing: limit per client IP.
        config: { rateLimit: { max: rateLimitPerMinute, timeWindow: '1 minute' } },
        bodyLimit: 16 * 1024,
      },
      async (request, reply) => {
        const enquiry = enquiryRequestSchema.parse(request.body);
        const receipt = await enquiries.submit(enquiry);
        // Log the reference and product only: no customer personal data in logs.
        request.log.info(
          { reference: receipt.reference, productId: enquiry.productId },
          'Enquiry sent',
        );
        return reply.status(201).send(receipt);
      },
    );
  };
}
