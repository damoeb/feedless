import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { RepositoryDataPageRoutingModule } from './repository-data-routing.module';

import { RepositoryDataPage } from './repository-data.page';
import { EmptyRepositoryModule } from '../../../../components/empty-repository/empty-repository.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    RepositoryDataPageRoutingModule,
    EmptyRepositoryModule
  ],
  declarations: [RepositoryDataPage]
})
export class RepositoryDataPageModule {
}
