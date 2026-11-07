import { describe, expect, it } from 'vitest';
import {
  matchesAllWords,
  normalise,
  toDisplayName,
  toSearchWords,
  toSlug,
} from '../src/catalogue/text.js';

/** Test helper: match raw text against a raw term, as a search does. */
const matches = (text: string, term: string) =>
  matchesAllWords(toSearchWords(text), toSearchWords(term));

describe('text helpers', () => {
  it('normalises case, apostrophes, accents and punctuation', () => {
    expect(normalise("  Men's  Café-Style, T-Shirt ")).toBe('mens cafe style t shirt');
  });

  it('makes URL-safe slugs from Fake Store category names', () => {
    expect(toSlug("men's clothing")).toBe('mens-clothing');
    expect(toSlug("women's clothing")).toBe('womens-clothing');
    expect(toSlug('electronics')).toBe('electronics');
  });

  it('capitalises display names', () => {
    expect(toDisplayName("men's clothing")).toBe("Men's clothing");
  });

  describe('matchesAllWords', () => {
    const text = "Mens Cotton Jacket great outerwear men's clothing";

    it.each([
      ['jacket', true],
      ['JACKET', true],
      ['mens jacket', true],
      ["men's", true],
      ['cotton outerwear', true],
      ['jack', true],
      ['jacket wool', false],
      ['laptop', false],
      ['acket', false],
    ])('"%s" -> %s', (term, expected) => {
      expect(matches(text, term)).toBe(expected);
    });

    it('does not let "mens" match "womens"', () => {
      expect(matches("Rain Jacket women's clothing", 'mens')).toBe(false);
    });

    it('never matches an empty or punctuation-only term', () => {
      expect(matches(text, '   ')).toBe(false);
      expect(matches(text, '!!')).toBe(false);
    });
  });
});
