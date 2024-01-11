import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { ReaderPageRoutingModule } from './reader-routing.module';

import { ReaderPage } from './reader.page';
import { ReaderModule } from '../../components/reader/reader.module';
import { PageHeaderModule } from '../../components/page-header/page-header.module';
import { EmbeddedWebsiteModule } from '../../components/embedded-website/embedded-website.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ReaderPageRoutingModule,
    ReaderModule,
    PageHeaderModule,
    EmbeddedWebsiteModule,
  ],
  declarations: [ReaderPage],
})
export class ReaderPageModule {}
