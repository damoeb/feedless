import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { IonApp } from '@ionic/angular/standalone';

@Component({
  imports: [RouterModule, IonApp],
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected title = 'feed-reader';
}
