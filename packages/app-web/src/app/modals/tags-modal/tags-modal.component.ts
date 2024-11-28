import { Component, inject } from '@angular/core';
import {
  ModalController,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonButtons,
  IonButton,
  IonIcon,
  IonContent,
  IonList,
  IonItem,
  IonLabel,
} from '@ionic/angular/standalone';
import { sortedUniq, without } from 'lodash-es';
import { addIcons } from 'ionicons';
import { closeOutline, trashOutline } from 'ionicons/icons';
import { SearchbarComponent } from '../../elements/searchbar/searchbar.component';


export interface TagsModalComponentProps {
  tags: string[];
}

@Component({
  selector: 'app-tags-modal',
  templateUrl: './tags-modal.component.html',
  styleUrls: ['./tags-modal.component.scss'],
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonIcon,
    IonContent,
    IonList,
    SearchbarComponent,
    IonItem,
    IonLabel
],
  standalone: true,
})
export class TagsModalComponent implements TagsModalComponentProps {
  private readonly modalCtrl = inject(ModalController);

  tags: string[] = [];

  constructor() {
    addIcons({ closeOutline, trashOutline });
  }

  closeModal() {
    return this.modalCtrl.dismiss(this.tags);
  }

  removeTag(tag: string) {
    this.tags = without(this.tags, tag);
  }

  addTag(value: string | number) {
    const tag = `${value}`;
    if (tag.trim().length > 0) {
      this.tags.push(tag);
      this.tags = sortedUniq(this.tags);
    }
  }
}
