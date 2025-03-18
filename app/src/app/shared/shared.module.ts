import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MaterialModule } from './material/material.module';
import { ReactiveFormsModule } from '@angular/forms';
import { NgxEchartsModule } from 'ngx-echarts';
import { GoogleMapsModule } from '@angular/google-maps';



@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MaterialModule,
    NgxEchartsModule.forRoot({
      echarts: () => import('echarts')
    }),
    GoogleMapsModule
  ],
  exports: [
    CommonModule,
    ReactiveFormsModule,
    MaterialModule,
    NgxEchartsModule,
    GoogleMapsModule
  ]
})
export class SharedModule { }
