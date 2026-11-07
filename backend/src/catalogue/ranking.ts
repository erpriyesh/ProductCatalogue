import type { Product } from './product.js';

/**
 * Ranks products by a weighted (Bayesian) rating, the approach IMDb uses for its Top 250.
 *
 *   score = (count * average + priorWeight * priorMean) / (count + priorWeight)
 *
 * Each product's own average is blended with the average of the products being ranked
 * (`priorMean`), and its review count decides how much it pulls away from that mean:
 *
 * - 5.0 from 1 review, priorWeight 25, mean 3.5   -> (5 + 87.5) / 26    = 3.56  (barely above average)
 * - 4.8 from 400 reviews, same settings            -> (1920 + 87.5) / 425 = 4.72  (close to its own 4.8)
 *
 * So a product needs both good ratings and enough of them to be "top rated". `priorWeight` is
 * how many reviews it takes to be trusted half way (RATING_PRIOR_WEIGHT, default 25). 0 means
 * plain average.
 */
export function weightedRating(
  average: number,
  count: number,
  priorMean: number,
  priorWeight: number,
): number {
  if (count + priorWeight === 0) return priorMean;
  return (count * average + priorWeight * priorMean) / (count + priorWeight);
}

/**
 * Returns the products best first by weighted rating. Ties go to more reviews, then id, so the
 * order is stable. The prior mean is the review-weighted average of the products being ranked
 * (a category, or a set of search results), so each list is judged against its own peers.
 */
export function rankByWeightedRating(products: readonly Product[], priorWeight: number): Product[] {
  const totalReviews = products.reduce((sum, p) => sum + p.rating.count, 0);
  const priorMean =
    totalReviews === 0
      ? 0
      : products.reduce((sum, p) => sum + p.rating.average * p.rating.count, 0) / totalReviews;

  return products
    .map((product) => ({
      product,
      score: weightedRating(product.rating.average, product.rating.count, priorMean, priorWeight),
    }))
    .sort(
      (a, b) =>
        b.score - a.score ||
        b.product.rating.count - a.product.rating.count ||
        a.product.id - b.product.id,
    )
    .map((entry) => entry.product);
}
