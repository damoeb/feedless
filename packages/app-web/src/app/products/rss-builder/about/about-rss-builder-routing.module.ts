import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AboutRssBuilderPage } from './about-rss-builder.page';

const routes: Routes = [
  {
    path: '',
    component: AboutRssBuilderPage,
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AboutRssBuilderRoutingModule {
}
