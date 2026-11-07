import type { AppConfig } from '../config/config.js';
import type { Logger } from '../shared/logger.js';
import { FakeStoreProductRepository } from './fakestore/fakeStoreProductRepository.js';
import type { ProductRepository } from './productRepository.js';

/**
 * Picks the product data source from configuration (PRODUCT_SOURCE). Adding a source (a PIM, a
 * CMS, a database) is: write a class implementing ProductRepository, add a case here, add its
 * name to the PRODUCT_SOURCE enum in config.ts. Routes, rules and the app are untouched.
 */
export function createProductRepository(
  config: AppConfig['catalogue'],
  log: Logger,
): ProductRepository {
  switch (config.source) {
    case 'fakestore':
      return new FakeStoreProductRepository({
        baseUrl: config.fakeStore.baseUrl,
        timeoutMs: config.fakeStore.timeoutMs,
        cacheTtlMs: config.cacheTtlMs,
        currency: config.currency,
        onStaleServed: (err) =>
          log.warn({ err }, 'Product source failed; serving cached catalogue'),
      });
  }
}
