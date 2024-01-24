import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { RepositoryPluginsPageRoutingModule } from './repository-plugins-routing.module';

import { RepositoryPluginsPage } from './repository-plugins.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    RepositoryPluginsPageRoutingModule,
  ],
  declarations: [RepositoryPluginsPage]
})
export class RepositoryPluginsPageModule {
}
