import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SharedModule } from './shared/shared.module';
import { MainModule } from './main/main.module';
import { Implementation } from './shared/models/workload';
import { UtilityService } from './shared/utility.service';
import { LoaderModule } from './shared/loader/loader.module';

@Component({
    selector: 'app-root',
    imports: [RouterOutlet, SharedModule, LoaderModule, MainModule],
    templateUrl: './app.component.html',
    styleUrl: './app.component.scss'
})
export class AppComponent {
  UtilityService = UtilityService;
  Implementation = Implementation;
  title = 'app';
}
