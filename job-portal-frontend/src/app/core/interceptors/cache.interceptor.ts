import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { of, tap } from 'rxjs';
import { CacheService } from '../services/cache.service';

export const cacheInterceptor: HttpInterceptorFn = (req, next) => {
  // Only cache GET requests
  if (req.method !== 'GET') {
    return next(req);
  }

  // Bypass cache if 'reset-cache' header is present
  if (req.headers.get('reset-cache')) {
    return next(req);
  }

  const cache = inject(CacheService);
  const cachedResponse = cache.get(req.urlWithParams);

  if (cachedResponse) {
    // Return cached response as an observable
    return of(cachedResponse);
  }

  // If not cached, proceed with the request and cache the result
  return next(req).pipe(
    tap(event => {
      if (event instanceof HttpResponse) {
        cache.put(req.urlWithParams, event);
      }
    })
  );
};
