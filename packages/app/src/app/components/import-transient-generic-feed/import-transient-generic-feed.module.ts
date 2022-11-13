import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImportTransientGenericFeedComponent } from './import-transient-generic-feed.component';
import { IonicModule } from '@ionic/angular';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [ImportTransientGenericFeedComponent],
  exports: [ImportTransientGenericFeedComponent],
  imports: [CommonModule, IonicModule, ReactiveFormsModule],
})
export class ImportTransientGenericFeedModule {}
