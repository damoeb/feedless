import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ChangeTrackerProductPage } from './change-tracker-product.page';
import {
  ApolloMockController,
  AppTestModule,
  mockScrape,
  mockServerSettings,
} from '../../app-test.module';
import { ChangeTrackerProductModule } from './change-tracker-product.module';
import { RouterTestingModule } from '@angular/router/testing';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('ChangeTrackerProductPage', () => {
  let component: ChangeTrackerProductPage;
  let fixture: ComponentFixture<ChangeTrackerProductPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ChangeTrackerProductModule,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) =>
            mockScrape(apolloMockController),
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(ChangeTrackerProductPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
