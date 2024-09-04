import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FilterItemsAccordionComponent } from './filter-items-accordion.component';
import { IonicModule } from '@ionic/angular';
import { ReactiveFormsModule } from '@angular/forms';
import { CodeEditorModule } from '../../elements/code-editor/code-editor.module';

@NgModule({
  declarations: [FilterItemsAccordionComponent],
  exports: [FilterItemsAccordionComponent],
  imports: [CommonModule, IonicModule, ReactiveFormsModule, CodeEditorModule],
})
export class FilterItemsAccordionModule {}
