import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchbarComponent } from './searchbar.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  IonInput,
  IonButton,
  IonIcon,
  IonSpinner,
} from '@ionic/angular/standalone';

@NgModule({
  declarations: [SearchbarComponent],
  exports: [SearchbarComponent],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    IonInput,
    IonButton,
    IonIcon,
    IonSpinner,
  ],
})
export class SearchbarModule {}
