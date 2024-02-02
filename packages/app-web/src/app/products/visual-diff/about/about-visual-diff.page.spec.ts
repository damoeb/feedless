import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AboutVisualDiffPage } from './about-visual-diff.page';
import { AppTestModule } from '../../../app-test.module';
import { AboutVisualDiffModule } from './about-visual-diff.module';

describe('AboutVisualDiffPage', () => {
  let component: AboutVisualDiffPage;
  let fixture: ComponentFixture<AboutVisualDiffPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [AboutVisualDiffModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(AboutVisualDiffPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
