import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RssBuilderMenuComponent } from './rss-builder-menu.component';
import { IonicModule } from '@ionic/angular';
import { RouterLink, RouterLinkActive } from '@angular/router';

@NgModule({
  declarations: [RssBuilderMenuComponent],
  exports: [RssBuilderMenuComponent],
  imports: [CommonModule, IonicModule, RouterLink, RouterLinkActive],
})
export class RssBuilderMenuModule {}
