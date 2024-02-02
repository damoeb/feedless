import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AboutFeedlessPage } from './about-feedless.page';

const routes: Routes = [
  {
    path: '',
    component: AboutFeedlessPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AboutFeedlessRoutingModule {}
