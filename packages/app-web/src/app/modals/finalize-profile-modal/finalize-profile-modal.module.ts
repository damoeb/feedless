import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FinalizeProfileModalComponent } from './finalize-profile-modal.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [FinalizeProfileModalComponent],
  exports: [FinalizeProfileModalComponent],
  imports: [CommonModule, IonicModule, RouterLink, ReactiveFormsModule],
})
export class FinalizeProfileModalModule {}
