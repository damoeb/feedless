import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { NxWelcome } from './nx-welcome';
import { IonApp, IonRouterOutlet } from '@ionic/angular/standalone';

@Component({
  imports: [NxWelcome, RouterModule, IonApp, IonRouterOutlet],
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected title = 'upcoming';
}
