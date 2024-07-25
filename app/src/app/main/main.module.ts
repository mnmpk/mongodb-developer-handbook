import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WorkloadsComponent } from './workloads/workloads.component';
import { SharedModule } from '../shared/shared.module';



@NgModule({
  declarations: [WorkloadsComponent],
  imports: [
    CommonModule, 
    SharedModule
  ]
})
export class MainModule { }
