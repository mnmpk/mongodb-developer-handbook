import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { Page } from '../shared/models/page';
import { Stat } from '../shared/models/stats';
import { Workload } from '../shared/models/workload';

const baseURL = environment.apiUrl + '/workloads';
@Injectable({
	providedIn: 'root'
})
export class WorkloadsService {

	constructor(private httpClient: HttpClient) { }

	list(page = 0, size = environment.defaultPageSize, sort?: string, direction?: string): Observable<Stat<Page<any>>> {
		let pageable: any = {
			page: page,
			size: size
		};
		if (sort && direction) {
			pageable.sort = sort + "," + direction
		}
		return this.httpClient.get<Stat<Page<any>>>(baseURL, { params: pageable });
	}
	find(id: string): Observable<Stat<any>> {
		return this.httpClient.get<Stat<any>>(`${baseURL}/${id}`);
	}

	insert(workload: Workload): Observable<Stat<any>> {
		return this.httpClient.post<Stat<any>>(`${baseURL}/insert`, workload);
	}
	update(workload: Workload): Observable<Stat<any>> {
		return this.httpClient.post<Stat<any>>(`${baseURL}/update`, workload);
	}
	delete(workload: Workload): Observable<Stat<any>> {
		return this.httpClient.post<Stat<any>>(`${baseURL}/delete`, workload);
	}
	replace(workload: Workload): Observable<Stat<any>> {
		return this.httpClient.post<Stat<any>>(`${baseURL}/replace`, workload);
	}
}
