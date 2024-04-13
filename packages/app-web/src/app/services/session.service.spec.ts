import { TestBed } from '@angular/core/testing';
import { AppTestModule } from '../app-test.module';
import { SessionService } from './session.service';

describe('ProfileService', () => {
  let service: SessionService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults()],
    });
    service = TestBed.inject(SessionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
