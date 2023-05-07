import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { LinkCliRoutingModule } from './link-cli-routing.module';

import { LinkCliPage } from './link-cli.page';
import { PageHeaderModule } from '../../components/page-header/page-header.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    LinkCliRoutingModule,
    PageHeaderModule,
  ],
  declarations: [LinkCliPage],
})
export class LinkCliPageModule {}
