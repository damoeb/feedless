import { RootJson } from '../src/services/rich-json/rich-json.service';

export const sourcesRichJson: RootJson = {
  buckets: [
    {
      title: 'People',
      visibility: 'private',
      subscriptions: [
        {
          tags: ['Michael Malice'],
          htmlUrl: 'https://michaelmalice.com/',
        },
        {
          tags: ['Robert Schoch'],
          htmlUrl: 'https://www.robertschoch.com',
        },
        {
          tags: ['Michael Lueders'],
          htmlUrl: 'https://michael-lueders.de/',
        },
        {
          tags: ['Philip Huebl'],
          htmlUrl: 'https://philipphuebl.com/en/home-en/',
        },
        {
          tags: ['Daniel Dennett'],
          title: 'Daniel Dennett Blog',
          htmlUrl: 'https://ase.tufts.edu/cogstud/dennett/index.html',
        },
        // {
        //   tags: ['Daniel Dennett'],
        //   title: 'Daniel Dennett Twitter',
        //   htmlUrl: 'https://twitter.com/danieldennett',
        // },
        // {
        //   tags: ['Daniel Dennett'],
        //   title: 'Daniel Dennett Search',
        //   query: 'daniel dennet',
        // },
      ],
      pipeline: [
        {
          map: 'filter',
          context: 'linkCount > 1',
        },
        {
          map: 'tag',
          context: 'People',
        },
      ],
      exporters: [
        {
          trigger: {
            on: 'scheduled',
            expression: 'every monday 17:00',
          },
          segment: {
            sortField: 'score',
            sortAsc: false,
            size: 20,
            digest: false,
          },
          targets: [
            {
              type: 'feed',
            },
          ],
        },
      ],
    },
    {
      title: 'Kids Audios/Videos',
      visibility: 'public',
      subscriptions: [
        {
          title: 'Heidi',
          xmlUrl: 'https://www.zdf.de/rss/zdf/kinder/heidi',
          harvest: false,
          tags: ['Kinder', 'Video'],
        },
        {
          title: 'Petterson und Findus',
          xmlUrl: 'https://www.zdf.de/rss/zdf/kinder/pettersson-und-findus',
          harvest: false,
        },
        {
          title: 'Pip und Posy',
          htmlUrl:
            'https://www.kika.de/pip-und-posy/sendungen/videos-pip-und-posy-100.html',
          harvest: false,
        },
      ],
      // pipeline: [
      //   {
      //     map: 'yt',
      //     context: JSON.stringify({ format: '720p' }),
      //   },
      // ],
      exporters: [
        {
          targets: [
            {
              type: 'feed',
              // id: 'inherited',
              forward_errors: true,
            },
          ],
        },
      ],
    },
    // {
    //   title: 'Watch pages',
    //   visibility: 'private',
    //   subscriptions: [
    //     {
    //       title: 'Aktionen @ALDI',
    //       htmlUrlBuilder: goTo(
    //         'https://www.aldi-suisse.ch/de/aktionen/aktuelle-aktionen-angebote/',
    //         { prefetch: true },
    //       )
    //         .findLinkMatching(
    //           'https://www.aldi-suisse.ch/de/aktionen/aktionen-ab-*',
    //         )
    //         .click()
    //         .setValue('#select', 'Windows')
    //         .waitFor(),
    //       retention_size: 1,
    //       harvest_with_prerender: true,
    //       harvest_site: true,
    //     },
    //   ],
    //   exporters: [
    //     {
    //       targets: [
    //         {
    //           type: 'feed',
    //         },
    //       ],
    //     },
    //   ],
    // },
  ],
};
