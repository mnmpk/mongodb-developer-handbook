import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { Page } from '../shared/models/page';
import { Stat } from '../shared/models/stats';
import { Workload } from '../shared/models/workload';

const baseURL = environment.apiUrl;
@Injectable({
	providedIn: 'root'
})
export class WorkloadsService {

	constructor(private httpClient: HttpClient) { }

	list(workload: Workload, page = 0, size = environment.defaultPageSize, sort?: string, direction?: string): Observable<Stat<Page<any>>> {
		let pageable: any = {
			page: page,
			size: size
		};
		if (sort && direction) {
			pageable.sort = sort + "," + direction
		}
		return this.httpClient.post<Stat<Page<any>>>(`${baseURL}/${workload.entity}/list`, workload, { params: pageable });
	}
	find(id: string): Observable<Stat<any>> {
		return this.httpClient.get<Stat<any>>(`${baseURL}/${id}`);
	}
	create(data: any): Observable<Stat<any>> {
		return this.httpClient.post<Stat<any>>(baseURL, data);
	}

	update(data: any): Observable<Stat<any>> {
		return this.httpClient.put<Stat<any>>(`${baseURL}/${data.id}`, data);
	}

	delete(id: string): Observable<Stat<any>> {
		return this.httpClient.delete<Stat<any>>(`${baseURL}/${id}`);
	}

	load(workload: Workload): Observable<Stat<any>> {
		return this.httpClient.post<Stat<any>>(`${baseURL}/${workload.entity}/load`, workload);
	}
}
