import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { RepositorySourcesPageRoutingModule } from './repository-sources-routing.module';

import { RepositorySourcesPage } from './repository-sources.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    RepositorySourcesPageRoutingModule,
  ],
  declarations: [RepositorySourcesPage],
})
export class RepositorySourcesPageModule {}
