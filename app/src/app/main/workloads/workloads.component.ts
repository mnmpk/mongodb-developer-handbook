import { Component } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Coventer, Implementation, OperationType, WorkloadType, WriteConcern } from '../../shared/models/workload';
import { WorkloadsService } from '../workloads.service';
import { UtilityService } from '../../shared/utilityService.service';
import { Stat } from '../../shared/models/stats';

@Component({
  selector: 'app-workloads',
  templateUrl: './workloads.component.html',
  styleUrl: './workloads.component.scss'
})
export class WorkloadsComponent {
  Implementation = Implementation;
  WorkloadType = WorkloadType;
  OperationType = OperationType;
  Coventer = Coventer;
  WriteConcern = WriteConcern;
  form!: FormGroup;

  constructor(private formBuilder: FormBuilder, private ultityService: UtilityService, private service: WorkloadsService) {

  }


  ngOnInit(): void {
    this.form = this.formBuilder.group({
      impl: [Implementation.DRIVER],
      type: [WorkloadType.WRITE],
      coll: ["test"],
      schema: [],
      converter: [Coventer.SPRING],
      opType: [OperationType.INSERT],
      numWorkers: [1],
      qty: [10000],
      w: [WriteConcern.majority],
      bulk: [true]
    });
  }

  save() {
    if (this.form.valid) {
      this.form.enable();
      let formValue = this.form.value;
      this.form.disable();
      formValue.impl = this.ultityService.enumValueToKey(Implementation, formValue.impl);
      formValue.type = this.ultityService.enumValueToKey(WorkloadType, formValue.type);
      formValue.converter = this.ultityService.enumValueToKey(Coventer, formValue.converter);
      formValue.opType = this.ultityService.enumValueToKey(OperationType, formValue.opType);
      formValue.w = this.ultityService.enumValueToKey(WriteConcern, formValue.w);
      this.service.insert(formValue).subscribe({
        next: (event: Stat<any>) => {

        },
        error: (err: any) => {
        }
      });
    } else {
      alert("The form is invalid.");
    }
    this.form.markAllAsTouched();
  }
}
