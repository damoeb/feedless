import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { NotebookHead, NotebookService } from '../services/notebook.service';

@Component({
  selector: 'app-untold-notes-menu',
  templateUrl: './untold-notes-menu.component.html',
  styleUrls: ['./untold-notes-menu.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntoldNotesMenuComponent implements OnInit {
  notebooks: NotebookHead[];

  constructor(
    private readonly notebookService: NotebookService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.notebookService.notebooksChanges.subscribe((notebooks) => {
      this.notebooks = notebooks;
      this.changeRef.detectChanges();
    });
  }
}
