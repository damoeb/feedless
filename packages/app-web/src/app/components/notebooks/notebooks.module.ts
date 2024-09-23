import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotebooksComponent } from './notebooks.component';
import { IonicModule } from '@ionic/angular';
import { FormsModule } from '@angular/forms';
import { ProductHeaderModule } from '../product-header/product-header.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';
import { RouterLink } from '@angular/router';

@NgModule({
  declarations: [NotebooksComponent],
  exports: [NotebooksComponent],
  imports: [
    CommonModule,
    IonicModule,
    FormsModule,
    ProductHeaderModule,
    SearchbarModule,
    RouterLink,
  ],
})
export class NotebooksModule {}
