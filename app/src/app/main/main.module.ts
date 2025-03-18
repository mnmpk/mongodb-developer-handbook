import { NgModule } from '@angular/core';
import { WorkloadsComponent } from './workloads/workloads.component';
import { ChangeStreamComponent } from './change-stream/change-stream.component';
import { SharedModule } from '../shared/shared.module';
import { DashboardComponent } from './dashboard/dashboard.component';
import { MetricsModule } from '../metrics/metrics.module';
import { GeoSpatialComponent } from './geo-spatial/geo-spatial.component';



@NgModule({
  declarations: [DashboardComponent, WorkloadsComponent, ChangeStreamComponent, GeoSpatialComponent],
  imports: [
    SharedModule, MetricsModule
  ]
})
export class MainModule { }
