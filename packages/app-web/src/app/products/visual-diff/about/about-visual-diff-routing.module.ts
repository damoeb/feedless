import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AboutVisualDiffPage } from './about-visual-diff.page';

const routes: Routes = [
  {
    path: '',
    component: AboutVisualDiffPage,
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AboutVisualDiffPageRoutingModule {
}
