import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { WaitListPage } from './wait-list.page';
import { WaitListPageModule } from './wait-list-page.module';
import { AppTestModule } from '../../app-test.module';

describe('TermsModalComponent', () => {
  let component: WaitListPage;
  let fixture: ComponentFixture<WaitListPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [WaitListPageModule, AppTestModule.withDefaults()]
    }).compileComponents();

    fixture = TestBed.createComponent(WaitListPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
