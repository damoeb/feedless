import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NewsletterComponent } from './newsletter.component';
import { IonicModule } from '@ionic/angular';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [NewsletterComponent],
  exports: [NewsletterComponent],
  imports: [CommonModule, IonicModule, ReactiveFormsModule]
})
export class NewsletterModule {
}
