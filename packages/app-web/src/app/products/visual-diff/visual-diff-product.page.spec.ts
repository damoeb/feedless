import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { VisualDiffProductPage } from './visual-diff-product.page';
import { AppTestModule } from '../../app-test.module';
import { VisualDiffProductModule } from './visual-diff-product.module';

describe('VisualDiffProductPage', () => {
  let component: VisualDiffProductPage;
  let fixture: ComponentFixture<VisualDiffProductPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [VisualDiffProductModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(VisualDiffProductPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
