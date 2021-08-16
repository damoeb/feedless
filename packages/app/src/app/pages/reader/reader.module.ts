import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';

import { ReaderPage } from './reader.page';
import { RouterModule } from '@angular/router';
import { ToolbarModule } from '../../components/toolbar/toolbar.module';
import { ReaderPageRoutingModule } from './reader-routing.module';
import { BaseModule } from '../../components/base/base.module';

@NgModule({
  declarations: [ReaderPage],
  exports: [ReaderPage],
  imports: [
    CommonModule,
    ReaderPageRoutingModule,
    IonicModule,
    RouterModule,
    ToolbarModule,
    BaseModule,
  ],
})
export class ReaderPageModule {}
