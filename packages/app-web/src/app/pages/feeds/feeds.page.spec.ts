import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeedsPage } from './feeds.page';
import { AppTestModule, mockRepositories } from '../../app-test.module';
import { ModalController } from '@ionic/angular/standalone';
import { IonicModule } from '@ionic/angular';

describe('FeedsPage', () => {
  let component: FeedsPage;
  let fixture: ComponentFixture<FeedsPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        FeedsPage,
        IonicModule.forRoot(),
        AppTestModule.withDefaults({
          configurer: (apolloMockController) =>
            mockRepositories(apolloMockController),
        }),
      ],
      providers: [
        {
          provide: ModalController,
          useValue: {
            create: () =>
              Promise.resolve({
                present: () => Promise.resolve(),
                onDidDismiss: () => Promise.resolve({}),
              }),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedsPage);
    component = fixture.componentInstance;
    component.repositories = [];
    component.documents = [];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
