import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ReportPage } from './report.page';
import { AppTestModule, mockRepositories } from '../../app-test.module';
import { ReportPageModule } from './report.module';

describe('FeedsPage', () => {
  let component: ReportPage;
  let fixture: ComponentFixture<ReportPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        ReportPageModule,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) =>
            mockRepositories(apolloMockController),
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ReportPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
