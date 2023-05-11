import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { IonicModule } from '@ionic/angular';

import { NotificationsPageRoutingModule } from './notifications-routing.module';

import { NotificationsPage } from './notifications.page';
import { PageHeaderModule } from '../../components/page-header/page-header.module';
import { ArticlesModule } from '../../components/articles/articles.module';
import { ExternalLinkModule } from '../../components/external-link/external-link.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    IonicModule,
    NotificationsPageRoutingModule,
    PageHeaderModule,
    ArticlesModule,
    ExternalLinkModule,
  ],
  declarations: [NotificationsPage],
})
export class NotificationsPageModule {}
