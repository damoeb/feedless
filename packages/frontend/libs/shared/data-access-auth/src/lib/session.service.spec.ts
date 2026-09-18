import { TestBed } from '@angular/core/testing';
import { AppTestModule } from '@feedless/testing';
import { SessionService } from './session.service';

describe('SessionService', () => {
  let service: SessionService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults()],
    });
    service = TestBed.inject(SessionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
