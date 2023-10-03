import { Component } from '@angular/core';

interface AppPage {
  title: string;
  url: string;
  icon: string;
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
      title: 'Create',
      url: '/buckets',
      icon: 'create-outline',
    },
    {
      title: 'Merge',
      icon: 'git-merge-outline',
      url: '/buckets',
    },
    {
      title: 'Filter',
      icon: 'filter-outline',
      url: '/buckets',
    },
    {
      title: 'Fulltext',
      icon: 'document-text-outline',
      url: '/buckets',
    },
  ];

  constructor() {}
}
