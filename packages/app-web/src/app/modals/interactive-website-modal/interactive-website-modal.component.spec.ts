import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { InteractiveWebsiteModalComponent } from './interactive-website-modal.component';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '../../app-test.module';
import { InteractiveWebsiteModalModule } from './interactive-website-modal.module';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('InteractiveWebsiteModalComponent', () => {
  let component: InteractiveWebsiteModalComponent;
  let fixture: ComponentFixture<InteractiveWebsiteModalComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [InteractiveWebsiteModalModule, AppTestModule.withDefaults()],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(InteractiveWebsiteModalComponent);
    component = fixture.componentInstance;
    component.scrapeRequest = {
      title: '',
      flow: { sequence: [] },
    };
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});