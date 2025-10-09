import { Routes } from '@angular/router';

import { DefaultRoutes } from '../default-routes';
import { AuthGuardService } from '../../guards/auth-guard.service';

export const VISUAL_DIFF_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./visual-diff-product.page').then((m) => m.VisualDiffProductPage),
    children: [
      {
        path: '',
        data: { title: '' },
        loadComponent: () =>
          import('./about/about-visual-diff.page').then((m) => m.AboutVisualDiffPage),
      },
      {
        path: 'tracker',
        // canActivate: [AuthGuardService],
        loadChildren: () =>
          import('../../pages/tracker-edit/tracker-edit.routes').then((m) => m.TRACKER_EDIT_ROUTES),
      },
      {
        path: 'agents',
        canActivate: [AuthGuardService],
        loadChildren: () => import('../../pages/agents/agents.routes').then((m) => m.AGENTS_ROUTES),
      },
      ...DefaultRoutes,
      {
        path: '**',
        redirectTo: '/',
      },
    ],
  },
];
