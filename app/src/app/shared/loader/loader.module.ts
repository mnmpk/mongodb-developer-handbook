import { NgModule } from '@angular/core';
import { LoaderComponent } from './loader.component';
import { SharedModule } from '../shared.module';

@NgModule({
	declarations: [LoaderComponent],
	imports: [
		SharedModule
	],
	exports: [
		LoaderComponent
	]
})
export class LoaderModule { }