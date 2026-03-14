import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AppTestModule, mockRepository } from '@feedless/testing';
import { EventSourcesPage } from './event-sources.page';
// eslint-disable-next-line @nx/enforce-module-boundaries
import { AppConfigService, PageService } from '@feedless/components';

describe('EventSourcesPage', () => {
  let component: EventSourcesPage;
  let fixture: ComponentFixture<EventSourcesPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        EventSourcesPage,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => {
            mockRepository(apolloMockController);
          },
        }),
      ],
      providers: [
        {
          provide: PageService,
          useValue: {
            setMetaTags: jest.fn(),
          },
        },
      ],
    }).compileComponents();

    const appConfigService = TestBed.inject(AppConfigService);
    appConfigService.customProperties = { eventRepositoryId: 'foo' };

    fixture = TestBed.createComponent(EventSourcesPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
