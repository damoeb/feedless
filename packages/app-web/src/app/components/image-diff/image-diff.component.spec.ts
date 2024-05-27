import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ImageDiffComponent } from './image-diff.component';
import { AppTestModule } from '../../app-test.module';
import { ImageDiffModule } from './image-diff.module';

describe('ImageDiffComponent', () => {
  let component: ImageDiffComponent;
  let fixture: ComponentFixture<ImageDiffComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ImageDiffModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(ImageDiffComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
