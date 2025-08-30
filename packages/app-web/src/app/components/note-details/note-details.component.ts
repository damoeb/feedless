import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  input,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Note, NotebookService } from '../../services/notebook.service';
import { NgClass } from '@angular/common';
import {
  createNoteHandleId,
  TreeNodeHandle,
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

  readonly handle = input.required<TreeNodeHandle>();
  isOpen: boolean = false;
  private subscriptions: Subscription[] = [];

  constructor() {}

  ngOnInit() {
    this.subscriptions.push(
      this.notebookService.openNoteChanges.subscribe((note) => {
        this.isOpen = note.id === this.note().id;
        this.changeRef.detectChanges();
      }),
      this.notebookService.closeNoteChanges.subscribe((note) => {
        if (note.id === this.note().id) {
          this.isOpen = false;
        }
        this.changeRef.detectChanges();
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  toggle() {
    return this.notebookService.openNoteById(this.handle().body.id);
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

  getId() {
    return createNoteHandleId(this.note());
  }
}
