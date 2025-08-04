import { Component } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ThreadService } from './thread.service';
import { MetricsService } from '../../metrics/metrics.service';

@Component({
  selector: 'app-thread',
  templateUrl: './thread.component.html',
  styleUrl: './thread.component.scss',
  standalone: false
})
export class ThreadComponent {
  form!: FormGroup;
  loading = false;
  result!:string;

  constructor(private formBuilder: FormBuilder, private service: ThreadService, private metricsService: MetricsService) {

  }
  ngOnInit(): void {
    this.form = this.formBuilder.group({
    });
  }
  submit() {
    if (this.form.valid) {
    } else {
      alert("The form is invalid.");
    }
    this.form.markAllAsTouched();
  }
}
