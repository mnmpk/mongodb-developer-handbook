import { Component, EventEmitter, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Converter, Implementation, OperationType, Workload, WorkloadType, WriteConcern } from '../../shared/models/workload';
import { WorkloadsService } from '../workloads.service';
import { UtilityService } from '../../shared/utility.service';
import { Stat } from '../../shared/models/stats';
import { MetricsService } from '../../metrics/metrics.service';
import { SelectionModel } from '@angular/cdk/collections';
import { MatTableDataSource } from '@angular/material/table';
import { Page } from '../../shared/models/page';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { map, merge, startWith, switchMap } from 'rxjs';

@Component({
  selector: 'app-workloads',
  templateUrl: './workloads.component.html',
  styleUrl: './workloads.component.scss'
})
export class WorkloadsComponent {
  UtilityService = UtilityService;
  Implementation = Implementation;
  WorkloadType = WorkloadType;
  OperationType = OperationType;
  Converter = Converter;
  WriteConcern = WriteConcern;
  form!: FormGroup;


  update$: EventEmitter<any> = new EventEmitter();
  columns: string[] = [];
  dataSource: MatTableDataSource<any> = new MatTableDataSource();
  selection = new SelectionModel<any>(true, []);
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  stat!: Stat<Page<any>>;
  page!: Page<any>;

  loading = false;
  searchValue: string = "";

  constructor(private formBuilder: FormBuilder, private utilityService: UtilityService, private service: WorkloadsService, private metricsService: MetricsService) {

  }


  ngOnInit(): void {
    this.form = this.formBuilder.group({
      type: [WorkloadType.WRITE, Validators.required],
      entity: ["users"],
      coll: ["test", Validators.required],
      schema: [],
      converter: [Converter.MONGODB],
      opType: [OperationType.INSERT, Validators.required],
      numWorkers: [1, [Validators.required, Validators.min(1), Validators.max(100)]],
      qty: [1000, Validators.required],
      w: [WriteConcern.MAJORITY],
      bulk: [false, Validators.required]
    });
  }

  ngAfterViewInit() {
    this.sort.sortChange.subscribe(() => (this.paginator.pageIndex = 0));
    merge(this.update$, this.sort.sortChange, this.paginator.page).pipe(
      startWith({}),
      switchMap(() => {
        let formValue = {...this.getFormValue(WorkloadType.READ), qty:this.paginator.pageSize};
        return this.service.list(formValue, this.paginator.pageIndex, this.paginator.pageSize, this.sort.active, this.sort.direction)
      }),
      map((stat: Stat<Page<any>>) => {
        this.metricsService.addResult(stat);
        this.stat = stat;
        this.columns = this.stat.fields;
        this.dataSource = new MatTableDataSource(stat.data[0].content);
        this.page = stat.data[0];
      }),
    ).subscribe();
  }

  search(event: Event) {

  }
  getFields() {
    return this.stat?.fields;
  }

  getFormValue(type: WorkloadType) {
    let formValue = {...this.form.value};
    formValue.impl = this.utilityService.enumValueToKey(Implementation, UtilityService.implementation);
    formValue.type = this.utilityService.enumValueToKey(WorkloadType, type);
    formValue.converter = this.utilityService.enumValueToKey(Converter, formValue.converter);
    formValue.opType = this.utilityService.enumValueToKey(OperationType, formValue.opType);
    formValue.w = this.utilityService.enumValueToKey(WriteConcern, formValue.w);
    if(this.page){
      formValue.ids = this.page.content.map(i=>i.id);
      if(formValue.opType!=this.utilityService.enumValueToKey(OperationType, OperationType.INSERT)) formValue.qty = this.page.page.size;
    }
    return formValue;
  }

  submit() {
    if (this.form.valid) {
      let formValue = this.getFormValue(WorkloadType.WRITE);
      this.loading = true;
      this.service.load(formValue).subscribe({
        next: (stat: Stat<any>) => {
          this.form.enable();
          this.loading = false;
          this.metricsService.addResult(stat);
          this.update$.emit();
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

  dropColumn(event: CdkDragDrop<string[]>) {
    moveItemInArray(this.columns, event.previousIndex, event.currentIndex);
  }

  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.length;
    return numSelected === numRows;
  }

  toggleAllRows() {
    if (this.isAllSelected()) {
      this.selection.clear();
      return;
    }

    this.selection.select(...this.dataSource.data);
  }
  checkboxLabel(row?: any): string {
    if (!row) {
      return `${this.isAllSelected() ? 'deselect' : 'select'} all`;
    }
    return `${this.selection.isSelected(row) ? 'deselect' : 'select'} row ${row.id}`;
  }

  applyFilter(event: Event) {
    this.searchValue = (event.target as HTMLInputElement).value;
    if (this.paginator.pageIndex == 0) {
      this.update$.emit();
    } else {
      this.paginator.firstPage();
    }
  }

}
