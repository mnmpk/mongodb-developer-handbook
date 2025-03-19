import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { Page } from '../../shared/models/page';
import { Stat } from '../../shared/models/stats';
import { Workload } from '../../shared/models/workload';

const baseURL = environment.japiUrl;
@Injectable({
	providedIn: 'root'
})
export class GeoSpatialService {

	constructor(private httpClient: HttpClient) { }
	findStopsNearby(latLng: {
		lat: number
		lng: number
	}) {
		return this.httpClient.get<any>(`${baseURL}/geo-spatial/stops`, { params: latLng });
	}
	findRoutesNearby(latLng: {
		lat: number
		lng: number
	}) {
		return this.httpClient.get<any>(`${baseURL}/geo-spatial/routes`, { params: latLng });
	}
	search(data:{start:number[],end:number[]}){
		return this.httpClient.post<any>(`${baseURL}/geo-spatial/routes`, data);
	}
}
