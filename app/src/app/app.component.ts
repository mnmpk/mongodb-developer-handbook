import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SharedModule } from './shared/shared.module';
import { MainModule } from './main/main.module';
import { MetricsModule } from './metrics/metrics.module';
import { Implementation } from './shared/models/workload';
import { UtilityService } from './shared/utility.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, SharedModule, MainModule, MetricsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  UtilityService = UtilityService;
  Implementation = Implementation;
  title = 'app';
}
