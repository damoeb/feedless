import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScrapeActionsComponent } from './scrape-actions.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SelectModule } from '../select/select.module';

@NgModule({
  declarations: [ScrapeActionsComponent],
  exports: [ScrapeActionsComponent],
  imports: [CommonModule, IonicModule, FormsModule, ReactiveFormsModule, SelectModule]
})
export class ScrapeActionsModule {}
