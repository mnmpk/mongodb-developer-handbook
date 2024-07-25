import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { WorkloadsComponent } from './workloads/workloads.component';
import { MaterialModule } from '../shared/material/material.module';



@NgModule({
  declarations: [WorkloadsComponent],
  imports: [
    CommonModule, 
    MaterialModule,
    ReactiveFormsModule
  ]
})
export class MainModule { }
