import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { SubscriptionsPage } from './subscriptions.page';
import { AppTestModule } from '../../../app-test.module';
import { VisualDiffPageModule } from '../visual-diff.module';

describe('SubscriptionsPage', () => {
  let component: SubscriptionsPage;
  let fixture: ComponentFixture<SubscriptionsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [SubscriptionsPage],
      imports: [VisualDiffPageModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(SubscriptionsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
