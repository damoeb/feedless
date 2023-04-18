import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PageHeaderComponent } from './page-header.component';
import { IonicModule } from '@ionic/angular';
import { RouterModule } from '@angular/router';
import { WizardModule } from '../wizard/wizard.module';

@NgModule({
  declarations: [PageHeaderComponent],
  exports: [PageHeaderComponent],
  imports: [CommonModule, IonicModule, RouterModule, WizardModule],
})
export class PageHeaderModule {}
