import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { Notebook, NotebookService } from '../../services/notebook.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-notebook-builder',
  templateUrl: './notebook-builder.component.html',
  styleUrls: ['./notebook-builder.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotebookBuilderComponent implements OnInit {
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
    this.notebookService.notebooksChanges.subscribe((notebooks) => {
      this.notebooks = notebooks;
      this.changeRef.detectChanges();
    });
  }
}
