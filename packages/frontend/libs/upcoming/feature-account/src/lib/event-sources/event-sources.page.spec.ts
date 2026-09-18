import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppTestModule, mockRepository } from '@feedless/testing';
import { EventSourcesPage } from './event-sources.page';
 
import { AppConfigService } from '@feedless/data-access-auth';
import { PageService } from '@feedless/data-access';

describe('EventSourcesPage', () => {
  let component: EventSourcesPage;
  let fixture: ComponentFixture<EventSourcesPage>;

  beforeEach(async () => {
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
            setMetaTags: vi.fn(),
          },
        },
      ],
    }).compileComponents();

    const appConfigService = TestBed.inject(AppConfigService);
    appConfigService.customProperties = { eventRepositoryId: 'foo' };

    fixture = TestBed.createComponent(EventSourcesPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
