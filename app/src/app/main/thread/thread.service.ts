import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

const baseURL = environment.japiUrl;
@Injectable({
	providedIn: 'root'
})
export class ThreadService {

	constructor(private httpClient: HttpClient) { }

}
