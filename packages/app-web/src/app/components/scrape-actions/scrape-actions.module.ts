import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScrapeActionsComponent } from './scrape-actions.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SelectModule } from '../select/select.module';
import { InputTextModule } from 'primeng/inputtext';

@NgModule({
  declarations: [ScrapeActionsComponent],
  exports: [ScrapeActionsComponent],
  imports: [CommonModule, IonicModule, FormsModule, ReactiveFormsModule, SelectModule, InputTextModule]
})
export class ScrapeActionsModule {}
