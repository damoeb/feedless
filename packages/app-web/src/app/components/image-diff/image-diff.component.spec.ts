import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ImageDiffComponent } from './image-diff.component';
import { AppTestModule } from '../../app-test.module';
import { ImageDiffModule } from './image-diff.module';
import { Record } from '../../graphql/types';

describe('ImageDiffComponent', () => {
  let component: ImageDiffComponent;
  let fixture: ComponentFixture<ImageDiffComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ImageDiffComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(ImageDiffComponent);
    component = fixture.componentInstance;
    component.before = {} as Record;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
