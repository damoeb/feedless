import { NgModule } from '@angular/core';
import { SearchAddressModalComponent } from './search-address-modal.component';
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
  declarations: [SearchAddressModalComponent],
  exports: [SearchAddressModalComponent],
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonIcon,
    IonContent,
    SearchbarComponent,
    IonList,
    IonItem,
    IonLabel,
  ],
})
export class SearchAddressModalModule {}
