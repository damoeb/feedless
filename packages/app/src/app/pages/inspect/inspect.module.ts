import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { InspectPageRoutingModule } from './inspect-routing.module';

import { InspectPage } from './inspect.page';
import { InspectionModule } from '../../components/inspection/inspection.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    InspectionModule,
    InspectPageRoutingModule,
  ],
  declarations: [InspectPage],
})
export class InspectPageModule {}
