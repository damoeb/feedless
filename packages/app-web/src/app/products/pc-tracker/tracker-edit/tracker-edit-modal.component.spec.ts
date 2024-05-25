import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { TrackerEditModalComponent } from './tracker-edit-modal.component';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '../../../app-test.module';
import { TrackerEditModalModule } from './tracker-edit-modal.module';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { ApolloClient } from '@apollo/client/core';

describe('TrackerEditModalComponent', () => {
  let component: TrackerEditModalComponent;
  let fixture: ComponentFixture<TrackerEditModalComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [TrackerEditModalModule, AppTestModule.withDefaults()],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerSettingsService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(TrackerEditModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
