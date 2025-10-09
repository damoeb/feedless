import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ConnectAppPage } from './connect-app.page';
import { ApolloMockController, AppTestModule, mockServerSettings } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('ConnectAppPage', () => {
  let component: ConnectAppPage;
  let fixture: ComponentFixture<ConnectAppPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [ConnectAppPage, AppTestModule.withDefaults(), RouterTestingModule.withRoutes([])],
    }).compileComponents();

    fixture = TestBed.createComponent(ConnectAppPage);
    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient)
    );
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
