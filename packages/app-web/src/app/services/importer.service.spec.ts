import { TestBed } from '@angular/core/testing';
import { AppTestModule } from '../app-test.module';
import { ImporterService } from './importer.service';

describe('ImporterService', () => {
  let service: ImporterService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults()],
    });
    service = TestBed.inject(ImporterService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
