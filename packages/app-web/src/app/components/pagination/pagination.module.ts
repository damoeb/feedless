import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PaginationComponent } from './pagination.component';
import { BubbleModule } from '../bubble/bubble.module';
import { RouterLink } from '@angular/router';
import { IonToolbar, IonButtons, IonButton } from '@ionic/angular/standalone';

@NgModule({
  declarations: [PaginationComponent],
  exports: [PaginationComponent],
  imports: [
    CommonModule,
    BubbleModule,
    RouterLink,
    IonToolbar,
    IonButtons,
    IonButton,
  ],
})
export class PaginationModule {}
