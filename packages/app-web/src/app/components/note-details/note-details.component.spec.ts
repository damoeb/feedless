import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoteDetailsComponent } from './note-details.component';
import { AppTestModule } from '../../app-test.module';

describe('NoteDetailsComponent', () => {
  let component: NoteDetailsComponent;
  let fixture: ComponentFixture<NoteDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NoteDetailsComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(NoteDetailsComponent);
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
