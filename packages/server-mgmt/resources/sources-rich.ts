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
    // {
    //   title: 'People',
    //   visibility: 'private',
    //   subscriptions: [
    //     {
    //       tags: ['Michael Malice'],
    //       htmlUrl: 'https://michaelmalice.com/',
    //       retention_size: 10,
    //     },
    //     {
    //       tags: ['Robert Schoch'],
    //       htmlUrl: 'https://www.robertschoch.com',
    //     },
    //     {
    //       tags: ['Michael Lueders'],
    //       htmlUrl: 'https://michael-lueders.de/',
    //     },
    //     {
    //       tags: ['Philip Huebl'],
    //       htmlUrl: 'https://philipphuebl.com/en/home-en/',
    //     },
    //     {
    //       tags: ['Daniel Dennett'],
    //       title: 'Daniel Dennett Blog',
    //       htmlUrl: 'https://ase.tufts.edu/cogstud/dennett/index.html',
    //     },
    //     {
    //       tags: ['Daniel Dennett'],
    //       title: 'Daniel Dennett Twitter',
    //       htmlUrl: 'https://twitter.com/danieldennett',
    //       filter: 'linkCount > 0',
    //     },
    //     {
    //       tags: ['Daniel Dennett'],
    //       title: 'Daniel Dennett Search',
    //       query: 'daniel dennet',
    //     },
    //   ],
    //   pipeline: [
    //     {
    //       map: 'filter',
    //       context: 'linkCount > 0',
    //     },
    //     {
    //       map: 'tag',
    //       context: JSON.stringify(['People']),
    //     },
    //   ],
    //   exporters: [
    //     {
    //       trigger: {
    //         on: 'change',
    //         // expression: 'every monday 17:00',
    //       },
    //       // segment: {
    //       //   sortField: 'score',
    //       //   sortAsc: false,
    //       //   size: 20,
    //       //   digest: false,
    //       // },
    //       targets: [
    //         {
    //           type: 'feed',
    //         },
    //       ],
    //     },
    //   ],
    // },
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
    //       tags: ['OxfordUnion'],
    //       htmlUrl: 'https://www.youtube.com/channel/UCY7dD6waquGnKTZSumPMTlQ',
    //     },
    //     {
    //       tags: ['Luke Smith'],
    //       xmlUrl: 'https://videos.lukesmith.xyz/feeds/videos.atom?sort=-publishedAt',
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
      title: 'Zürich Veranstaltungen',
      visibility: 'public',
      subscriptions: [
        {
          xmlUrl: 'http://localhost:8080/api/web-to-feed?version=0.1&url=https%3A%2F%2Fwww.stadt-zuerich.ch%2Fportal%2Fde%2Findex%2Faktuelles%2Fagenda.html&linkXPath=.%2Fdiv%5B1%5D%2Fa%5B1%5D&extendContext=&contextXPath=%2F%2Fmain%5B1%5D%2Fdiv%5B4%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B2%5D%2Fdiv%5B2%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv',
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
        {
          trigger: {
            on: 'scheduled',
            expression: 'every day 19:00'
          },
          segment: {
            sortField: 'date_published',
            // shift: '1d',
            size: 10
          },
          targets: [
            {
              type: 'push',
            },
          ],
        }
      ],
    },
    // {
    //   title: 'Music',
    //   visibility: 'public',
    //   subscriptions: [
    //     {
    //       tags: ['Sounds!'],
    //       htmlUrl: 'https://www.srf.ch/audio/sounds',
    //     },
    //     {
    //       htmlUrl: 'https://soundcloud.com/damoeb/likes',
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
    // {
    //   title: 'Podcasts',
    //   visibility: 'public',
    //   subscriptions: [
    //     {
    //       tags: ['Future of Life'],
    //       htmlUrl: 'https://futureoflife.org/the-future-of-life-podcast/',
    //     },
    //     {
    //       tags: ['After On'],
    //       htmlUrl: 'https://after-on.com',
    //     },
    //     {
    //       tags: ['Mindscape'],
    //       htmlUrl: 'https://www.preposterousuniverse.com/',
    //     },
    //     {
    //       tags: ['Universe Today'],
    //       htmlUrl: 'https://www.universetoday.com/',
    //     },
    //     {
    //       tags: ['Radiolab'],
    //       htmlUrl: 'https://www.wnycstudios.org/podcasts/radiolab/',
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
    // {
    //   title: 'Kids Audios/Videos',
    //   visibility: 'public',
    //   subscriptions: [
    //     {
    //       title: 'Heidi',
    //       xmlUrl: 'https://www.zdf.de/rss/zdf/kinder/heidi',
    //       harvest: false,
    //       tags: ['Kinder', 'Video'],
    //     },
    //     {
    //       title: 'Lassie',
    //       xmlUrl: 'https://www.zdf.de/rss/zdf/kinder/lassie',
    //       harvest: false,
    //       tags: ['Kinder', 'Video'],
    //     },
    //     {
    //       title: 'Petterson und Findus',
    //       xmlUrl: 'https://www.zdf.de/rss/zdf/kinder/pettersson-und-findus',
    //       harvest: false,
    //     },
    //     {
    //       title: 'Pip und Posy',
    //       xmlUrl:
    //         'http://localhost:8080/api/web-to-feed?version=0.1&url=https%3A%2F%2Fwww.kika.de%2Fpip-und-posy%2Fsendungen%2Fvideos-pip-und-posy-100.html&linkXPath=.%2Fdiv%5B1%5D%2Fdiv%5B2%5D%2Fspan%5B2%5D%2Fh4%5B1%5D%2Fa%5B1%5D&extendContext=&contextXPath=%2F%2Fdiv%5B6%5D%2Fdiv%5B1%5D%2Fdiv%5B3%5D%2Fdiv%5B1%5D%2Fdiv%5B3%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv',
    //       harvest: false,
    //     },
    //     {
    //       title: 'ZDFchen',
    //       xmlUrl:
    //         'https://www.zdf.de/rss/zdf/kinder/zdfchen-filme',
    //       harvest: false,
    //     },
    //   ],
    //   pipeline: [
    //     {
    //       map: 'yt',
    //       context: JSON.stringify({ format: '720p', manual: false }),
    //     },
    //   ],
    //   exporters: [
    //     {
    //       targets: [
    //         {
    //           type: 'feed',
    //           // id: 'inherited',
    //         },
    //       ],
    //     },
    //   ],
    // },
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
