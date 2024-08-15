import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { VisualDiffProductPage } from './visual-diff-product.page';
import { DefaultRoutes } from '../default-routes';
import { AuthGuardService } from '../../guards/auth-guard.service';

const routes: Routes = [
  {
    path: '',
    component: VisualDiffProductPage,
    children: [
      {
        path: '',
        data: { title: '' },
        loadChildren: () =>
          import('./about/about-visual-diff.module').then(
            (m) => m.AboutVisualDiffModule,
          ),
      },
      {
        path: 'tracker',
        // canActivate: [AuthGuardService],
        loadChildren: () =>
          import('../../pages/tracker-edit/tracker-edit.module').then(
            (m) => m.TrackerEditPageModule,
          ),
      },
      {
        path: 'agents',
        canActivate: [AuthGuardService],
        loadChildren: () =>
          import('../../pages/agents/agents.module').then(
            (m) => m.AgentsPageModule,
          ),
      },
      ...DefaultRoutes,
      {
        path: '**',
        redirectTo: '/',
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class VisualDiffProductRoutingModule {}
