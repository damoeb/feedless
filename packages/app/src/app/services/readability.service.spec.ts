import { TestBed } from '@angular/core/testing';

import { ReadabilityService } from './readability.service';

describe('ReadabilityService', () => {
  let service: ReadabilityService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ReadabilityService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
