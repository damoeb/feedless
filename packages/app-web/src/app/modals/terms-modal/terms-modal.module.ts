import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TermsModalComponent } from './terms-modal.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [TermsModalComponent],
  exports: [TermsModalComponent],
  imports: [CommonModule, IonicModule],
})
export class TermsModalModule {}
