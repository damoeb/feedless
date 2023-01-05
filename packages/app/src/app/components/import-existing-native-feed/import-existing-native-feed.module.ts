import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImportExistingNativeFeedComponent } from './import-existing-native-feed.component';
import { IonicModule } from '@ionic/angular';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [ImportExistingNativeFeedComponent],
  exports: [ImportExistingNativeFeedComponent],
  imports: [CommonModule, IonicModule, ReactiveFormsModule],
})
export class ImportExistingNativeFeedModule {}
