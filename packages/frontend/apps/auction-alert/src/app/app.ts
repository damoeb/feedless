import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterModule } from '@angular/router';
import { IonApp } from '@ionic/angular/standalone';

@Component({
  imports: [RouterModule, IonApp],
  selector: 'app-root',
  templateUrl: './app.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './app.scss',
})
export class App {
  protected title = 'auction-alert';
}
