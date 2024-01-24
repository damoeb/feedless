import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { TermsComponent } from './terms.component';
import { TermsModule } from './terms.module';
import { AppTestModule } from '../../app-test.module';

describe('TermsComponent', () => {
  let component: TermsComponent;
  let fixture: ComponentFixture<TermsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [TermsModule, AppTestModule.withDefaults()]
    }).compileComponents();

    fixture = TestBed.createComponent(TermsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
