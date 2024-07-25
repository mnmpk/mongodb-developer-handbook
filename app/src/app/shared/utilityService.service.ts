import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root'
})
export class UtilityService {

	constructor() { }

	enumValueToKey(e:any, value:any){
		return Object.keys(e)[Object.values(e).indexOf(value)];
	}
}
