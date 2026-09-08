import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FilterItemsAccordionComponent } from './filter-items-accordion.component';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '@feedless/testing';
import { ServerConfigService } from '../../services';
import { ApolloClient } from '@apollo/client/core';

describe('FilterItemsAccordionComponent', () => {
  let component: FilterItemsAccordionComponent;
  let fixture: ComponentFixture<FilterItemsAccordionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults(), FilterItemsAccordionComponent],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(FilterItemsAccordionComponent);
    component = fixture.componentInstance;
    const componentRef = fixture.componentRef;
    componentRef.setInput('labelPrefix', '');
    componentRef.setInput('filterPlugin', null);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
