import { TestBed } from '@angular/core/testing';
import { AppTestModule } from '../app-test.module';
import { FeedService } from './feed.service';

describe('FeedService', () => {
  let service: FeedService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults()]
    });
    service = TestBed.inject(FeedService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
