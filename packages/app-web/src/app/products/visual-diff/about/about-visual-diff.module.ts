import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';
import { AboutVisualDiffPage } from './about-visual-diff.page';
import { SearchbarModule } from '../../../elements/searchbar/searchbar.module';
import { ProductHeadlineModule } from '../../../components/product-headline/product-headline.module';
import { AboutVisualDiffPageRoutingModule } from './about-visual-diff-routing.module';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    ReactiveFormsModule,
    AboutVisualDiffPageRoutingModule,
    SearchbarModule,
    ProductHeadlineModule
  ],
  declarations: [AboutVisualDiffPage]
})
export class AboutVisualDiffModule {
}
