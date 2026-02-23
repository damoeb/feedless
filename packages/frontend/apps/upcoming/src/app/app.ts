import { Component } from '@angular/core';
import { RouterModule, RouterOutlet } from '@angular/router';
import { IonApp } from '@ionic/angular/standalone';

@Component({
  imports: [RouterModule, RouterOutlet, IonApp],
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected title = 'upcoming';
}
