import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScrapeActionsComponent } from './scrape-actions.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [ScrapeActionsComponent],
  exports: [ScrapeActionsComponent],
  imports: [CommonModule, IonicModule, FormsModule, ReactiveFormsModule],
})
export class ScrapeActionsModule {}
