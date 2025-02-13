import { Component, inject } from '@angular/core';
import { ModalController } from '@ionic/angular/standalone';
import { sortedUniq, without } from 'lodash-es';
import { addIcons } from 'ionicons';
import { closeOutline, trashOutline } from 'ionicons/icons';

export interface TagsModalComponentProps {
  tags: string[];
}

@Component({
  selector: 'app-tags-modal',
  templateUrl: './tags-modal.component.html',
  styleUrls: ['./tags-modal.component.scss'],
  standalone: false,
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
      this.tags = sortedUniq([...this.tags, tag]);
    }
  }
}
