import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { NotificationsPage } from './notifications.page';
import { NotificationsPageModule } from './notifications.module';
import { AppTestModule, mockSearchArticles } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('NotificationsPage', () => {
  let component: NotificationsPage;
  let fixture: ComponentFixture<NotificationsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        NotificationsPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockSearchArticles(apolloMockController);
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NotificationsPage);
    component = fixture.componentInstance;
    const clock = jasmine.clock().install();
    fixture.detectChanges();
    clock.tick(15000);
    clock.uninstall();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
