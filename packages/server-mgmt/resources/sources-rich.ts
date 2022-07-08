import { RootJson } from '../src/services/rich-json/rich-json.service';
import dayjs, { ManipulateType } from 'dayjs';

function toMinutes(value: number, unit: ManipulateType) {
  return dayjs(0).add(1, unit).toDate().getTime() / (1000 * 60);
}

export const sourcesRichJson: RootJson = {
  // plugins: [
  //   {
  //     type: 'subscriptions',
  //     params: {
  //       userId: 'pedro1976',
  //       bucket: 'hn',
  //     },
  //     specification: {
  //       output: {
  //         urls: [
  //           'https://news.ycombinator.com/submitted?id=${params.userId}',
  //           'https://news.ycombinator.com/favorites?id=${params.userId}',
  //           'https://news.ycombinator.com/upvoted?id=${params.userId}',
  //         ],
  //         feedItemUrlsLike: 'https://news.ycombinator.com/user',
  //         then: {
  //           urls: ['https://news.ycombinator.com/favorites?id=${url.id}'],
  //           linkXPath: './td[1]/table[1]/tbody[1]/tr/td/span/a[1]',
  //           extendContext: 'n',
  //           contextXPath: '//center[1]/table[1]/tbody[1]/tr',
  //           exclude: [encodeURIComponent('?id=${params.userId}'), '/newest'],
  //         },
  //       },
  //     },
  //   },
  // ],

  buckets: [
    // {
    //   title: 'People',
    //   visibility: 'private',
    //   subscriptions: [
    //     {
    //       tags: ['blog'],
    //       htmlUrl: 'https://michaelmalice.com/',
    //       retention_size: 10,
    //     },
    //     {
    //       tags: ['blog'],
    //       htmlUrl: 'https://www.robertschoch.com',
    //     },
    //     {
    //       tags: ['blog'],
    //       htmlUrl: 'https://michael-lueders.de/',
    //     },
    //     {
    //       tags: ['blog'],
    //       htmlUrl: 'https://philipphuebl.com/en/home-en/',
    //     },
    //     {
    //       tags: ['blog'],
    //       title: 'Daniel Dennett Blog',
    //       htmlUrl: 'https://ase.tufts.edu/cogstud/dennett/index.html',
    //     },
    //     {
    //       tags: ['micro'],
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
    //       tags: ['video'],
    //       htmlUrl: 'https://www.youtube.com/channel/UCc8_dv1ysWAo4LzyJH3Xuww//',
    //     },
    //     {
    //       tags: ['blog'],
    //       htmlUrl: 'https://www.citizenlab.co/blog/',
    //     },
    //     {
    //       tags: ['video'],
    //       htmlUrl: 'https://www.youtube.com/channel/UCjtUS7-SZTi6pXjUbzGHQCg',
    //     },
    //     {
    //       tags: ['video'],
    //       htmlUrl: 'https://www.youtube.com/channel/UCY7dD6waquGnKTZSumPMTlQ',
    //     },
    //     {
    //       tags: ['video'],
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
      tags: ['event'],
      subscriptions: [
        {
          xmlUrl: 'http://localhost:8080/api/web-to-feed?version=0.1&url=https%3A%2F%2Fwww.stadt-zuerich.ch%2Fportal%2Fde%2Findex%2Faktuelles%2Fagenda.html&linkXPath=.%2Fdiv%5B1%5D%2Fa%5B1%5D&extendContext=&contextXPath=%2F%2Fmain%5B1%5D%2Fdiv%5B4%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B2%5D%2Fdiv%5B2%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv&dateXPath=.%2Fdiv%5B1%5D%2Fa%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Ftime%5B1%5D',
          harvest: true
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
        // notify the user one day before an event takes place
        {
          trigger: {
            on: 'change',
          },
          segment: {
            lookAheadMin: toMinutes(1, 'day'),
          },
          targets: [
            {
              type: 'push',
            },
          ],
        },
        // deliver a forecast digest
        {
          trigger: {
            on: 'scheduled',
            expression: 'every sunday 22:16'
          },
          segment: {
            sortField: 'score',
            digest: true,
            lookAheadMin: toMinutes(1, 'week'),
            size: 10
          },
          targets: [
            {
              type: 'feed',
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
    //       tags: ['music'],
    //       htmlUrl: 'https://www.srf.ch/audio/sounds',
    //     },
    //     {
    //       tags: ['music'],
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
    //       tags: ['podcast'],
    //       htmlUrl: 'https://futureoflife.org/the-future-of-life-podcast/',
    //     },
    //     {
    //       tags: ['podcast'],
    //       htmlUrl: 'https://after-on.com',
    //     },
    //     {
    //       tags: ['podcast'],
    //       htmlUrl: 'https://www.preposterousuniverse.com/',
    //     },
    //     {
    //       tags: ['podcast'],
    //       htmlUrl: 'https://www.universetoday.com/',
    //     },
    //     {
    //       tags: ['podcast'],
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
    {
      title: 'Kids Audios/Videos',
      visibility: 'public',
      subscriptions: [
        {
          title: 'Heidi',
          xmlUrl: 'https://www.zdf.de/rss/zdf/kinder/heidi',
          harvest: false,
          tags: ['video'],
        },
        {
          title: 'Lassie',
          xmlUrl: 'https://www.zdf.de/rss/zdf/kinder/lassie',
          harvest: false,
          tags: ['video'],
        },
        {
          title: 'Petterson und Findus',
          xmlUrl: 'https://www.zdf.de/rss/zdf/kinder/pettersson-und-findus',
          harvest: false,
          tags: ['video'],
        },
        {
          title: 'Pip und Posy',
          xmlUrl:
            'http://localhost:8080/api/web-to-feed?version=0.1&url=https%3A%2F%2Fwww.kika.de%2Fpip-und-posy%2Fsendungen%2Fvideos-pip-und-posy-100.html&linkXPath=.%2Fdiv%5B1%5D%2Fdiv%5B2%5D%2Fspan%5B2%5D%2Fh4%5B1%5D%2Fa%5B1%5D&extendContext=&contextXPath=%2F%2Fdiv%5B6%5D%2Fdiv%5B1%5D%2Fdiv%5B3%5D%2Fdiv%5B1%5D%2Fdiv%5B3%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv',
          harvest: false,
          tags: ['video'],
        },
        // {
        //   title: 'ZDFchen',
        //   xmlUrl:
        //     'https://www.zdf.de/rss/zdf/kinder/zdfchen-filme',
        //   harvest: false,
        //   tags: ['video'],
        // },
      ],
      pipeline: [
        {
          map: 'yt',
          context: JSON.stringify({ format: '720p', manual: false }),
        },
      ],
      exporters: [
        {
          targets: [
            {
              type: 'feed',
              // id: 'inherited',
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
    // {
    //   title: 'Self Test',
    //   visibility: 'private',
    //   subscriptions: [
    //     {
    //       title: 'full fledged feed',
    //       xmlUrl: 'http://localhost:8080/debug/atom-feed-with-digest-auth',
    //       auth: {
    //         digest: 'wef'
    //       }
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
