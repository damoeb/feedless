import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChooseBucketComponent } from './choose-bucket.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [ChooseBucketComponent],
  exports: [ChooseBucketComponent],
  imports: [CommonModule, IonicModule],
})
export class ChooseBucketModule {}
