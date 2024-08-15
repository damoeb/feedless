import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { IonicModule } from '@ionic/angular';
import { ProductHeadlineModule } from '../product-headline/product-headline.module';
import { RepositoriesDirectoryComponent } from './repositories-directory.component';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { ProductHeaderModule } from '../product-header/product-header.module';
import { RouterLink } from '@angular/router';

@NgModule({
  imports: [
    CommonModule,
    IonicModule,
    ProductHeadlineModule,
    SearchbarModule,
    ProductHeaderModule,
    RouterLink,
  ],
  declarations: [RepositoriesDirectoryComponent],
  exports: [RepositoriesDirectoryComponent],
})
export class RepositoriesDirectoryModule {}
