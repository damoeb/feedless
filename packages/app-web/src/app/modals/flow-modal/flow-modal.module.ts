import { NgModule } from '@angular/core';
import { FlowModalComponent } from '././flow-modal.component';
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
} from '@ionic/angular/standalone';
import { SearchbarComponent } from '../../elements/searchbar/searchbar.component';

@NgModule({
  declarations: [FlowModalComponent],
  exports: [FlowModalComponent],
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
export class FlowModalModule {}
