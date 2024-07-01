import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchAddressModalComponent } from './search-address-modal.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';

@NgModule({
  declarations: [SearchAddressModalComponent],
  exports: [SearchAddressModalComponent],
  imports: [
    CommonModule,
    IonicModule,
    RouterLink,
    FormsModule,
    SearchbarModule,
  ],
})
export class SearchAddressModalModule {}
