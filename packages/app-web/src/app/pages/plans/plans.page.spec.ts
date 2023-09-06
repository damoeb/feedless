import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { PlansPage } from './plans.page';
import { PlansPageModule } from './plans.module';
import { AppTestModule, mockPlans } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('PlansPage', () => {
  let component: PlansPage;
  let fixture: ComponentFixture<PlansPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        PlansPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockPlans(apolloMockController);
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PlansPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
