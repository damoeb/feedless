import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoteDetailsComponent } from './note-details.component';
import { AppTestModule } from '../../app-test.module';
import { from, of } from 'rxjs';
import { NotebookService, NoteHandle } from '../../services/notebook.service';
import { AuthGuardService } from '../../guards/auth-guard.service';

describe('NoteDetailsComponent', () => {
  let component: NoteDetailsComponent;
  let fixture: ComponentFixture<NoteDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NoteDetailsComponent, AppTestModule.withDefaults()],
      providers: [
        {
          provide: AuthGuardService,
          useValue: {
            assertLoggedIn: () => {},
          },
        },
        {
          provide: NotebookService,
          useValue: {
            openNoteChanges: { subscribe: jest.fn() },
            closeNoteChanges: { subscribe: jest.fn() },
            moveStartChanges: { subscribe: jest.fn() },
            moveEndChanges: { subscribe: jest.fn() },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NoteDetailsComponent);
    component = fixture.componentInstance;

    const componentRef = fixture.componentRef;
    const handle: NoteHandle = {
      body: {} as any,
      body$: () => of(),
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
