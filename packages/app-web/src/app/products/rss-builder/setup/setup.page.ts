import { ChangeDetectionStrategy, Component, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import '@justinribeiro/lite-youtube';
import {
  IonButton,
  IonButtons,
  IonCard,
  IonCardContent,
  IonCardHeader, IonCardSubtitle, IonCardTitle, IonCheckbox,
  IonContent,
  IonHeader, IonItem, IonList, IonListHeader, IonNote, IonSelect, IonSelectOption, IonText,
  IonToolbar
} from '@ionic/angular/standalone';
import { DarkModeButtonComponent } from '../../../components/dark-mode-button/dark-mode-button.component';

@Component({
  selector: 'app-setup',
  templateUrl: './setup.page.html',
  styleUrls: ['./setup.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    IonContent,
    IonCard,
    IonCardHeader,
    IonCardContent,
    DarkModeButtonComponent,
    IonButtons,
    IonHeader,
    IonToolbar,
    IonButton,
    IonText,
    IonSelect,
    IonSelectOption,
    IonList,
    IonItem,
    IonCheckbox,
    IonCardSubtitle,
    IonCardTitle,
    IonListHeader,
    IonNote
  ],
  standalone: true,
})
export class SetupPage {
  currentPage: number = 0;
  constructor() {
  }

}
