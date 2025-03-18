import { inject } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpRequest, HttpEvent, HttpResponse, HttpEventType, HttpInterceptorFn, HttpHandlerFn } from '@angular/common/http';
import { LoaderService } from './loader.service';


export const loaderInterceptor: HttpInterceptorFn = (
	request: HttpRequest<any>,
	next: HttpHandlerFn
): Observable<HttpEvent<any>> => {
	const loaderService = inject(LoaderService);
	loaderService.load(request, "Requesting server... ");
	return new Observable((observer: any) => {
		const subscription = next(request)
			.subscribe({
				next: (event) => {
					if (event.type === HttpEventType.UploadProgress && event.total) {
						loaderService.progress = Math.round(100 * event.loaded / event.total);
					} else if (event instanceof HttpResponse) {
						observer.next(event);
					}
				},
				error: (err) => {
					alert(err.error.message || err.message);
					observer.error(err);
				},
				complete: () => {
					loaderService.finish(request);
					observer.complete();
				}
			});
		return () => {
			loaderService.finish(request);
			subscription.unsubscribe();
		};
	});
};