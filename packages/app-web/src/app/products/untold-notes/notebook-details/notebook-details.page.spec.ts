import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { NotebookDetailsPage } from './notebook-details.page';
import { AppTestModule } from '../../../app-test.module';
import { SubscriptionDetailsPageModule } from './notebook-details.module';

describe('SubscriptionDetailsPage', () => {
  let component: NotebookDetailsPage;
  let fixture: ComponentFixture<NotebookDetailsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [SubscriptionDetailsPageModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(NotebookDetailsPage);
    component = fixture.componentInstance;
    component.subscription = {} as any;
    component.documents = [];
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
