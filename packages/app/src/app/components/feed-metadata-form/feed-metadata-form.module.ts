import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedMetadataFormComponent } from './feed-metadata-form.component';
import { IonicModule } from '@ionic/angular';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [FeedMetadataFormComponent],
  exports: [FeedMetadataFormComponent],
  imports: [CommonModule, IonicModule, ReactiveFormsModule],
})
export class FeedMetadataFormModule {}
