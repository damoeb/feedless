import { Component, inject, PLATFORM_ID } from '@angular/core';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonItem,
  IonLabel,
  IonReorder,
  IonReorderGroup,
  IonTitle,
  IonToolbar,
  ModalController,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { chevronBackOutline } from 'ionicons/icons';
import { isPlatformBrowser } from '@angular/common';
import { IconComponent } from '../../components/icon/icon.component';

export interface FlowModalComponentProps {
  // tags: string[];
}

@Component({
  selector: 'app-flow-modal',
  templateUrl: './flow-modal.component.html',
  styleUrls: ['./flow-modal.component.scss'],
  standalone: true,
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IconComponent,
    IonContent,
    IonItem,
    IonLabel,
    IonReorder,
    IonReorderGroup,
  ],
})
export class FlowModalComponent implements FlowModalComponentProps {
  private readonly modalCtrl = inject(ModalController);
  private readonly platformId = inject(PLATFORM_ID);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ chevronBackOutline });
    }
  }

  closeModal() {
    return this.modalCtrl.dismiss();
  }
}
