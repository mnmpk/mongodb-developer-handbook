import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

const baseURL = environment.japiUrl;
@Injectable({
	providedIn: 'root'
})
export class CacheService {

	constructor(private httpClient: HttpClient) { }

	apiWithCache(size: number): Observable<string> {
		return this.httpClient.get<string>(`${baseURL}/cache/data`, { params: {size:size} });
	}
	clear(size: number): Observable<string> {
		return this.httpClient.get<string>(`${baseURL}/cache/clear`, { params: {size:size} });
	}
}
