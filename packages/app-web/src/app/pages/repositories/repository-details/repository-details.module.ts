import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { RepositoryDetailsPageRoutingModule } from './repository-details-routing.module';

import { RepositoryDetailsPage } from './repository-details.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    RepositoryDetailsPageRoutingModule,
  ],
  declarations: [RepositoryDetailsPage],
})
export class RepositoryDetailsPageModule {}
