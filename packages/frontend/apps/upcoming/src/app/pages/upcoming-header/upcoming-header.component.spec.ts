import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { UpcomingHeaderComponent } from './upcoming-header.component';
import { AppTestModule, mockEvents } from '@feedless/test';
import dayjs from 'dayjs';
import { getCachedLocations } from '../../../../../../libs/geo/src/lib/places';
import { AppConfigService } from '@feedless/services';
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
    const componentRef = fixture.componentRef;
    componentRef.setInput('date', dayjs());
    componentRef.setInput('location', getCachedLocations()[0]);
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
