import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { Stat } from '../../shared/models/stats';

const baseURL = environment.japiUrl;
@Injectable({
	providedIn: 'root'
})
export class ThreadService {

	constructor(private httpClient: HttpClient) { }

	run(value:{size: string, tasks: string, serviceTime:string, waitTime:string}): Observable<Stat<any>> {
		return this.httpClient.get<Stat<any>>(`${baseURL}/threads`, { params: { noOfThreads: value.size,
			noOfTasks: value.tasks,
			serviceTime: value.serviceTime,
			waitTime: value.waitTime
		 }, withCredentials: true });
	}
}
