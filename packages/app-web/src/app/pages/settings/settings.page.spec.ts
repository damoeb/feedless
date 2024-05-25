import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { SettingsPage } from './settings.page';
import { SettingsPageModule } from './settings.module';
import { ApolloMockController, AppTestModule, mockLicense, mockServerSettings } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';
import { ServerSettingsService } from '../../services/server-settings.service';
import { ApolloClient } from '@apollo/client/core';

xdescribe('SettingsPage', () => {
  let component: SettingsPage;
  let fixture: ComponentFixture<SettingsPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        SettingsPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockLicense(apolloMockController);
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SettingsPage);
    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerSettingsService),
      TestBed.inject(ApolloClient),
    );
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
