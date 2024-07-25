import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WorkloadsComponent } from './workloads.component';

describe('WorkloadsComponent', () => {
  let component: WorkloadsComponent;
  let fixture: ComponentFixture<WorkloadsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WorkloadsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(WorkloadsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
