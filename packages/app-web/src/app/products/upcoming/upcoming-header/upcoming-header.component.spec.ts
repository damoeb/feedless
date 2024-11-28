import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { UpcomingHeaderComponent } from './upcoming-header.component';
import { AppTestModule, mockEvents } from '../../../app-test.module';
import dayjs from 'dayjs';
import { getCachedLocations } from '../places';
import { AppConfigService } from '../../../services/app-config.service';
import { of } from 'rxjs';

describe('UpcomingHeaderComponent', () => {
  let component: UpcomingHeaderComponent;
  let fixture: ComponentFixture<UpcomingHeaderComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        UpcomingHeaderComponent,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => {
            mockEvents(apolloMockController);
          },
        }),
      ],
      providers: [
        {
          provide: AppConfigService,
          useValue: {
            customProperties: { eventRepositoryId: '123' },
            getActiveProductConfigChange: () => of(),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UpcomingHeaderComponent);
    component = fixture.componentInstance;
    component.date = dayjs();
    component.location = getCachedLocations()[0];
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
