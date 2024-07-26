import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Conventer, Implementation, OperationType, WorkloadType, WriteConcern } from '../../shared/models/workload';
import { WorkloadsService } from '../workloads.service';
import { UtilityService } from '../../shared/utilityService.service';
import { Stat } from '../../shared/models/stats';
import { MetricsService } from '../../metrics/metrics.service';

@Component({
  selector: 'app-workloads',
  templateUrl: './workloads.component.html',
  styleUrl: './workloads.component.scss'
})
export class WorkloadsComponent {
  Implementation = Implementation;
  WorkloadType = WorkloadType;
  OperationType = OperationType;
  Conventer = Conventer;
  WriteConcern = WriteConcern;
  form!: FormGroup;
  loading = false;

  constructor(private formBuilder: FormBuilder, private ultityService: UtilityService, private service: WorkloadsService, private metricsService: MetricsService) {

  }


  ngOnInit(): void {
    this.form = this.formBuilder.group({
      impl: [Implementation.DRIVER, Validators.required],
      type: [WorkloadType.WRITE, Validators.required],
      entity: ["users"],
      coll: ["test", Validators.required],
      schema: [],
      converter: [Conventer.MONGODB],
      opType: [OperationType.INSERT, Validators.required],
      numWorkers: [1, [Validators.required, Validators.min(1), Validators.max(100)]],
      qty: [1000, Validators.required],
      w: [WriteConcern.MAJORITY],
      bulk: [false, Validators.required]
    });
  }

  save() {
    if (this.form.valid) {
      this.form.enable();
      let formValue = this.form.value;
      this.form.disable();
      formValue.impl = this.ultityService.enumValueToKey(Implementation, formValue.impl);
      formValue.type = this.ultityService.enumValueToKey(WorkloadType, formValue.type);
      formValue.converter = this.ultityService.enumValueToKey(Conventer, formValue.converter);
      formValue.opType = this.ultityService.enumValueToKey(OperationType, formValue.opType);
      formValue.w = this.ultityService.enumValueToKey(WriteConcern, formValue.w);
      this.loading = true;
      this.service.load(formValue).subscribe({
        next: (stat: Stat<any>) => {
          this.form.enable();
          this.loading = false;
          this.metricsService.addResult(stat);
        },
        error: (err: any) => {
          alert(err.message);
          this.form.enable();
          this.loading = false;
        }
      });
    } else {
      alert("The form is invalid.");
    }
    this.form.markAllAsTouched();
  }
}
