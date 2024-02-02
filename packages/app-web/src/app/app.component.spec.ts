import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { TestBed, waitForAsync } from '@angular/core/testing';

import { RouterTestingModule } from '@angular/router/testing';

import { AppComponent } from './app.component';
import { AppTestModule } from './app-test.module';
import {
  GqlProfileQuery,
  GqlProfileQueryVariables,
  Profile as ProfileQuery,
} from '../generated/graphql';

describe('AppComponent', () => {
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [AppComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      imports: [
        RouterTestingModule.withRoutes([]),
        AppTestModule.withDefaults((apolloMockController) => {
          apolloMockController
            .mockQuery<GqlProfileQuery, GqlProfileQueryVariables>(ProfileQuery)
            .and.resolveOnce(async () => {
              return {
                data: {
                  profile: {} as any,
                },
              };
            });
        }),
      ],
    }).compileComponents();
  }));

  it('should create the app', waitForAsync(() => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  }));
});
