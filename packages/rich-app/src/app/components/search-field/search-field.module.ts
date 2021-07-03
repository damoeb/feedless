import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchFieldComponent } from './search-field.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [SearchFieldComponent],
  exports: [SearchFieldComponent],
  imports: [CommonModule, IonicModule],
})
export class SearchFieldModule {}
