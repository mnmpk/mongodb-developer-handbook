import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
const baseURL = environment.japiUrl + "/change-stream";
@Injectable({
	providedIn: 'root'
})
export class ChangeStreamService {

	constructor(private httpClient: HttpClient) { }
	list(): any {
		return this.httpClient.get<any>(`${baseURL}/list`);
	}
	watch(req: any): any {
		return this.httpClient.get<any>(`${baseURL}/watch/${req.collection}`, { params: { pipeline: req.pipeline, mode: req.mode } });
	}
	unwatch(req: any){
		return this.httpClient.get<any>(`${baseURL}/unwatch/${req.collection}`, { params: { pipeline: req.pipeline, mode: req.mode } });
	}
}
