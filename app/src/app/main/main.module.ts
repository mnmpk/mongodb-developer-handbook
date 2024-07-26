import { NgModule } from '@angular/core';
import { WorkloadsComponent } from './workloads/workloads.component';
import { SharedModule } from '../shared/shared.module';



@NgModule({
  declarations: [WorkloadsComponent],
  imports: [
    SharedModule
  ]
})
export class MainModule { }
