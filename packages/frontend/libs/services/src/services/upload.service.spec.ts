import { TestBed } from '@angular/core/testing';
import { UploadService } from './upload.service';
import { AppTestModule } from '@feedless/test';

describe('UploadService', () => {
  let service: UploadService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults()],
    });
    service = TestBed.inject(UploadService);
  });

  it('is defined', () => {
    expect(service).toBeDefined();
  });
});
