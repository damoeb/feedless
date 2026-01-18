import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AppTestModule, mockRepository } from '@feedless/testing';
import { EditorPage } from './editor.page';
import { AppConfigService, PageService } from '@feedless/services';

describe('EditorPage', () => {
  let component: EditorPage;
  let fixture: ComponentFixture<EditorPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        EditorPage,
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

    fixture = TestBed.createComponent(EditorPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
