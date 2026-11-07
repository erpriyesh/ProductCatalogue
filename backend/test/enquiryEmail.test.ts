import { describe, expect, it } from 'vitest';
import type { Product } from '../src/catalogue/product.js';
import { buildSalesEnquiryEmail, escapeHtml } from '../src/enquiry/enquiryEmail.js';

const product: Product = {
  id: 2,
  title: 'Mens Cotton Jacket',
  description: 'Warm',
  price: { amount: 55.99, currency: 'AUD' },
  category: { slug: 'mens-clothing', name: "Men's clothing" },
  imageUrl: 'https://example.test/2.png',
  rating: { average: 4.7, count: 500 },
};

describe('buildSalesEnquiryEmail', () => {
  const email = buildSalesEnquiryEmail({
    reference: 'ENQ-1',
    product,
    salesInbox: 'sales@example.test',
    enquiry: {
      productId: 2,
      name: 'Ada <script>alert(1)</script>',
      email: 'ada@example.test',
      message: 'Do you have size L?',
    },
  });

  it('goes to the sales inbox with reply-to set to the customer', () => {
    expect(email.to).toBe('sales@example.test');
    expect(email.replyTo).toBe('ada@example.test');
    expect(email.subject).toBe('New enquiry ENQ-1: Mens Cotton Jacket');
  });

  it('includes the product details from the catalogue and the customer details', () => {
    expect(email.text).toContain('Product: Mens Cotton Jacket (#2)');
    expect(email.text).toContain('Price: $55.99');
    expect(email.text).toContain('Phone: Not given');
    expect(email.text).toContain('Message: Do you have size L?');
  });

  it('escapes customer input in the HTML body', () => {
    expect(email.html).not.toContain('<script>');
    expect(email.html).toContain('Ada &lt;script&gt;alert(1)&lt;/script&gt;');
  });

  it('flattens line breaks out of the subject', () => {
    const tricky = buildSalesEnquiryEmail({
      reference: 'ENQ-2',
      product: { ...product, title: 'Jacket\r\nBcc: victim@example.test' },
      salesInbox: 'sales@example.test',
      enquiry: { productId: 2, name: 'A', email: 'a@example.test' },
    });
    expect(tricky.subject).not.toMatch(/[\r\n]/);
  });
});

describe('escapeHtml', () => {
  it('escapes the five HTML-significant characters', () => {
    expect(escapeHtml(`<a href="x">'&'</a>`)).toBe(
      '&lt;a href=&quot;x&quot;&gt;&#39;&amp;&#39;&lt;/a&gt;',
    );
  });
});
