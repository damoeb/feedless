import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
})
export class AppComponent {
  public appPages = [
    { title: 'Buckets', url: '/buckets' },
    { title: 'Feeds', url: '/feeds' },
    { title: 'Settings', url: '/settings' },
    { title: 'Profile', url: '/profile' },
  ];
  constructor() {}
}
