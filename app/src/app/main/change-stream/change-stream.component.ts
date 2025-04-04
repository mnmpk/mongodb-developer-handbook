import { Component, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatTableDataSource } from '@angular/material/table';
import { merge, startWith, switchMap, map } from 'rxjs';
import { Page } from '../../shared/models/page';
import { Stat } from '../../shared/models/stats';
import { WorkloadType } from '../../shared/models/workload';
import { ChangeStreamService } from './change-stream.service';

@Component({
    selector: 'app-change-stream',
    templateUrl: './change-stream.component.html',
    styleUrl: './change-stream.component.scss',
    standalone: false
})
export class ChangeStreamComponent {
  update$: EventEmitter<any> = new EventEmitter();
  columns: string[] = ['collection', 'mode', 'pipeline', 'actions'];
  dataSource: MatTableDataSource<any> = new MatTableDataSource();
  form!: FormGroup;

  constructor(private formBuilder: FormBuilder, private service: ChangeStreamService) { }

  ngOnInit() {
    this.form = this.formBuilder.group({
      collection: [null, Validators.required],
      mode: [null, Validators.required],
      pipeline: [null, Validators.required],
    });
  }
  ngAfterViewInit() {
    merge(this.update$).pipe(
      startWith({}),
      switchMap(() => {
        return this.service.list();
      }),
      map((res: any) => {
        this.dataSource = new MatTableDataSource(res);
      }),
    ).subscribe();
  }
  submit() {
    if (this.form.valid) {
      let formValue = this.form.value;
      this.service.watch(formValue).subscribe({
        next: (stat: Stat<any>) => {
          this.form.enable();
          this.update$.emit();
        },
        error: (err: any) => {
          alert(err.message);
          this.form.enable();
        }
      });
    } else {
      alert("The form is invalid.");
    }
    this.form.markAllAsTouched();
  }
  unwatch(changeStream:any){
    this.service.watch(changeStream).subscribe({
      next: (stat: Stat<any>) => {
        this.form.enable();
        this.update$.emit();
      },
      error: (err: any) => {
        alert(err.message);
        this.form.enable();
      }
    });
  }
}
