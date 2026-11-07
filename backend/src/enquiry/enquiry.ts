import { z } from 'zod';

/**
 * What a customer submits. Validated on the server even though the app validates too:
 * the server is the only check a malicious client cannot skip.
 *
 * Only the product id is accepted. The product title and price in the email come from our
 * catalogue, so a client cannot make the sales team see a product or price it invented.
 */
export const enquiryRequestSchema = z.strictObject({
  productId: z.number().int().positive(),
  name: z.string().trim().min(1, 'Name is required').max(100),
  email: z.string().trim().max(254).pipe(z.email('Enter a valid email address')),
  phone: z
    .string()
    .trim()
    .max(20)
    .regex(/^\+?[0-9 ()-]{6,20}$/, 'Enter a valid phone number')
    .optional()
    .or(z.literal('').transform(() => undefined)),
  message: z.string().trim().max(1000).optional(),
});

export type EnquiryRequest = z.infer<typeof enquiryRequestSchema>;

export interface EnquiryReceipt {
  /** Quoted to the customer and in the email subject so sales can match replies. */
  readonly reference: string;
}
