import { CatalogueService } from './catalogue/catalogueService.js';
import { createProductRepository } from './catalogue/productSourceFactory.js';
import type { AppConfig } from './config/config.js';
import { createEmailSender } from './enquiry/email/emailSenderFactory.js';
import { EnquiryService } from './enquiry/enquiryService.js';
import type { Logger } from './shared/logger.js';

export interface Container {
  readonly catalogue: CatalogueService;
  readonly enquiries: EnquiryService;
}

/**
 * Composition root: builds the object graph from configuration. The only place that connects
 * concrete adapters (chosen by the factories) to the services that use them.
 */
export function createContainer(config: AppConfig, log: Logger): Container {
  const products = createProductRepository(config.catalogue, log);

  const catalogue = new CatalogueService(
    products,
    {
      featuredProductIds: config.catalogue.featuredProductIds,
      categoryTopRatedCount: config.catalogue.categoryTopRatedCount,
      ratingPriorWeight: config.catalogue.ratingPriorWeight,
    },
    (missingIds) => log.warn({ missingIds }, 'Featured product ids not found in catalogue'),
  );

  const enquiries = new EnquiryService(catalogue, createEmailSender(config.email, log), {
    salesInbox: config.email.salesInbox,
  });

  return { catalogue, enquiries };
}
