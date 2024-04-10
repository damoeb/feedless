import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AboutPcTrackerPage } from './about-pc-tracker.page';

const routes: Routes = [
  {
    path: '',
    component: AboutPcTrackerPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AboutPcTrackerRoutingModule {}
