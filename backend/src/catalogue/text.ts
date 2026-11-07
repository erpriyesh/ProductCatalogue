/**
 * Text helpers shared by slugs and search, so "Men's" matches "mens" in both places.
 */

/** Lower-case, drop apostrophes and accents, collapse everything else to single spaces. */
export function normalise(text: string): string {
  return text
    .normalize('NFKD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .replace(/['’]/g, '')
    .replace(/[^a-z0-9]+/g, ' ')
    .trim();
}

/** "men's clothing" -> "mens-clothing" */
export function toSlug(text: string): string {
  return normalise(text).replace(/ /g, '-');
}

/** "men's clothing" -> "Men's clothing" */
export function toDisplayName(text: string): string {
  const trimmed = text.trim();
  return trimmed.charAt(0).toUpperCase() + trimmed.slice(1);
}

/** "Men's Cotton-Jacket" -> ["mens", "cotton", "jacket"]. Computed once per product, not per search. */
export function toSearchWords(text: string): string[] {
  return normalise(text).split(' ').filter(Boolean);
}

/**
 * A product matches when every search word starts one of its words.
 *
 * - "mens jacket" finds "Mens Cotton Jacket" (a whole-phrase substring match would not).
 * - "jack" finds "Jacket" (prefix, so partial words work).
 * - "mens" does not find "Womens clothing" (a plain substring match would).
 */
export function matchesAllWords(
  textWords: readonly string[],
  termWords: readonly string[],
): boolean {
  return (
    termWords.length > 0 &&
    termWords.every((termWord) => textWords.some((textWord) => textWord.startsWith(termWord)))
  );
}
