import { TestBed } from '@angular/core/testing';

import { BucketService } from './bucket.service';
import { AppTestModule } from '../app-test.module';

describe('BucketService', () => {
  let service: BucketService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults()],
    });
    service = TestBed.inject(BucketService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
