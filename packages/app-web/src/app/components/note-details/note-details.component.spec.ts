import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoteDetailsComponent } from './note-details.component';
import { AppTestModule } from '../../app-test.module';
import { NoteHandle } from '../../pages/notebook-details/notebook-details.page';
import { from, of } from 'rxjs';

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
    const handle: NoteHandle = {
      body: {} as any,
      expanded: false,
      disabled: false,
      level: 0,
      childrenCount: () => of(0),
      scrollTo: (event: MouseEvent) => {},
      children: () => from([]),
      toggleUpvote: () => {},
    };
    componentRef.setInput('handle', handle);

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
