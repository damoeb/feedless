import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { RepositoriesPageRoutingModule } from './repositories-routing.module';

import { RepositoriesPage } from './repositories.page';
import { BubbleModule } from '../../components/bubble/bubble.module';
import { ExportModalModule } from '../../modals/export-modal/export-modal.module';
import { SearchbarModule } from '../../elements/searchbar/searchbar.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    RepositoriesPageRoutingModule,
    BubbleModule,
    ReactiveFormsModule,
    ExportModalModule,
    SearchbarModule,
  ],
  declarations: [RepositoriesPage],
})
export class RepositoriesPageModule {}
