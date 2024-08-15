import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TrackerEditPage } from './tracker-edit.page';

const routes: Routes = [
  {
    path: '',
    component: TrackerEditPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class TrackerEditRoutingModule {}
