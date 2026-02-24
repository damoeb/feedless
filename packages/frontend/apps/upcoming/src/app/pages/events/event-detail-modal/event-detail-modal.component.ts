import { Component, inject, PLATFORM_ID } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import {
  AlertController,
  IonBadge,
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonInput,
  IonItem,
  IonTextarea,
  IonToolbar,
  ModalController,
} from '@ionic/angular/standalone';
import {
  IconComponent,
  RecordService,
  RemoveIfProdDirective,
} from '@feedless/components';
import { addIcons } from 'ionicons';
import {
  calendarNumberOutline,
  closeOutline,
  openOutline,
  trashOutline,
} from 'ionicons/icons';
import { isPlatformBrowser } from '@angular/common';
import dayjs from 'dayjs';
import 'dayjs/locale/de';
import { LocalizedEvent } from '../../../event.service';
import { NamedLatLon } from '@feedless/core';

@Component({
  selector: 'app-event-detail-modal',
  templateUrl: './event-detail-modal.component.html',
  styleUrls: ['./event-detail-modal.component.scss'],
  standalone: true,
  imports: [
    ReactiveFormsModule,
    IonHeader,
    IonToolbar,
    IonButtons,
    IonButton,
    IconComponent,
    IonContent,
    IonBadge,
    IonItem,
    IonInput,
    IonTextarea,
    RemoveIfProdDirective,
  ],
})
export class EventDetailModalComponent {
  private readonly modalCtrl = inject(ModalController);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly recordService = inject(RecordService);
  private readonly alertCtrl = inject(AlertController);

  readonly event: LocalizedEvent;
  readonly place: NamedLatLon;
  readonly repositoryId: string;
  editMode = false;
  saving = false;
  deleting = false;

  readonly editForm = new FormGroup({
    title: new FormControl<string>('', { nonNullable: true }),
    body: new FormControl<string>('', { nonNullable: true }),
    tagsStr: new FormControl<string>('', { nonNullable: true }),
    startingAtStr: new FormControl<string>('', { nonNullable: true }),
  });

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({
        closeOutline,
        openOutline,
        calendarNumberOutline,
        trashOutline,
      });
    }
  }

  close() {
    return this.modalCtrl.dismiss();
  }

  toggleEditMode(): void {
    if (!this.editMode) {
      this.initEditForm();
    }
    this.editMode = !this.editMode;
  }

  private initEditForm(): void {
    const e = this.event;
    const tags = e.tags ?? [];
    const tagsStr = Array.isArray(tags)
      ? (tags as string[]).join(', ')
      : String(tags ?? '');
    const startingAt = e.startingAt != null ? e.startingAt : Date.now();
    const startingAtStr = dayjs(startingAt).format('YYYY-MM-DDTHH:mm');
    this.editForm.patchValue({
      title: e.title ?? '',
      body: e.text ?? '',
      tagsStr,
      startingAtStr,
    });
  }

  async save(): Promise<void> {
    if (this.saving) return;
    const e = this.event;
    const id = (e as { id?: string }).id ?? (e as unknown as { id: string }).id;
    if (!id) return;
    this.saving = true;
    try {
      const v = this.editForm.getRawValue();
      const tags = v.tagsStr
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean);
      await this.recordService.updateRecord({
        where: { id },
        data: {
          title: { set: v.title },
          text: { set: v.body },
          tags: { set: tags },
        },
      });
      this.editMode = false;
      await this.modalCtrl.dismiss({ saved: true });
    } finally {
      this.saving = false;
    }
  }

  async confirmDelete(): Promise<void> {
    const alert = await this.alertCtrl.create({
      header: 'Veranstaltung löschen',
      message: 'Möchtest du diese Veranstaltung wirklich löschen?',
      buttons: [
        { text: 'Abbrechen', role: 'cancel' },
        { text: 'Löschen', role: 'destructive', handler: () => this.delete() },
      ],
    });
    await alert.present();
  }

  private async delete(): Promise<void> {
    if (this.deleting) return;
    this.deleting = true;
    try {
      await this.recordService.removeById({
        where: {
          repository: { id: this.repositoryId },
          id: { eq: this.event.id },
        },
      });
      await this.modalCtrl.dismiss({ deleted: true });
    } finally {
      this.deleting = false;
    }
  }

  formatDate(date: number, format: string): string {
    return dayjs(date).locale('de').format(format);
  }

  openEventUrl(url: string) {
    if (url) {
      window.open(url, '_blank', 'noopener,noreferrer');
    }
  }

  getTitle(): string {
    return (
      this.event.title
        ?.replace(/[0-9]{1,2}\.[ .]?[a-z]{3,10}[ .]?[0-9]{2,4}/gi, '')
        ?.replace(/[0-9]{1,2}\.[ .]?[0-9]{1,2}[ .]?[0-9]{2,4}/gi, '') ?? ''
    );
  }
}
