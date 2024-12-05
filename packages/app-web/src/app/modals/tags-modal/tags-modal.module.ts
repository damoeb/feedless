import { NgModule } from '@angular/core';
import { TagsModalComponent } from './tags-modal.component';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';
import { SearchbarComponent } from '../../elements/searchbar/searchbar.component';

@NgModule({
  declarations: [TagsModalComponent],
  exports: [TagsModalComponent],
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
  ],
})
export class TagsModalModule {}
