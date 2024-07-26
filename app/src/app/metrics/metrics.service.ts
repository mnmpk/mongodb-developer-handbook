import { EventEmitter, Injectable } from '@angular/core';
import { Stat } from '../shared/models/stats';

@Injectable({
  providedIn: 'root'
})
export class MetricsService {
	public static update$ = new EventEmitter<Stat<any>>();

  private static histories: Stat<any>[] = [];

  constructor() { }

  addResult(stat: Stat<any>){
    MetricsService.histories.push(stat);
    MetricsService.update$.emit(stat);
  }
  clearResult(){
    MetricsService.histories=[];
  }
  getResults(){
    return MetricsService.histories;
  }
}
