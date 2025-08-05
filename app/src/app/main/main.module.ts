import { NgModule } from '@angular/core';
import { WorkloadsComponent } from './workloads/workloads.component';
import { ChangeStreamComponent } from './change-stream/change-stream.component';
import { SharedModule } from '../shared/shared.module';
import { DashboardComponent } from './dashboard/dashboard.component';
import { MetricsModule } from '../metrics/metrics.module';
import { GeoSpatialComponent } from './geo-spatial/geo-spatial.component';
import { CacheComponent } from './cache/cache.component';
import { SessionComponent } from './session/session.component';
import { ThreadComponent } from './thread/thread.component';



@NgModule({
  declarations: [DashboardComponent, WorkloadsComponent, ChangeStreamComponent, GeoSpatialComponent, CacheComponent, SessionComponent, ThreadComponent],
  imports: [
    SharedModule, MetricsModule
  ]
})
export class MainModule { }
