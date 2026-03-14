import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonItem,
  IonLabel,
  IonList,
  IonListHeader,
  IonMenu,
  IonMenuButton,
  IonSplitPane,
  IonToolbar,
} from '@ionic/angular/standalone';
// eslint-disable-next-line @nx/enforce-module-boundaries
import {
  DarkModeButtonComponent,
  ProfileButtonComponent,
} from '@feedless/components';
import { upcomingBaseRoute } from '../../upcoming-product-routes';
import { template } from 'typesafe-routes';

@Component({
  selector: 'app-profile-page',
  templateUrl: './profile.page.html',
  styleUrls: ['./profile.page.scss'],
  standalone: true,
  imports: [
    IonHeader,
    IonToolbar,
    IonButtons,
    IonButton,
    IonMenuButton,
    IonContent,
    IonList,
    IonListHeader,
    IonItem,
    IonLabel,
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
    DarkModeButtonComponent,
    ProfileButtonComponent,
    IonSplitPane,
    IonMenu,
  ],
})
export class ProfilePage {
  routes = upcomingBaseRoute;
  tmpl = template;
}
