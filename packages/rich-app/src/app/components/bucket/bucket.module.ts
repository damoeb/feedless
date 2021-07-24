import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BucketComponent } from './bucket.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [BucketComponent],
  exports: [BucketComponent],
  imports: [CommonModule, IonicModule, FormsModule],
})
export class BucketModule {}
