import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  input,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Note, NotebookService, NoteId } from '../../services/notebook.service';
import { NgClass } from '@angular/common';
import {
  createNoteHandleId,
  NoteHandle,
} from '../../pages/notebook-details/notebook-details.page';
import { relativeTimeOrElse } from '../agents/agents.component';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-note-details',
  templateUrl: './note-details.component.html',
  styleUrls: ['./note-details.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgClass],
  standalone: true,
})
export class NoteDetailsComponent implements OnInit, OnDestroy {
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly notebookService = inject(NotebookService);

  readonly handle = input.required<NoteHandle>();
  isOpen: boolean = false;
  isMove: boolean = false;
  private subscriptions: Subscription[] = [];
  private noteIdMoved: NoteId;

  constructor() {}

  ngOnInit() {
    this.subscriptions.push(
      this.notebookService.openNoteChanges.subscribe((note) => {
        const isIdEqual = note.id === this.note().id;
        // const isParentIdEqual = note.parent === this.handle().body.parent;
        this.isOpen = isIdEqual;
        // this.handle().expanded = isIdEqual || !isParentIdEqual;
        this.changeRef.detectChanges();
      }),
      this.notebookService.closeNoteChanges.subscribe((note) => {
        if (note.id === this.note().id) {
          this.isOpen = false;
        }
        this.handle().expanded = true;
        this.changeRef.detectChanges();
      }),
      this.notebookService.moveStartChanges.subscribe((noteId) => {
        if (noteId === this.note().id || this.isChild(noteId)) {
          this.handle().disabled = true;
        } else {
          this.isMove = true;
          this.noteIdMoved = noteId;
        }
        this.changeRef.detectChanges();
      }),
      this.notebookService.moveEndChanges.subscribe((noteId) => {
        if (this.handle().disabled) {
          this.handle().disabled = false;
        }
        this.isMove = false;
        this.changeRef.detectChanges();
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s?.unsubscribe());
  }

  toggle() {
    if (this.isMove) {
      this.notebookService.changeParentById(
        this.noteIdMoved,
        this.handle().body.id,
      );
    } else {
      return this.notebookService.openNoteById(this.handle().body.id);
    }
  }

  extendNote(event: Event) {
    this.notebookService.createNote(
      {
        parent: this.handle().body.id,
      },
      true,
    );
    event.stopPropagation();
  }

  toggleUpvote(event: Event) {
    this.handle().toggleUpvote();
    event.stopPropagation();
  }

  note(): Note {
    return this.handle().body;
  }

  changeAgo() {
    return relativeTimeOrElse(this.note().updatedAt);
  }

  private isChild(noteId: string): boolean {
    return false;
  }

  getId() {
    return createNoteHandleId(this.note());
  }
}
