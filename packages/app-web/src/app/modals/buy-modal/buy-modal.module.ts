import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BuyModalComponent } from './buy-modal.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule } from '@angular/forms';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';

@NgModule({
  declarations: [BuyModalComponent],
  exports: [BuyModalComponent],
  imports: [CommonModule, IonicModule, FormsModule, SearchbarModule],
})
export class BuyModalModule {}
