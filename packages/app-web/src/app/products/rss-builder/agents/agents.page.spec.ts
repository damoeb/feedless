import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { AgentsPage } from './agents.page';
import { PlansPageModule } from './agents.module';
import { AppTestModule, mockPlans } from '../../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('AgentsPage', () => {
  let component: AgentsPage;
  let fixture: ComponentFixture<AgentsPage>;

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

    fixture = TestBed.createComponent(AgentsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
