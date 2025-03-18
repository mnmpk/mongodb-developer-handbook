import { TestBed } from '@angular/core/testing';

import { WorkloadsService } from './workloads.service';

describe('WorkloadsService', () => {
  let service: WorkloadsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WorkloadsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
