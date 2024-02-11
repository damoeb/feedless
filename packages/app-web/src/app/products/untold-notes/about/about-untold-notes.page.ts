import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Notebook, NotebookService } from '../services/notebook.service';

@Component({
  selector: 'app-about-untold-notes',
  templateUrl: './about-untold-notes.page.html',
  styleUrls: ['./about-untold-notes.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AboutUntoldNotesPage implements OnInit {
  busy = false;
  notebooks: Notebook[];

  constructor(
    private readonly router: Router,
    private readonly notebookService: NotebookService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async createNotebook(notebookName: string) {
    if (this.busy) {
      return;
    }
    this.busy = true;
    try {
      const notebook = await this.notebookService.createNotebook(notebookName);
      await this.router.navigateByUrl(`/notebook/${notebook.id}`);
    } catch (e) {
      console.warn(e);
    }
    this.busy = false;
  }

  ngOnInit(): void {
    this.notebookService.notebooksChanges.subscribe(notebooks => {
      this.notebooks = notebooks;
      this.changeRef.detectChanges();
    })
  }
}
