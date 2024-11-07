import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { EventsPage } from './events.page';

const routes: Routes = [
  {
    path: '',
    component: EventsPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class EventsRoutingModule {}
