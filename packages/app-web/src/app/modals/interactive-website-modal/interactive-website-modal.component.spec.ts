import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { InteractiveWebsiteModalComponent } from './interactive-website-modal.component';
import {
  ApolloMockController,
  AppTestModule,
  mockServerSettings,
} from '../../app-test.module';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('InteractiveWebsiteModalComponent', () => {
  let component: InteractiveWebsiteModalComponent;
  let fixture: ComponentFixture<InteractiveWebsiteModalComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [InteractiveWebsiteModalComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(InteractiveWebsiteModalComponent);
    component = fixture.componentInstance;
    component.source = {
      title: '',
      flow: {
        sequence: [
          {
            fetch: {
              get: {
                url: {
                  literal: '',
                },
              },
            },
          },
        ],
      },
    };
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
