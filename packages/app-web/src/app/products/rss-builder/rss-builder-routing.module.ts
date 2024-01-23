import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { RssBuilderPage } from './rss-builder.page';
import { ProductService } from '../../services/product.service';
import { FeedBuilderPage } from './feed-builder/feed-builder.page';
import { AboutRssBuilderPage } from './about/about-rss-builder.page';

const routes: Routes = [
  {
    path: '',
    component: RssBuilderPage,
    children: [
      {
        path: '',
        component: AboutRssBuilderPage,
      },
      {
        path: 'builder',
        component: FeedBuilderPage,
      }
    ]
  },
  ...ProductService.defaultRoutes
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class RssBuilderPageRoutingModule {}
