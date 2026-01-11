import { Component, inject, PLATFORM_ID } from '@angular/core';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonItem,
  IonLabel,
  IonList,
  IonTitle,
  IonToolbar,
  ModalController,
} from '@ionic/angular/standalone';
import { sortedUniq, without } from 'lodash-es';
import { addIcons } from 'ionicons';
import { closeOutline, trashOutline } from 'ionicons/icons';
import { SearchbarComponent } from '@feedless/form-elements';
import { isPlatformBrowser } from '@angular/common';
import { IconComponent } from '../../components/icon/icon.component';

export interface TagsModalComponentProps {
  tags: string[];
}

@Component({
  selector: 'app-tags-modal',
  templateUrl: './tags-modal.component.html',
  styleUrls: ['./tags-modal.component.scss'],
  standalone: true,
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IconComponent,
    IonContent,
    IonList,
    SearchbarComponent,
    IonItem,
    IonLabel,
  ],
})
export class TagsModalComponent implements TagsModalComponentProps {
  private readonly modalCtrl = inject(ModalController);
  private readonly platformId = inject(PLATFORM_ID);
  tags: string[] = [];

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ closeOutline, trashOutline });
    }
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
      this.tags = sortedUniq([...this.tags, tag]);
    }
  }
}
