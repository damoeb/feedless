import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TextDiffComponent } from './text-diff.component';
import { AppTestModule } from '../../app-test.module';
import { Record } from '../../graphql/types';

describe('TextDiffComponent', () => {
  let component: TextDiffComponent;
  let fixture: ComponentFixture<TextDiffComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TextDiffComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(TextDiffComponent);
    component = fixture.componentInstance;
    component.before = {
      html: '',
      text: '',
      rawBase64: '',
    } as Record;
    component.after = {
      html: '',
      text: '',
      rawBase64: '',
    } as Record;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
