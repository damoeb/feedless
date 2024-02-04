import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Router } from '@angular/router';
import { NotebookService } from '../services/notebook.service';

@Component({
  selector: 'app-about-untold-notes',
  templateUrl: './about-untold-notes.page.html',
  styleUrls: ['./about-untold-notes.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AboutUntoldNotesPage {
  busy = false;

  constructor(
    private readonly router: Router,
    private readonly notebookService: NotebookService,
  ) {}

  async createNotebook(notebookName: string) {
    if (this.busy) {
      return;
    }
    this.busy = true;
    try {
      const notebook = await this.notebookService.createNotebook(notebookName);
      await this.router.navigateByUrl(`/notebook/${notebook.id}`)
    } catch (e) {
      console.warn(e);
    }
    this.busy = false;
  }
}
