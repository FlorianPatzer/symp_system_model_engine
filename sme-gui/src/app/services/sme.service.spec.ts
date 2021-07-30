import { TestBed } from '@angular/core/testing';

import { SmeService } from './sme.service';

describe('SmeService', () => {
  let service: SmeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SmeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
