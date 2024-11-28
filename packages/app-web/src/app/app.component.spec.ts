import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { TestBed, waitForAsync } from '@angular/core/testing';

import { RouterTestingModule } from '@angular/router/testing';

import { AppComponent } from './app.component';
import { AppTestModule } from './app-test.module';
import {
  GqlSessionQuery,
  GqlSessionQueryVariables,
  Session,
} from '../generated/graphql';

describe('AppComponent', () => {
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
    declarations: [AppComponent],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    imports: [
        RouterTestingModule.withRoutes([]),
        AppTestModule.withDefaults({
            configurer: (apolloMockController) => apolloMockController
                .mockQuery<GqlSessionQuery, GqlSessionQueryVariables>(Session)
                .and.resolveOnce(async () => {
                return {
                    data: {
                        session: {} as any,
                    },
                };
            }),
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
