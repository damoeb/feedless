import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PluginsPage } from './plugins.page';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '../../app-test.module';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('PluginsPage', () => {
  let component: PluginsPage;
  let fixture: ComponentFixture<PluginsPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PluginsPage, AppTestModule.withDefaults()],
      providers: [],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(PluginsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
