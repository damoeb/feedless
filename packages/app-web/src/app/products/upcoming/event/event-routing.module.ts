import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { EventPage } from './event.page';

const routes: Routes = [
  {
    path: '',
    component: EventPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class EventRoutingModule {}
