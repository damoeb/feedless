import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchAddressModalComponent } from './search-address-modal.component';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonButtons,
  IonButton,
  IonIcon,
  IonContent,
  IonList,
  IonItem,
  IonLabel,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [SearchAddressModalComponent],
  exports: [SearchAddressModalComponent],
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    SearchbarModule,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonIcon,
    IonContent,
    IonList,
    IonItem,
    IonLabel,
  ],
})
export class SearchAddressModalModule {}
