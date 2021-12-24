import { RootJson } from '../src/services/rich-json/rich-json.service';

export const sourcesRichJson: RootJson = {
  plugins: [
    {
      type: 'subscriptions',
      params: {
        userId: 'pedro1976',
        bucket: 'hn',
      },
      specification: {
        output: {
          urls: [
            'https://news.ycombinator.com/submitted?id=${params.userId}',
            'https://news.ycombinator.com/favorites?id=${params.userId}',
            'https://news.ycombinator.com/upvoted?id=${params.userId}',
          ],
          feedItemUrlsLike: 'https://news.ycombinator.com/user',
          then: {
            urls: ['https://news.ycombinator.com/favorites?id=${url.id}'],
            linkXPath: './td[1]/table[1]/tbody[1]/tr/td/span/a[1]',
            extendContext: 'n',
            contextXPath: '//center[1]/table[1]/tbody[1]/tr',
            exclude: [encodeURIComponent('?id=${params.userId}'), '/newest'],
          },
        },
      },
    },
    // {
    //   type: 'subscriptions',
    //   params: { userId: 'damoeb', bucket: 'twitter' },
    //   output: {
    //     feeds: ['https://twitter.com/${userId}/following'],
    //     feedItemUrlsLike: 'https://twitter.com/([.*])',
    //     then: {
    //       feeds: [
    //         'https://news.ycombinator.com/submitted?id=$1',
    //         'https://news.ycombinator.com/favorites?id=$1',
    //         'https://news.ycombinator.com/upvoted?id=$1',
    //       ],
    //       feedItemUrlsLike: 'https://news.ycombinator.com/user?id=([.*])',
    //     },
    //   },
    // },
  ],

  buckets: [
    {
      title: 'People',
      visibility: 'private',
      subscriptions: [
        {
          tags: ['Michael Malice'],
          htmlUrl: 'https://michaelmalice.com/',
          retention_size: 10,
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
        {
          tags: ['Daniel Dennett'],
          title: 'Daniel Dennett Twitter',
          htmlUrl: 'https://twitter.com/danieldennett',
          filter: 'linkCount > 0',
        },
        {
          tags: ['Daniel Dennett'],
          title: 'Daniel Dennett Search',
          query: 'daniel dennet',
        },
      ],
      pipeline: [
        {
          map: 'filter',
          context: 'linkCount > 0',
        },
        {
          map: 'tag',
          context: JSON.stringify(['People']),
        },
      ],
      exporters: [
        {
          trigger: {
            on: 'change',
            // expression: 'every monday 17:00',
          },
          // segment: {
          //   sortField: 'score',
          //   sortAsc: false,
          //   size: 20,
          //   digest: false,
          // },
          targets: [
            {
              type: 'feed',
            },
          ],
        },
      ],
    },
    // {
    //   title: 'Inspiration',
    //   visibility: 'public',
    //   subscriptions: [
    //     {
    //       tags: ['Moonlight MVP'],
    //       htmlUrl: 'https://www.youtube.com/channel/UCc8_dv1ysWAo4LzyJH3Xuww//',
    //     },
    //     {
    //       tags: ['citizenlab'],
    //       htmlUrl: 'https://www.citizenlab.co/blog/',
    //     },
    //     {
    //       tags: ['Undecided with Matt Ferrell'],
    //       htmlUrl: 'https://www.youtube.com/channel/UCjtUS7-SZTi6pXjUbzGHQCg',
    //     },
    //     {
    //       tags: [' OxfordUnion '],
    //       htmlUrl: 'https://www.youtube.com/channel/UCY7dD6waquGnKTZSumPMTlQ',
    //     },
    //   ],
    //   exporters: [
    //     {
    //       trigger: {
    //         on: 'change',
    //       },
    //       targets: [
    //         {
    //           type: 'feed',
    //         },
    //       ],
    //     },
    //   ],
    // },
    {
      title: 'Music',
      visibility: 'public',
      subscriptions: [
        {
          tags: ['Sounds!'],
          htmlUrl: 'https://www.srf.ch/audio/sounds',
        },
        {
          htmlUrl: 'https://soundcloud.com/damoeb/likes',
        },
      ],
      exporters: [
        {
          trigger: {
            on: 'change',
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
      title: 'Podcasts',
      visibility: 'public',
      subscriptions: [
        {
          tags: ['Future of Life'],
          htmlUrl: 'https://futureoflife.org/the-future-of-life-podcast/',
        },
        {
          tags: ['After On'],
          htmlUrl: 'https://after-on.com',
        },
        {
          tags: ['Mindscape'],
          htmlUrl: 'https://www.preposterousuniverse.com/',
        },
        {
          tags: ['Universe Today'],
          htmlUrl: 'https://www.universetoday.com/',
        },
        {
          tags: ['Radiolab'],
          htmlUrl: 'https://www.wnycstudios.org/podcasts/radiolab/',
        },
      ],
      exporters: [
        {
          trigger: {
            on: 'change',
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
          title: 'Lassie',
          xmlUrl: 'https://www.zdf.de/rss/zdf/kinder/lassie',
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
      pipeline: [
        {
          map: 'yt',
          context: JSON.stringify({ format: '720p' }),
        },
      ],
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
