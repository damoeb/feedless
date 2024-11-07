import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { UpcomingHeaderComponent } from './upcoming-header.component';
import { AppTestModule, mockEvents } from '../../../app-test.module';
import dayjs from 'dayjs';
import { placeAffolternAmAlbis } from '../places';
import { EventsPageModule } from '../events/events-page.module';
import { AppConfigService } from '../../../services/app-config.service';
import { of } from 'rxjs';

describe('UpcomingHeaderComponent', () => {
  let component: UpcomingHeaderComponent;
  let fixture: ComponentFixture<UpcomingHeaderComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        EventsPageModule,
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
    component.location = placeAffolternAmAlbis;
    component.categories = [];
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
