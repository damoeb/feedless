import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RepositoryModalComponent } from './repository-modal.component';
import {
  ApolloMockController,
  AppTestModule,
  mocks,
  mockServerSettings,
} from '../../app-test.module';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('RepositoryModalComponent', () => {
  let component: RepositoryModalComponent;
  let fixture: ComponentFixture<RepositoryModalComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [AppTestModule.withDefaults(), RepositoryModalComponent],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(RepositoryModalComponent);
    component = fixture.componentInstance;
    component.repository = mocks.repository;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
