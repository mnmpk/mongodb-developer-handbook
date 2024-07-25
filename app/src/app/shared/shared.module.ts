import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FlexLayoutModule } from '@angular/flex-layout';
import { MaterialModule } from './material/material.module';
import { ReactiveFormsModule } from '@angular/forms';



@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    FlexLayoutModule,
    ReactiveFormsModule, 
    MaterialModule
  ],
  exports:[FlexLayoutModule, ReactiveFormsModule, MaterialModule]
})
export class SharedModule { }
