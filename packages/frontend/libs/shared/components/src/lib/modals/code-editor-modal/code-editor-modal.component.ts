import {
  Component,
  inject,
  PLATFORM_ID,
  ChangeDetectionStrategy,
} from '@angular/core';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  ModalController,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { closeOutline } from 'ionicons/icons';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { IconComponent } from '@feedless/ui';
import {
  CodeEditorComponent,
  ContentType,
} from '@feedless/ui';

export type CodeEditorModalDetail = {
  label: string;
  value: string;
  href?: string;
};

export interface CodeEditorModalComponentProps {
  text: string;
  title: string;
  details?: CodeEditorModalDetail[];
  readOnly?: boolean;
  controls?: boolean;
  contentType?: ContentType;
}

@Component({
  selector: 'app-code-editor-modal',
  templateUrl: './code-editor-modal.component.html',
  styleUrls: ['./code-editor-modal.component.scss'],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [
    CommonModule,
    FormsModule,
    IonHeader,
    ReactiveFormsModule,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IconComponent,
    IonContent,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonContent,
    CodeEditorComponent,
  ],
})
export class CodeEditorModalComponent implements CodeEditorModalComponentProps {
  private readonly modalCtrl = inject(ModalController);
  private readonly platformId = inject(PLATFORM_ID);

  text: string;
  title: string;
  details: CodeEditorModalDetail[] = [];
  contentType: ContentType;
  readOnly: boolean;

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ closeOutline });
    }
  }

  cancel() {
    return this.modalCtrl.dismiss();
  }
}
