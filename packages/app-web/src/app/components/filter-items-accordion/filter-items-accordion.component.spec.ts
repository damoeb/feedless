import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FilterItemsAccordionComponent } from './filter-items-accordion.component';
import { FilterItemsAccordionModule } from './filter-items-accordion.module';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '../../app-test.module';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('FilterItemsAccordionComponent', () => {
  let component: FilterItemsAccordionComponent;
  let fixture: ComponentFixture<FilterItemsAccordionComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults(), FilterItemsAccordionModule],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(FilterItemsAccordionComponent);
    component = fixture.componentInstance;
    component.filterPlugin = null;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
