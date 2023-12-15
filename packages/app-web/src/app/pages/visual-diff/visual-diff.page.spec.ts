import { async, ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { VisualDiffPage } from './visual-diff.page';
import { ScrapeSourceComponent } from '../../components/scrape-source/scrape-source.component';
import { ScrapeSourceModule } from '../../components/scrape-source/scrape-source.module';
import { AppTestModule } from '../../app-test.module';
import { VisualDiffPageModule } from './visual-diff.module';

describe('VisualDiffPage', () => {
  let component: VisualDiffPage;
  let fixture: ComponentFixture<VisualDiffPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ VisualDiffPage ],
      imports: [VisualDiffPageModule, AppTestModule.withDefaults()]
    }).compileComponents();

    fixture = TestBed.createComponent(VisualDiffPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
