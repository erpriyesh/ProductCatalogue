import { CatalogueService } from '../../src/catalogue/catalogueService.js';
import { FakeStoreProductRepository } from '../../src/catalogue/fakestore/fakeStoreProductRepository.js';
import type { EmailMessage, EmailSender } from '../../src/enquiry/email/emailSender.js';
import { EnquiryService } from '../../src/enquiry/enquiryService.js';
import { buildApp } from '../../src/http/app.js';

/** A trimmed copy of Fake Store's real response shape, with ratings chosen to make ordering obvious. */
export const fakeStoreProducts = [
  {
    id: 1,
    title: 'Fjallraven Backpack',
    price: 109.95,
    description: 'Fits 15 inch laptops',
    category: "men's clothing",
    image: 'https://fakestoreapi.com/img/1.png',
    rating: { rate: 3.9, count: 120 },
  },
  {
    id: 2,
    title: 'Mens Cotton Jacket',
    price: 55.99,
    description: 'Great outerwear for spring',
    category: "men's clothing",
    image: 'https://fakestoreapi.com/img/2.png',
    rating: { rate: 4.7, count: 500 },
  },
  {
    id: 3,
    title: 'Mens Casual Slim Fit',
    price: 15.99,
    description: 'Colours may vary',
    category: "men's clothing",
    image: 'https://fakestoreapi.com/img/3.png',
    rating: { rate: 2.1, count: 430 },
  },
  {
    id: 4,
    title: 'Mens Premium T-Shirt',
    price: 22.3,
    description: 'Slim fitting style',
    category: "men's clothing",
    image: 'https://fakestoreapi.com/img/4.png',
    rating: { rate: 4.7, count: 259 },
  },
  {
    id: 5,
    title: 'WD 4TB Gaming Drive',
    price: 114,
    description: 'Works with PlayStation 4',
    category: 'electronics',
    image: 'https://fakestoreapi.com/img/5.png',
    rating: { rate: 4.8, count: 400 },
  },
  {
    id: 6,
    title: 'Gold Petite Micropave',
    price: 168,
    description: 'Satisfaction guaranteed <b>bold</b>',
    category: 'jewelery',
    image: 'https://fakestoreapi.com/img/6.png',
    rating: { rate: 3.9, count: 70 },
  },
];

export function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'content-type': 'application/json' },
  });
}

/** A `fetch` stand-in that serves the fixture, and counts calls so caching can be asserted. */
export function fakeFetch(
  respond: () => Response | Promise<Response> = () => jsonResponse(fakeStoreProducts),
) {
  const calls: string[] = [];
  const fn = (async (input: string | URL | Request) => {
    calls.push(String(input));
    return respond();
  }) as typeof fetch;
  return Object.assign(fn, { calls });
}

export class RecordingEmailSender implements EmailSender {
  readonly sent: EmailMessage[] = [];
  failWith: Error | undefined;

  async send(message: EmailMessage): Promise<void> {
    if (this.failWith) throw this.failWith;
    this.sent.push(message);
  }
}

export function repository(fetchFn: typeof fetch = fakeFetch()) {
  return new FakeStoreProductRepository({
    baseUrl: 'https://fake.store',
    timeoutMs: 1000,
    cacheTtlMs: 60_000,
    currency: 'AUD',
    fetch: fetchFn,
  });
}

/** The real app with real services, a stubbed upstream and a recording email sender. */
export async function testApp(options: { fetch?: typeof fetch; rateLimitPerMinute?: number } = {}) {
  const catalogue = new CatalogueService(repository(options.fetch), {
    featuredProductIds: [5, 2, 6],
    categoryTopRatedCount: 2,
    ratingPriorWeight: 25,
  });
  const email = new RecordingEmailSender();
  const enquiries = new EnquiryService(catalogue, email, {
    salesInbox: 'sales@example.test',
    newReference: () => 'ENQ-TEST0001',
  });
  const app = await buildApp({
    catalogue,
    enquiries,
    enquiryRateLimitPerMinute: options.rateLimitPerMinute ?? 100,
  });
  return { app, email };
}
