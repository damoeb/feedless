import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { DiscountTagComponent } from './discount-tag.component';
import { DiscountTagModule } from './discount-tag.module';
import {
  ApolloMockController,
  AppTestModule,
  mockLicense,
  mockServerSettings,
} from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('DiscountTagComponent', () => {
  let component: DiscountTagComponent;
  let fixture: ComponentFixture<DiscountTagComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        DiscountTagModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockLicense(apolloMockController);
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DiscountTagComponent);
    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
