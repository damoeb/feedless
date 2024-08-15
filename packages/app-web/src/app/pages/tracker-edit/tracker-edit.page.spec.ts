import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { TrackerEditPage } from './tracker-edit.page';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '../../app-test.module';
import { TrackerEditPageModule } from './tracker-edit.module';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('SubscriptionEditPage', () => {
  let component: TrackerEditPage;
  let fixture: ComponentFixture<TrackerEditPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [TrackerEditPageModule, AppTestModule.withDefaults()],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(TrackerEditPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
