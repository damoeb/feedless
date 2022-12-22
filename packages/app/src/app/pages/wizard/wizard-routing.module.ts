import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { WizardPage } from './wizard.page';

const routes: Routes = [
  {
    path: '',
    component: WizardPage
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class WizardPageRoutingModule {}
