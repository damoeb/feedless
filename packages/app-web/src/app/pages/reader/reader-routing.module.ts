import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ReaderPage } from './reader.page';

const routes: Routes = [
  {
    path: '',
    component: ReaderPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ReaderPageRoutingModule {}
