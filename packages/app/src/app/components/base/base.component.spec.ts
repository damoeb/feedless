import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { BaseComponent } from './base.component';
import { BaseModule } from './base.module';
import { RouterTestingModule } from '@angular/router/testing';
import { ApolloTestingModule } from 'apollo-angular/testing';

describe('BaseComponent', () => {
  let component: BaseComponent;
  let fixture: ComponentFixture<BaseComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [BaseModule, RouterTestingModule, ApolloTestingModule],
      }).compileComponents();

      fixture = TestBed.createComponent(BaseComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
