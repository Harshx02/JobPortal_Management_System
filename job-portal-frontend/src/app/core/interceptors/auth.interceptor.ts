import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
) => {
  const token = localStorage.getItem('jp_token');
  const userId = localStorage.getItem('jp_userId');

  let headers = req.headers;
  if (token) {
    headers = headers.set('Authorization', `Bearer ${token}`);
  }
  if (userId) {
    headers = headers.set('X-User-Id', userId);
  }

  if (token || userId) {
    const cloned = req.clone({ headers });
    return next(cloned);
  }

  return next(req);
};
