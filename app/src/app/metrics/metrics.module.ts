import { NgModule } from '@angular/core';
import { MetricsComponent } from './metrics/metrics.component';
import { SharedModule } from '../shared/shared.module';



@NgModule({
  declarations: [MetricsComponent],
  imports: [
    SharedModule
  ],
  exports: [MetricsComponent]
})
export class MetricsModule { }
