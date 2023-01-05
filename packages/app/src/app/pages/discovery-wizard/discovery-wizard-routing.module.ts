import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { DiscoveryWizardPage } from './discovery-wizard.page';

const routes: Routes = [
  {
    path: '',
    component: DiscoveryWizardPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DiscoveryWizardPageRoutingModule {}
