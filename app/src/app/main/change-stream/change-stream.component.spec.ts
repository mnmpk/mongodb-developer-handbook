import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChangeStreamComponent } from './change-stream.component';

describe('ChangeStreamComponent', () => {
  let component: ChangeStreamComponent;
  let fixture: ComponentFixture<ChangeStreamComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChangeStreamComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ChangeStreamComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
