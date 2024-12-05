import { Routes } from '@angular/router';
import { LicensePage } from './license.page';

export const LICENSE_ROUTES: Routes = [
  {
    path: '**',
    component: LicensePage,
  },
];
