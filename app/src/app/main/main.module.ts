import { NgModule } from '@angular/core';
import { WorkloadsComponent } from './workloads/workloads.component';
import { ChangeStreamComponent } from './change-stream/change-stream.component';
import { SharedModule } from '../shared/shared.module';



@NgModule({
  declarations: [WorkloadsComponent, ChangeStreamComponent],
  imports: [
    SharedModule
  ]
})
export class MainModule { }
