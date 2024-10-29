import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { UpcomingProductRoutingModule } from './upcoming-product-routing.module';

import { UpcomingProductPage } from './upcoming-product-page.component';
import { DarkModeButtonModule } from '../../components/dark-mode-button/dark-mode-button.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { BubbleModule } from '../../components/bubble/bubble.module';
import { MapModule } from '../../components/map/map.module';
import { MapModalModule } from '../../modals/map-modal/map-modal.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    UpcomingProductRoutingModule,
    DarkModeButtonModule,
    SearchbarModule,
    BubbleModule,
    MapModule,
    MapModalModule,
    ReactiveFormsModule,
  ],
  declarations: [UpcomingProductPage],
})
export class UpcomingProductModule {}
