import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {FolderPage} from './folder.page';

const routes: Routes = [
  {
    path: '',
    component: FolderPage
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class FolderPageRoutingModule {
}
