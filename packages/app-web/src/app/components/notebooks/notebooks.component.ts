import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Notebook, NotebookService } from '../../services/notebook.service';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { addIcons } from 'ionicons';
import { chevronForwardOutline } from 'ionicons/icons';

@Component({
  selector: 'app-notebook-builder',
  templateUrl: './notebooks.component.html',
  styleUrls: ['./notebooks.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotebooksComponent implements OnInit, OnDestroy {
  busy = false;
  notebooks: Notebook[];
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly router: Router,
    private readonly notebookService: NotebookService,
    private readonly changeRef: ChangeDetectorRef,
  ) {
    addIcons({ chevronForwardOutline });
  }

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
    this.subscriptions.push(
      this.notebookService.notebooksChanges.subscribe((notebooks) => {
        this.notebooks = notebooks;
        this.changeRef.detectChanges();
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
