import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Notebook, NotebookService } from '../../services/notebook.service';
import { Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { addIcons } from 'ionicons';
import { chevronForwardOutline } from 'ionicons/icons';
import { ProductHeaderComponent } from '../product-header/product-header.component';
import { SearchbarComponent } from '../../elements/searchbar/searchbar.component';

import {
  IonChip,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonListHeader,
} from '@ionic/angular/standalone';

@Component({
  selector: 'app-notebook-builder',
  templateUrl: './notebooks.component.html',
  styleUrls: ['./notebooks.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ProductHeaderComponent,
    SearchbarComponent,
    IonList,
    IonListHeader,
    IonLabel,
    IonItem,
    RouterLink,
    IonIcon,
    IonChip,
  ],
  standalone: true,
})
export class NotebooksComponent implements OnInit, OnDestroy {
  private readonly router = inject(Router);
  private readonly notebookService = inject(NotebookService);
  private readonly changeRef = inject(ChangeDetectorRef);

  busy = false;
  notebooks: Notebook[];
  private subscriptions: Subscription[] = [];

  constructor() {
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
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
