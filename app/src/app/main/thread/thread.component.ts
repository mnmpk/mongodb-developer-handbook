import { Component } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ThreadService } from './thread.service';
import { MetricsService } from '../../metrics/metrics.service';
import { Stat } from '../../shared/models/stats';

@Component({
  selector: 'app-thread',
  templateUrl: './thread.component.html',
  styleUrl: './thread.component.scss',
  standalone: false
})
export class ThreadComponent {
  form!: FormGroup;
  loading = false;
  result!: string;

  constructor(private formBuilder: FormBuilder, private service: ThreadService, private metricsService: MetricsService) {

  }
  ngOnInit(): void {
    this.form = this.formBuilder.group({
      size: [10],
      tasks: [10000],
      serviceTime: [5],
      waitTime: [10],
    });
  }
  submit() {
    if (this.form.valid) {
      this.loading = true;
      this.service.run(this.form.value).subscribe({
        next: (stat: Stat<any>) => {
          this.metricsService.addResult(stat);
          this.loading = false;
        },
        error: (err: any) => {
          alert(err.message);
          this.loading = false;
        }
      });
    } else {
      alert("The form is invalid.");
    }
    this.form.markAllAsTouched();
  }
}
