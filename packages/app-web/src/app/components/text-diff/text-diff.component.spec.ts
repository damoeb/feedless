import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TextDiffComponent } from './text-diff.component';
import { AppTestModule } from '../../app-test.module';

describe('TextDiffComponent', () => {
  let component: TextDiffComponent;
  let fixture: ComponentFixture<TextDiffComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TextDiffComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(TextDiffComponent);
    component = fixture.componentInstance;

    const componentRef = fixture.componentRef;
    componentRef.setInput('before', {
      html: '',
      text: '',
      rawBase64: '',
    });
    componentRef.setInput('after', {
      html: '',
      text: '',
      rawBase64: '',
    });

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
