import { Component, inject } from '@angular/core';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonReorder,
  IonReorderGroup,
  IonTitle,
  IonToolbar,
  ModalController,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { chevronBackOutline } from 'ionicons/icons';
import { SearchbarComponent } from '../../elements/searchbar/searchbar.component';

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
    IonIcon,
    IonContent,
    IonList,
    SearchbarComponent,
    IonItem,
    IonLabel,
    IonReorder,
    IonReorderGroup,
  ],
})
export class FlowModalComponent implements FlowModalComponentProps {
  private readonly modalCtrl = inject(ModalController);

  constructor() {
    addIcons({ chevronBackOutline });
  }

  closeModal() {
    return this.modalCtrl.dismiss();
  }
}
