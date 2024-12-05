import { NgModule } from '@angular/core';
import { MapModalComponent } from './map-modal.component';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonIcon,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';
import { MapComponent } from '../../components/map/map.component';

@NgModule({
  declarations: [MapModalComponent],
  exports: [MapModalComponent],
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonIcon,
    IonContent,
    MapComponent,
  ],
})
export class MapModalModule {}
