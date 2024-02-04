import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AboutUntoldNotesPage } from './about-untold-notes.page';

const routes: Routes = [
  {
    path: '',
    component: AboutUntoldNotesPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AboutUntoldNotesRoutingModule {}
