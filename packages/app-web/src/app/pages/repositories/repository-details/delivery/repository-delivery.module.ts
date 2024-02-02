import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { RepositoryDeliveryPageRoutingModule } from './repository-delivery-routing.module';

import { RepositoryDeliveryPage } from './repository-delivery.page';
import { EmptyRepositoryModule } from '../../../../components/empty-repository/empty-repository.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    RepositoryDeliveryPageRoutingModule,
    EmptyRepositoryModule,
  ],
  declarations: [RepositoryDeliveryPage],
})
export class RepositoryDeliveryPageModule {}
