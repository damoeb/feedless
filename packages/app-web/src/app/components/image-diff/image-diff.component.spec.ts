import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ImageDiffComponent } from './image-diff.component';
import { AppTestModule } from '../../app-test.module';
import { ImageDiffModule } from './image-diff.module';
import { WebDocument } from '../../graphql/types';

describe('ImageDiffComponent', () => {
  let component: ImageDiffComponent;
  let fixture: ComponentFixture<ImageDiffComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ImageDiffModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(ImageDiffComponent);
    component = fixture.componentInstance;
    component.before = {} as WebDocument
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
