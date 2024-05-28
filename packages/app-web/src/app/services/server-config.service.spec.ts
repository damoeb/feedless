import { TestBed } from '@angular/core/testing';
import { AppTestModule } from '../app-test.module';
import { ServerConfigService } from './server-config.service';

describe('ServerSettingsService', () => {
  let service: ServerConfigService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults()],
    });
    service = TestBed.inject(ServerConfigService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
