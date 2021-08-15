import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseComponent } from './base.component';
import { IonicModule } from '@ionic/angular';
import { BubbleModule } from '../bubble/bubble.module';
import { RouterModule } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { BucketCreateModule } from '../bucket-create/bucket-create.module';

@NgModule({
  declarations: [BaseComponent],
  exports: [BaseComponent],
  imports: [
    CommonModule,
    IonicModule,
    RouterModule,
    HttpClientModule,
    BubbleModule,
    BucketCreateModule,
  ],
})
export class BaseModule {}
