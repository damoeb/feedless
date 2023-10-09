import { Component } from '@angular/core';

interface AppPage {
  title: string;
  url: string;
  color?: string;
}

@Component({
  selector: 'app-feedless-menu',
  templateUrl: './feedless-menu.component.html',
  styleUrls: ['./feedless-menu.component.scss'],
})
export class FeedlessMenuComponent {
  public appPages: AppPage[] = [
    {
      title: 'Feeds',
      url: '/buckets',
    },
    {
      title: 'Agents',
      url: '/agents',
    },
    // {
    //   title: 'Filter',
    //   url: '/buckets',
    // },
    // {
    //   title: 'Fulltext',
    //   icon: 'document-text-outline',
    //   url: '/buckets',
    // },
  ];

  constructor() {}
}
