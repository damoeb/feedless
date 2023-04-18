import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ExternalLinkComponent } from './external-link.component';
import { IonicModule } from '@ionic/angular';

@NgModule({
  declarations: [ExternalLinkComponent],
  exports: [ExternalLinkComponent],
  imports: [CommonModule, IonicModule],
})
export class ExternalLinkModule {}
