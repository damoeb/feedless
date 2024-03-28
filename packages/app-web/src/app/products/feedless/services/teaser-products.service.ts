import { Injectable } from '@angular/core';

export type ProductTeaser = {
  localSetup: string;
  id: string
  title: string
  imageUrl: string
  subtitle: string
  body: string
  available: boolean
  costs: number,
  features: string[]
}

@Injectable()
export class TeaserProductsService {

  getProducts(): ProductTeaser[] {
    return [
      {
        id: 'rss-proxy',
        title: 'RSS proxy',
        subtitle: 'RSS Feed Builder',
        imageUrl: '/assets/rss-proxy.jpeg',
        body: 'Create an ATOM or JSON feed of any website or feeds (web to feed), just by analyzing just the HTML structure and filter them.',
        available: true,
        costs: 29.99,
        features: [
          'Web to Feed',
          'Filters',
          'Custom feed parser rules',
          'Pre-rendering in chromium',
          'Self Hosting',
          'Source Available'
        ],
        localSetup: ''
      },
      {
        id: 'visual-diff',
        title: 'Visual Diff',
        subtitle: 'Page Change Tracker',
        imageUrl: '/assets/visualdiff.jpeg',
        body: 'Detect changes in a website based on image, markup or text and get a notification via mail or feed.',
        available: true,
        costs: 49.99,
        features: [
          'Page change tracking',
          'Track pixel, markup or text',
          'Customizable trigger threshold',
          'Support full page and page fragment',
          'Prerendering in chromium',
          'Email notifications',
          'Self Hosting'
        ],
        localSetup: ''
      },
      // {
      //   id: 'untold',
      //   title: 'Untold Notes',
      //   subtitle: 'connected notes',
      //   imageUrl: '/assets/untold.jpeg',
      //   body: 'Here\'s a small text description for the card content. Nothing more, nothing less.',
      //   available: false
      // },
      // {
      //   id: 'upcoming',
      //   title: 'upcoming',
      //   subtitle: 'Localized Event Sourcing',
      //   imageUrl: '/assets/upcoming.jpeg',
      //   body: 'Here\'s a small text description for the card content. Nothing more, nothing less.',
      //   available: false
      // },
      {
        id: 'reader',
        title: 'Reader',
        subtitle: 'Reader',
        imageUrl: '/assets/reader.jpeg',
        body: 'Unclutter a website and transform it into a version optimized for reading',
        available: true,
        costs: 0,
        features: [
          'Font',
          'Text Size',
          'Text Alignment',
          'Bionic Font'
        ],
        localSetup: ''
      }
      // {
      //   id: 'feedless',
      //   title: 'feedless',
      //   subtitle: 'Reader',
      //   imageUrl: '/assets/feedless.jpeg',
      //   body: 'Here\'s a small text description for the card content. Nothing more, nothing less.',
      //   available: false
      // }
    ];
  }
}
