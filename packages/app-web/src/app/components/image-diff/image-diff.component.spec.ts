import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ImageDiffComponent } from './image-diff.component';
import { AppTestModule } from '../../app-test.module';

describe('ImageDiffComponent', () => {
  let component: ImageDiffComponent;
  let fixture: ComponentFixture<ImageDiffComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ImageDiffComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(ImageDiffComponent);
    component = fixture.componentInstance;
    const componentRef = fixture.componentRef;
    componentRef.setInput('before', {});
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
