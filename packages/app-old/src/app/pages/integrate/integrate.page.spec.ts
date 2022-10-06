import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { IntegratePage } from './integrate.page';
import { IntegratePageModule } from './integrate.module';
import { RouterTestingModule } from '@angular/router/testing';
import { ApolloTestingModule } from 'apollo-angular/testing';

describe('IntegratePage', () => {
  let component: IntegratePage;
  let fixture: ComponentFixture<IntegratePage>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [
          IntegratePageModule,
          RouterTestingModule,
          ApolloTestingModule,
        ],
      }).compileComponents();

      fixture = TestBed.createComponent(IntegratePage);
      component = fixture.componentInstance;
      component.article = {} as any;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
