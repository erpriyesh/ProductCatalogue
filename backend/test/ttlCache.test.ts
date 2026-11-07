import { describe, expect, it, vi } from 'vitest';
import { TtlCache } from '../src/shared/ttlCache.js';

function clock(start = 0) {
  let now = start;
  return { now: () => now, advance: (ms: number) => (now += ms) };
}

describe('TtlCache', () => {
  it('loads once and serves from cache within the TTL', async () => {
    const time = clock();
    const load = vi.fn().mockResolvedValue('v1');
    const cache = new TtlCache(load, 1000, undefined, time.now);

    expect(await cache.get()).toBe('v1');
    time.advance(999);
    expect(await cache.get()).toBe('v1');
    expect(load).toHaveBeenCalledTimes(1);
  });

  it('reloads after the TTL expires', async () => {
    const time = clock();
    const load = vi.fn().mockResolvedValueOnce('v1').mockResolvedValueOnce('v2');
    const cache = new TtlCache(load, 1000, undefined, time.now);

    await cache.get();
    time.advance(1000);
    expect(await cache.get()).toBe('v2');
  });

  it('shares one in-flight load between concurrent callers', async () => {
    const load = vi.fn().mockResolvedValue('v1');
    const cache = new TtlCache(load, 1000);

    await Promise.all([cache.get(), cache.get(), cache.get()]);
    expect(load).toHaveBeenCalledTimes(1);
  });

  it('serves the stale value and reports it when a refresh fails', async () => {
    const time = clock();
    const failure = new Error('upstream down');
    const load = vi.fn().mockResolvedValueOnce('v1').mockRejectedValueOnce(failure);
    const onStale = vi.fn();
    const cache = new TtlCache(load, 1000, onStale, time.now);

    await cache.get();
    time.advance(5000);
    expect(await cache.get()).toBe('v1');
    expect(onStale).toHaveBeenCalledWith(failure);
  });

  it('propagates the error when there is nothing cached yet, then retries next time', async () => {
    const load = vi.fn().mockRejectedValueOnce(new Error('down')).mockResolvedValueOnce('v1');
    const cache = new TtlCache(load, 1000);

    await expect(cache.get()).rejects.toThrow('down');
    expect(await cache.get()).toBe('v1');
  });
});
