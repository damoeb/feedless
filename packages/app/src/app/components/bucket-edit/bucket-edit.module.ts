import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BucketEditComponent } from './bucket-edit.component';
import { IonicModule } from '@ionic/angular';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [BucketEditComponent],
  exports: [BucketEditComponent],
  imports: [CommonModule, IonicModule, ReactiveFormsModule],
})
export class BucketEditModule {}
