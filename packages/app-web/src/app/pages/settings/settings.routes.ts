import { Routes } from '@angular/router';
import { SettingsPage } from './settings.page';

export const SETTINGS_ROUTES: Routes = [
  {
    path: '**',
    component: SettingsPage,
  },
];
