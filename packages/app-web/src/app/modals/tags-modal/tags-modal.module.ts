import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TagsModalComponent } from './tags-modal.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule } from '@angular/forms';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';

@NgModule({
  declarations: [TagsModalComponent],
  exports: [TagsModalComponent],
  imports: [CommonModule, IonicModule, FormsModule, SearchbarModule]
})
export class TagsModalModule {}
