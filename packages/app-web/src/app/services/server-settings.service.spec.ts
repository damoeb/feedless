import { TestBed } from '@angular/core/testing';
import { AppTestModule } from '../app-test.module';
import { ServerSettingsService } from './server-settings.service';

describe('ServerSettingsService', () => {
  let service: ServerSettingsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults()]
    });
    service = TestBed.inject(ServerSettingsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
