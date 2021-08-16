import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { FolderPageRoutingModule } from './folder-routing.module';

import { FolderPage } from './folder.page';
import { BaseModule } from '../../components/base/base.module';
import { ToolbarModule } from '../../components/toolbar/toolbar.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    FolderPageRoutingModule,
    ToolbarModule,
    BaseModule,
    ToolbarModule,
  ],
  declarations: [FolderPage],
})
export class FolderPageModule {}
