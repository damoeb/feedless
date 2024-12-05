import { Routes } from '@angular/router';
import { ConnectAppPage } from './connect-app.page';

export const CONNECT_APP_ROUTES: Routes = [
  {
    path: '',
    component: ConnectAppPage,
  },
  {
    path: ':link',
    component: ConnectAppPage,
  },
];
