import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FeedBuilderPage } from './feed-builder.page';

const routes: Routes = [
  {
    path: '',
    component: FeedBuilderPage
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FeedBuilderPageRoutingModule {
}
