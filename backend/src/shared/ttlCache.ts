/**
 * Caches one async value for a time-to-live.
 *
 * - Concurrent callers share a single in-flight load (no stampede on the upstream).
 * - If a refresh fails and an older value exists, the stale value is served and the error
 *   logged, so a flaky upstream degrades gracefully instead of breaking every page.
 *
 * In-memory is right for a single instance; with several instances this would move to Redis
 * (ElastiCache) behind the same shape.
 */
export class TtlCache<T> {
  private value: { data: T; loadedAt: number } | undefined;
  private inFlight: Promise<T> | undefined;

  constructor(
    private readonly load: () => Promise<T>,
    private readonly ttlMs: number,
    private readonly onStaleServed: (error: unknown) => void = () => {},
    private readonly now: () => number = Date.now,
  ) {}

  async get(): Promise<T> {
    if (this.value && this.now() - this.value.loadedAt < this.ttlMs) {
      return this.value.data;
    }
    this.inFlight ??= this.refresh().finally(() => {
      this.inFlight = undefined;
    });
    return this.inFlight;
  }

  private async refresh(): Promise<T> {
    try {
      const data = await this.load();
      this.value = { data, loadedAt: this.now() };
      return data;
    } catch (error) {
      if (this.value) {
        this.onStaleServed(error);
        return this.value.data;
      }
      throw error;
    }
  }
}
