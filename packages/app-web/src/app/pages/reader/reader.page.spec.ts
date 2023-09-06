import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ReaderPage } from './reader.page';
import { AppTestModule, mockScrape } from '../../app-test.module';
import { ReaderPageModule } from './reader.module';
import { RouterTestingModule } from '@angular/router/testing';

describe('ReaderPage', () => {
  let component: ReaderPage;
  let fixture: ComponentFixture<ReaderPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        ReaderPageModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockScrape(apolloMockController);
        }),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ReaderPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
