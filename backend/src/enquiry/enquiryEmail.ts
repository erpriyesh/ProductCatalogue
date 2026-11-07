import type { Product } from '../catalogue/product.js';
import type { EmailMessage } from './email/emailSender.js';
import type { EnquiryRequest } from './enquiry.js';

/**
 * Builds the email the sales inbox receives. Pure function: easy to test, and the one place
 * to change when the creative team designs a branded template.
 */
export function buildSalesEnquiryEmail(input: {
  readonly reference: string;
  readonly enquiry: EnquiryRequest;
  readonly product: Product;
  readonly salesInbox: string;
}): EmailMessage {
  const { reference, enquiry, product, salesInbox } = input;
  const price = formatPrice(product.price.amount, product.price.currency);
  const rows: [string, string][] = [
    ['Reference', reference],
    ['Product', `${product.title} (#${product.id})`],
    ['Category', product.category.name],
    ['Price', price],
    ['Customer', enquiry.name],
    ['Email', enquiry.email],
    ['Phone', enquiry.phone ?? 'Not given'],
    ['Message', enquiry.message || 'No message'],
  ];

  return {
    to: salesInbox,
    // Sales can reply straight to the customer.
    replyTo: enquiry.email,
    subject: singleLine(`New enquiry ${reference}: ${product.title}`),
    text: [
      'A customer has enquired about a product.',
      '',
      ...rows.map(([label, value]) => `${label}: ${value}`),
    ].join('\n'),
    html: [
      '<h2>New product enquiry</h2>',
      '<table cellpadding="6" style="border-collapse:collapse">',
      ...rows.map(
        ([label, value]) =>
          `<tr><th align="left">${escapeHtml(label)}</th><td style="white-space:pre-wrap">${escapeHtml(value)}</td></tr>`,
      ),
      '</table>',
    ].join('\n'),
  };
}

/** Customer input goes into HTML, so it is escaped: no markup or script injection into the inbox. */
export function escapeHtml(value: string): string {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

/** Line breaks in a subject could forge extra headers; flatten them. */
function singleLine(value: string): string {
  return value.replace(/[\r\n]+/g, ' ').slice(0, 200);
}

function formatPrice(amount: number, currency: string): string {
  return new Intl.NumberFormat('en-AU', { style: 'currency', currency }).format(amount);
}
