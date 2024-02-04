import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { ReaderProductRoutingModule } from './reader-product-routing.module';

import { ReaderProductPage } from './reader-product.page';
import { ReaderModule } from '../../components/reader/reader.module';
import { EmbeddedWebsiteModule } from '../../components/embedded-website/embedded-website.module';
import { DarkModeButtonModule } from '../../components/dark-mode-button/dark-mode-button.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { ReaderMenuModule } from './reader-menu/reader-menu.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReaderMenuModule,
    IonicModule,
    ReaderProductRoutingModule,
    ReaderModule,
    EmbeddedWebsiteModule,
    DarkModeButtonModule,
    SearchbarModule,
  ],
  declarations: [ReaderProductPage],
})
export class ReaderProductModule {}
