import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

const baseURL = environment.japiUrl;
@Injectable({
	providedIn: 'root'
})
export class SessionService {

	constructor(private httpClient: HttpClient) { }

	put(text: string): Observable<string> {
		return this.httpClient.get<string>(`${baseURL}/session/put`, { params: { s: text }, withCredentials: true });
	}
	clear(): Observable<string> {
		return this.httpClient.get<string>(`${baseURL}/session/clear`, { withCredentials: true });
	}
	login(uId: string): Observable<string> {
		return this.httpClient.get<string>(`${baseURL}/session/login`, { params: { uId: uId }, withCredentials: true });
	}
	logout(): Observable<string> {
		return this.httpClient.get<string>(`${baseURL}/session/logout`, { withCredentials: true });
	}
}
