import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { TermsPage } from './terms.page';
import { TermsPageModule } from './terms.module';
import { AppTestModule } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('TermsPage', () => {
  let component: TermsPage;
  let fixture: ComponentFixture<TermsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        TermsPageModule,
        AppTestModule.withDefaults(),
        RouterTestingModule.withRoutes([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TermsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
