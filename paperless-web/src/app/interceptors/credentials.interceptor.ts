import { HttpInterceptorFn } from '@angular/common/http';

/** Schickt bei jedem Request das HttpOnly-Cookie mit dem JWT mit. */
export const credentialsInterceptor: HttpInterceptorFn = (req, next) =>
  next(req.clone({ withCredentials: true }));
