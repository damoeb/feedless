import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BucketComponent } from './bucket.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [BucketComponent],
  exports: [BucketComponent],
  imports: [CommonModule, IonicModule],
})
export class BucketModule {}
