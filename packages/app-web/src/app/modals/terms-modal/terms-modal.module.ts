import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TermsModalComponent } from './terms-modal.component';
import { IonicModule } from '@ionic/angular';
import { TermsModule } from '../../components/terms/terms.module';

@NgModule({
  declarations: [TermsModalComponent],
  exports: [TermsModalComponent],
  imports: [CommonModule, IonicModule, TermsModule],
})
export class TermsModalModule {}
