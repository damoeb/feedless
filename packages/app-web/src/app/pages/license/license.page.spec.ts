import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LicensePage } from './license.page';
import { ApolloMockController, AppTestModule, mockServerSettings } from '../../app-test.module';
import { RouterTestingModule } from '@angular/router/testing';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('LicencePage', () => {
  let component: LicensePage;
  let fixture: ComponentFixture<LicensePage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LicensePage, AppTestModule.withDefaults(), RouterTestingModule.withRoutes([])],
    }).compileComponents();

    fixture = TestBed.createComponent(LicensePage);
    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient)
    );
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
