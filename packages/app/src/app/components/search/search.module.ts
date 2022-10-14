import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { SearchPageRoutingModule } from './search-routing.module';

import { SearchPage } from './search.page';
import { PaginatedModule } from '../paginated/paginated.module';

@NgModule({
  imports: [CommonModule, FormsModule, IonicModule, SearchPageRoutingModule, PaginatedModule],
  declarations: [SearchPage],
})
export class SearchPageModule {}
