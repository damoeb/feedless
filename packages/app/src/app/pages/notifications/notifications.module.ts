import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { NotificationsPageRoutingModule } from './notifications-routing.module';

import { NotificationsPage } from './notifications.page';
import { PageHeaderModule } from '../../components/page-header/page-header.module';
import { ArticlesModule } from '../../components/articles/articles.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    NotificationsPageRoutingModule,
    PageHeaderModule,
    ArticlesModule,
  ],
  declarations: [NotificationsPage],
})
export class NotificationsPageModule {}
