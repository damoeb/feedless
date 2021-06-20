import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {BucketComponent} from './bucket.component';



@NgModule({
  declarations: [BucketComponent],
  exports: [BucketComponent],
  imports: [
    CommonModule
  ]
})
export class BucketModule { }
