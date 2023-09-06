import { TestBed } from '@angular/core/testing';
import { AppTestModule } from '../app-test.module';
import { WizardService } from './wizard.service';
import { IonicModule } from '@ionic/angular';

describe('WizardService', () => {
  let service: WizardService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults(), IonicModule.forRoot()],
    });
    service = TestBed.inject(WizardService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
