import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { ConnectAppPageRoutingModule } from './connect-app-routing.module';

import { ConnectAppPage } from './connect-app.page';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    ConnectAppPageRoutingModule,
  ],
  declarations: [ConnectAppPage],
})
export class LinkAppPageModule {}
