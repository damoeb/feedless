import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImportTransientNativeFeedComponent } from './import-transient-native-feed.component';
import { IonicModule } from '@ionic/angular';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [ImportTransientNativeFeedComponent],
  exports: [ImportTransientNativeFeedComponent],
  imports: [CommonModule, IonicModule, ReactiveFormsModule],
})
export class ImportTransientNativeFeedModule {}
