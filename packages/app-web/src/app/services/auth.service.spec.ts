import { TestBed } from '@angular/core/testing';
import { AppTestModule } from '../app-test.module';
import { AuthService } from './auth.service';
import { IonicModule } from '@ionic/angular';

describe('AuthService', () => {
  let service: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults(), IonicModule.forRoot()]
    });
    service = TestBed.inject(AuthService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
