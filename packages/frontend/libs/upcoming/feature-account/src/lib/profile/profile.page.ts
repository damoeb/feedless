import { Component, ChangeDetectionStrategy } from '@angular/core';
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
 
import { DarkModeButtonComponent, ProfileButtonComponent } from '@feedless/components';
import { upcomingBaseRoute } from '@feedless/upcoming-util';
import { template } from 'typesafe-routes';

@Component({
  selector: 'app-profile-page',
  templateUrl: './profile.page.html',
  styleUrls: ['./profile.page.scss'],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.Eager,
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
