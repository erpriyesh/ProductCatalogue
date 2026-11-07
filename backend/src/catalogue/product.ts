/**
 * The catalogue's own product model. It is what our API returns, independent of any data
 * source's shape, so swapping Fake Store for a real PIM/CMS does not change the app contract.
 */
export interface Product {
  readonly id: number;
  readonly title: string;
  readonly description: string;
  readonly price: Money;
  readonly category: Category;
  readonly imageUrl: string;
  readonly rating: Rating;
}

export interface Money {
  /** Decimal amount in major units, e.g. 109.95. */
  readonly amount: number;
  /** ISO 4217 currency code, e.g. AUD. */
  readonly currency: string;
}

export interface Category {
  /** URL-safe identifier, e.g. "mens-clothing". */
  readonly slug: string;
  /** Display name, e.g. "Men's clothing". */
  readonly name: string;
}

export interface Rating {
  /** Average score out of 5. */
  readonly average: number;
  readonly count: number;
}
