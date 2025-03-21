import { Component } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { CacheService } from './cache.service';
import { MetricsService } from '../../metrics/metrics.service';
import { Stat } from '../../shared/models/stats';
import { Implementation, Workload, WorkloadType } from '../../shared/models/workload';

@Component({
  selector: 'app-cache',
  templateUrl: './cache.component.html',
  styleUrl: './cache.component.scss',
  standalone: false
})
export class CacheComponent {
  form!: FormGroup;
  loading = false;
  result!:string;

  constructor(private formBuilder: FormBuilder, private service: CacheService, private metricsService: MetricsService) {

  }
  ngOnInit(): void {
    this.form = this.formBuilder.group({
      size: [5120],
    });
  }
  submit() {
    if (this.form.valid) {
      this.loading = true;
      let stat = new Stat();
      stat.startAt = new Date();
      stat.workload = new Workload();
      stat.workload.type = WorkloadType.READ;
      stat.workload.qty = 1;
      stat.workload.numWorkers = 1;
      stat.workload.impl=Implementation.SPRING;
      this.service.apiWithCache(this.form.value.size).subscribe({
        next: (result: any) => {
          stat.endAt = new Date();
          stat.duration = stat.endAt.getTime()- stat.startAt.getTime();
          stat.min = stat.duration;
          stat.avg = stat.duration;
          stat.max = stat.duration;
          stat.ops = stat.workload.qty/stat.duration*1000;
          this.metricsService.addResult(stat);
          this.result = result.value;
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
  clear() {
    if (this.form.valid) {
      this.loading = true;
      this.service.clear(this.form.value.size).subscribe({
        next: (result: any) => {
          this.result = result.value;
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
