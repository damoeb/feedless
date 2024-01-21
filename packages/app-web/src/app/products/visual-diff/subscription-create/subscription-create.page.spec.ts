import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { SubscriptionCreatePage } from './subscription-create.page';
import { AppTestModule } from '../../../app-test.module';
import { VisualDiffPageModule } from '../visual-diff.module';

describe('VisualDiffPage', () => {
  let component: SubscriptionCreatePage;
  let fixture: ComponentFixture<SubscriptionCreatePage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [SubscriptionCreatePage],
      imports: [VisualDiffPageModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(SubscriptionCreatePage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
