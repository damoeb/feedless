import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { SecretsPage } from './secrets.page';
import { SecretsPageModule } from './secrets.module';
import { AppTestModule } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('SecretsPage', () => {
  let component: SecretsPage;
  let fixture: ComponentFixture<SecretsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        SecretsPageModule,
        AppTestModule.withDefaults(),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SecretsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
