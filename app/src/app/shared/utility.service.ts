import { EventEmitter, Injectable } from '@angular/core';
import { Implementation } from './models/workload';

@Injectable({
	providedIn: 'root'
})
export class UtilityService {
	public static implementation: Implementation = Implementation.DRIVER;
	constructor() { }

	enumValueToKey(e: any, value: any) {
		return Object.keys(e)[Object.values(e).indexOf(value)];
	}
}
