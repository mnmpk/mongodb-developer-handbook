import { HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ProgressSpinnerMode } from '@angular/material/progress-spinner';
import { BehaviorSubject, Observable, Subscription, tap, timer } from 'rxjs';

@Injectable({
	providedIn: 'root'
})
export class LoaderService {
	public tasks: HttpRequest<any>[] = [];
	public isLoading = new BehaviorSubject<number>(100);

	public message: string="";
	public mode: ProgressSpinnerMode = "determinate";
	public progress: number = 100;
	maxNoOfTask: number = 0;

	constructor() { }

	load(http: HttpRequest<any>, message: string) {
		this.tasks.push(http);
		this.maxNoOfTask++;
		this.message = message;
		this.mode = this.maxNoOfTask > 1 ? "determinate" : "indeterminate";
		console.log(this.mode);
		this.progress = this.getProgress();
		this.isLoading.next(this.progress);
	}
	finish(http: HttpRequest<any>) {
		const i = this.tasks.indexOf(http);
		if (i >= 0) {
			this.tasks.splice(i, 1);
		}
		this.progress = this.getProgress();
		this.mode = "determinate";
		if (this.progress >= 100){
			this.maxNoOfTask = 0;
		}
		this.isLoading.next(this.progress);
	}
	finishAll() {
		this.tasks.splice(0, this.tasks.length);
		this.maxNoOfTask = 0;
		this.progress = 100;
		this.isLoading.next(this.progress);
	}
	startProgress(noOfTasks:number, taskTimeRequired:number, callback?:Function){
		const interval = 100;
		const t = timer(0, interval);
		return t.subscribe(val => {
		  this.mode = "determinate";
		  this.progress = (((val * interval) / taskTimeRequired) / (noOfTasks)) * 100;
		  if(callback)
		  	callback(val * interval);
		});
	}
	stopProgress(t: Subscription, callback?:Function) {
	  t.unsubscribe();
	  if(callback)
		  callback();
	}
	getProgress() { if(!this.maxNoOfTask && !this.tasks.length) return 100;return (this.maxNoOfTask - this.tasks.length) / this.maxNoOfTask * 100; }
}
