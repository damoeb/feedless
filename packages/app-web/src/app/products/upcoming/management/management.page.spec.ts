import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AppTestModule, mockRepository } from '../../../app-test.module';
import { ManagementPage } from './management.page';
import { AppConfigService } from '../../../services/app-config.service';
import { PageService } from '../../../services/page.service';

describe('ManagementPage', () => {
  let component: ManagementPage;
  let fixture: ComponentFixture<ManagementPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ManagementPage,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => {
            mockRepository(apolloMockController);
          },
        }),
      ],
      providers: [{
        provide: PageService,
        useValue: {
          setMetaTags: jest.fn()
        }
      }],
    }).compileComponents();

    const appConfigService = TestBed.inject(AppConfigService);
    appConfigService.customProperties = { eventRepositoryId: 'foo' };

    fixture = TestBed.createComponent(ManagementPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
