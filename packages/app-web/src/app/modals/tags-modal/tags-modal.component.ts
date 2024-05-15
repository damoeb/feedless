import { Component } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { sortedUniq, without } from 'lodash-es';

export interface TagsModalComponentProps {
  tags: string[]
}

@Component({
  selector: 'app-tags-modal',
  templateUrl: './tags-modal.component.html',
  styleUrls: ['./tags-modal.component.scss'],
})
export class TagsModalComponent implements TagsModalComponentProps {

  tags: string[] = [];

  constructor(private readonly modalCtrl: ModalController) {}

  closeModal() {
    return this.modalCtrl.dismiss(this.tags);
  }

  removeTag(tag: string) {
    this.tags = without(this.tags, tag)
  }

  addTag(value: string | number) {
    const tag = `${value}`;
    if (tag.trim().length > 0) {
      this.tags.push(tag);
      this.tags = sortedUniq(this.tags);
    }
  }
}
