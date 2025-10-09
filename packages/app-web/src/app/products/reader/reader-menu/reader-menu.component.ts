import { Component } from '@angular/core';
import { IonItem, IonLabel, IonList, IonListHeader } from '@ionic/angular/standalone';

@Component({
  selector: 'app-reader-menu',
  templateUrl: './reader-menu.component.html',
  styleUrls: ['./reader-menu.component.scss'],
  imports: [IonList, IonListHeader, IonItem, IonLabel],
  standalone: true,
})
export class ReaderMenuComponent {
  constructor() {}
}
