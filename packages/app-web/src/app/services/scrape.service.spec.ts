import { TestBed } from '@angular/core/testing';
import { AppTestModule } from '../app-test.module';
import { ScrapeService } from './scrape.service';
import { IonicModule } from '@ionic/angular';

describe('ScrapeService', () => {
  let service: ScrapeService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults(), IonicModule.forRoot()]
    });
    service = TestBed.inject(ScrapeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
