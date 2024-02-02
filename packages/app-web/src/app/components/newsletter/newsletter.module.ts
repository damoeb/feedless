import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NewsletterComponent } from './newsletter.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';

@NgModule({
  declarations: [NewsletterComponent],
  exports: [NewsletterComponent],
  imports: [
    CommonModule,
    IonicModule,
    ReactiveFormsModule,
    FormsModule,
    SearchbarModule,
  ],
})
export class NewsletterModule {}
