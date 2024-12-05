import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AboutVisualDiffPage } from './about-visual-diff.page';
import { AppTestModule } from '../../../app-test.module';

describe('AboutVisualDiffPage', () => {
  let component: AboutVisualDiffPage;
  let fixture: ComponentFixture<AboutVisualDiffPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AboutVisualDiffPage, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(AboutVisualDiffPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
