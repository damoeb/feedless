import { TestBed } from '@angular/core/testing';
import { AppTestModule } from '../app-test.module';
import { OpmlService } from './opml.service';

describe('OpmlService', () => {
  let service: OpmlService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults()],
    });
    service = TestBed.inject(OpmlService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
