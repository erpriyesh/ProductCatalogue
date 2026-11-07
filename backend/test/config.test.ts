import { describe, expect, it } from 'vitest';
import { ConfigError, loadConfig } from '../src/config/config.js';

describe('loadConfig', () => {
  it('provides working local defaults', () => {
    const config = loadConfig({});
    expect(config.server.port).toBe(3000);
    expect(config.catalogue.featuredProductIds).toEqual([11, 12, 3, 18, 5]);
    expect(config.catalogue.currency).toBe('AUD');
    expect(config.email.transport).toBe('smtp');
    expect(config.catalogue.source).toBe('fakestore');
    expect(config.email.smtp).toMatchObject({ host: 'localhost', port: 1025, secure: false });
  });

  it('parses featured ids, booleans and strips a trailing slash from the base URL', () => {
    const config = loadConfig({
      FEATURED_PRODUCT_IDS: ' 4, 8 ,15 ',
      SMTP_SECURE: 'true',
      SMTP_USER: '',
      FAKE_STORE_BASE_URL: 'https://example.test/',
    });
    expect(config.catalogue.featuredProductIds).toEqual([4, 8, 15]);
    expect(config.email.smtp.secure).toBe(true);
    expect(config.email.smtp.user).toBeUndefined();
    expect(config.catalogue.fakeStore.baseUrl).toBe('https://example.test');
  });

  it('treats an empty FEATURED_PRODUCT_IDS as "choose automatically"', () => {
    expect(loadConfig({ FEATURED_PRODUCT_IDS: '' }).catalogue.featuredProductIds).toEqual([]);
    expect(loadConfig({ FEATURED_PRODUCT_IDS: ' , ' }).catalogue.featuredProductIds).toEqual([]);
  });

  it('fails fast with every problem listed', () => {
    const load = () =>
      loadConfig({ PORT: 'abc', FEATURED_PRODUCT_IDS: '1,two', CATALOGUE_CURRENCY: 'dollars' });
    expect(load).toThrow(ConfigError);
    for (const variable of ['PORT', 'FEATURED_PRODUCT_IDS', 'CATALOGUE_CURRENCY']) {
      expect(load).toThrow(variable);
    }
  });
});
