import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { SourcePageRoutingModule } from './source-routing.module';

import { SourcePage } from './source.page';
import { PageHeaderModule } from '../../../components/page-header/page-header.module';
import { ExternalLinkModule } from '../../../components/external-link/external-link.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    SourcePageRoutingModule,
    PageHeaderModule,
    ExternalLinkModule,
  ],
  declarations: [SourcePage],
})
export class SourcePageModule {}
