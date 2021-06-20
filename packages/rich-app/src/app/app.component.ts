import {Component} from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
})
export class AppComponent {
  public appPages = [
    // {title: 'Inbox', url: '/folder/Inbox', icon: 'mail'},
    // {title: 'Outbox', url: '/folder/Outbox', icon: 'paper-plane'},
    {title: 'Favorites', url: '/folder/Favorites', icon: 'heart', count: 4},
    {title: 'Archived', url: '/folder/Archived', icon: 'archive', count: 0},
    {title: 'Drafts', url: '/folder/Drafts', count: 0}
  ];
  public groupsPages = [
    {title: 'Podcasts', url: '/bucket/d4514e64255f733a', icon: 'lock-closed', count: 0},
    {title: 'Österreich', url: '/bucket/d4514e64255f733b', count: 0 },
    {title: 'Europa', url: '/bucket/d4514e64255f733b', count: 0 },
    {title: 'Russland', url: '/bucket/d4514e64255f733b', count: 0 },
    {title: 'Spanish', url: '/bucket/d4514e64255f733b', count: 0 },
    {title: 'IT', url: '/bucket/d4514e64255f733c', count: 12 },
  ];
  public labels = ['Family', 'Friends', 'Notes', 'Work', 'Travel', 'Reminders'];

  constructor() {
  }
}
