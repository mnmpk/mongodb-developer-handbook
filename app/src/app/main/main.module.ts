import { NgModule } from '@angular/core';
import { WorkloadsComponent } from './workloads/workloads.component';
import { ChangeStreamComponent } from './change-stream/change-stream.component';
import { SharedModule } from '../shared/shared.module';
import { DashboardComponent } from './dashboard/dashboard.component';
import { MetricsModule } from '../metrics/metrics.module';



@NgModule({
  declarations: [DashboardComponent, WorkloadsComponent, ChangeStreamComponent],
  imports: [
    SharedModule, MetricsModule
  ]
})
export class MainModule { }
