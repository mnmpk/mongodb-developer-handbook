import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

const baseURL = environment.japiUrl;
@Injectable({
	providedIn: 'root'
})
export class AIService {

	constructor(private httpClient: HttpClient) { }

	test(message: string): Observable<any> {
		return this.httpClient.post<any>(`${baseURL}/test-ai`, { params: message });
	}
}
