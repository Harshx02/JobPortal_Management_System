import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class CacheService {
  private cache = new Map<string, { response: HttpResponse<any>, expiry: number }>();
  private readonly DEFAULT_TTL = 300000; // 5 minutes in ms

  constructor() {}

  /**
   * Puts a response into the cache.
   * If persistence is needed, could also write to SessionStorage here.
   */
  put(url: string, response: HttpResponse<any>, ttl: number = this.DEFAULT_TTL): void {
    const expiry = Date.now() + ttl;
    this.cache.set(url, { response, expiry });
    
    // Optional: Sync to SessionStorage for persistence across F5
    try {
      sessionStorage.setItem(`cache_${url}`, JSON.stringify({
        body: response.body,
        status: response.status,
        statusText: response.statusText,
        url: response.url,
        expiry
      }));
    } catch (e) {
      console.warn('SessionStorage cache failed (likely quota or private mode)', e);
    }
  }

  /**
   * Gets a response from the cache if it hasn't expired.
   */
  get(url: string): HttpResponse<any> | null {
    // 1. Check in-memory Map first (fastest)
    const cached = this.cache.get(url);
    if (cached) {
      if (Date.now() <= cached.expiry) {
        return cached.response;
      }
      this.cache.delete(url);
    }

    // 2. Check SessionStorage (survives refresh)
    const sessionData = sessionStorage.getItem(`cache_${url}`);
    if (sessionData) {
      try {
        const parsed = JSON.parse(sessionData);
        if (Date.now() <= parsed.expiry) {
          const response = new HttpResponse({
            body: parsed.body,
            status: parsed.status,
            statusText: parsed.statusText,
            url: parsed.url
          });
          // Hydrate back to in-memory map
          this.cache.set(url, { response, expiry: parsed.expiry });
          return response;
        }
        sessionStorage.removeItem(`cache_${url}`);
      } catch (e) {
        sessionStorage.removeItem(`cache_${url}`);
      }
    }

    return null;
  }

  /**
   * Clears specific entry or entire cache.
   */
  clear(url?: string): void {
    if (url) {
      this.cache.delete(url);
      sessionStorage.removeItem(`cache_${url}`);
    } else {
      this.cache.clear();
      Object.keys(sessionStorage)
        .filter(k => k.startsWith('cache_'))
        .forEach(k => sessionStorage.removeItem(k));
    }
  }
}
