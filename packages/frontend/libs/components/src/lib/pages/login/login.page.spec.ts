import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoginPage } from './login.page';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '@feedless/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';
import { environment } from '@feedless/core';
import { GqlVertical } from '@feedless/graphql-api';

// isDevMode() reads this global; production builds compile it to false
const angularGlobals = globalThis as { ngDevMode?: unknown };

describe('LoginPage', () => {
  let component: LoginPage;
  let fixture: ComponentFixture<LoginPage>;

  async function createPage() {
    await TestBed.configureTestingModule({
      imports: [
        LoginPage,
        AppTestModule.withDefaults(),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginPage);
    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ApolloClient),
    );
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  const initialDevMode = angularGlobals.ngDevMode;
  const initialProduct = environment.product;

  afterEach(() => {
    angularGlobals.ngDevMode = initialDevMode;
    environment.product = initialProduct;
  });

  it('should create', async () => {
    await createPage();

    expect(component).toBeTruthy();
  });

  it('logs in with the oauth registration of the active product outside dev mode', async () => {
    angularGlobals.ngDevMode = false;
    environment.product = GqlVertical.Upcoming;

    await createPage();

    const apiUrl = TestBed.inject(ServerConfigService).apiUrl;
    expect(component.loginUrl).toBe(`${apiUrl}/oauth2/authorization/upcoming`);
  });
});
