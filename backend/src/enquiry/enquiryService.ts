import { randomBytes } from 'node:crypto';
import type { CatalogueService } from '../catalogue/catalogueService.js';
import { EmailDeliveryError } from '../shared/errors.js';
import type { EmailSender } from './email/emailSender.js';
import type { EnquiryReceipt, EnquiryRequest } from './enquiry.js';
import { buildSalesEnquiryEmail } from './enquiryEmail.js';

export interface EnquiryServiceOptions {
  readonly salesInbox: string;
  readonly newReference?: () => string;
}

/**
 * Turns a validated enquiry into an email to the sales inbox.
 *
 * The receipt is returned only after the provider accepts the email, so the app's "Thank you"
 * is never shown for an enquiry that was lost. Next step: persist the enquiry and send through
 * an outbox/queue (SQS) so a provider outage delays the email instead of failing the request.
 */
export class EnquiryService {
  private readonly newReference: () => string;

  constructor(
    private readonly catalogue: CatalogueService,
    private readonly email: EmailSender,
    private readonly options: EnquiryServiceOptions,
  ) {
    this.newReference = options.newReference ?? defaultReference;
  }

  async submit(enquiry: EnquiryRequest): Promise<EnquiryReceipt> {
    // Throws NotFoundError (404) when the product does not exist.
    const product = await this.catalogue.product(enquiry.productId);
    const reference = this.newReference();
    const message = buildSalesEnquiryEmail({
      reference,
      enquiry,
      product,
      salesInbox: this.options.salesInbox,
    });
    try {
      await this.email.send(message);
    } catch (error) {
      throw new EmailDeliveryError('We could not send your enquiry. Please try again.', {
        cause: error,
      });
    }
    return { reference };
  }
}

/** e.g. "ENQ-4F7A9C2B": short enough to read over the phone, random enough not to collide. */
function defaultReference(): string {
  return `ENQ-${randomBytes(4).toString('hex').toUpperCase()}`;
}
