import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchAddressModalComponent } from './search-address-modal.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [SearchAddressModalComponent],
  exports: [SearchAddressModalComponent],
  imports: [
    CommonModule,
    IonicModule,
    RouterLink,
    FormsModule
  ]
})
export class SearchAddressModalModule {}
