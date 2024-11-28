import { Routes } from '@angular/router';

export const TRACKER_EDIT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./tracker-edit.page').then((m) => m.TrackerEditPage),
  },
];
