import { EventEmitter, Injectable } from '@angular/core';
import { Stat } from '../shared/models/stats';

@Injectable({
  providedIn: 'root'
})
export class MetricsService {
	public static update$ = new EventEmitter<Stat<any>>();
  public static MAX_STATS = 30;

  private static histories: Stat<any>[] = [];

  constructor() { }

  addResult(stat: Stat<any>){
    if(MetricsService.histories.length>MetricsService.MAX_STATS)
      MetricsService.histories.shift();
    MetricsService.histories.push(stat);
    MetricsService.update$.emit(stat);
  }
  clearResult(){
    MetricsService.histories=[];
    MetricsService.update$.emit();
  }
  getResults(){
    return MetricsService.histories;
  }
}
