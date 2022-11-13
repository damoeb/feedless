import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PreviewTransientNativeFeedComponent } from './preview-transient-native-feed.component';
import { IonicModule } from '@ionic/angular';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [PreviewTransientNativeFeedComponent],
  exports: [PreviewTransientNativeFeedComponent],
  imports: [CommonModule, IonicModule, ReactiveFormsModule],
})
export class PreviewTransientNativeFeedModule {}
