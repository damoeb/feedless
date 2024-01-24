import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { RepositoryCreatePageRoutingModule } from './repository-create-routing.module';

import { RepositoryCreatePage } from './repository-create.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    RepositoryCreatePageRoutingModule,
    ReactiveFormsModule
  ],
  declarations: [RepositoryCreatePage]
})
export class RepositoryCreatePageModule {
}
