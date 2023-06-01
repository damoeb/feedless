import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { GettingStartedPageRoutingModule } from './getting-started-routing.module';

import { GettingStartedPage } from './getting-started.page';
import { PageHeaderModule } from '../../components/page-header/page-header.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    GettingStartedPageRoutingModule,
    PageHeaderModule,
    ReactiveFormsModule,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  declarations: [GettingStartedPage],
})
export class GettingStartedPageModule {}
