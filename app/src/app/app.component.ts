import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MaterialModule } from './shared/material/material.module';
import { FlexLayoutModule } from '@angular/flex-layout';
import { MainModule } from './main/main.module';
import { MetricsComponent } from './metrics/metrics/metrics.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, FlexLayoutModule, MaterialModule, MainModule, MetricsComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'app';
}
