import { NgModule } from '@angular/core';
import { WorkloadsComponent } from './workloads/workloads.component';
import { ChangeStreamComponent } from './change-stream/change-stream.component';
import { SharedModule } from '../shared/shared.module';
import { DashboardComponent } from './dashboard/dashboard.component';



@NgModule({
  declarations: [DashboardComponent, WorkloadsComponent, ChangeStreamComponent],
  imports: [
    SharedModule
  ]
})
export class MainModule { }
