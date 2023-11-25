import { GqlAgentInput, GqlBucketCreateInput, GqlBucketWhereInput, GqlScrapeRequestInput } from '../../../generated/graphql';
import { ScrapeResponse } from '../../graphql/types';
import { isEqual, isNull, isUndefined, without } from 'lodash-es';
import { ScrapeService } from '../../services/scrape.service';
import { Agent } from '../../services/agent.service';
import { Subject } from 'rxjs';

export type Maybe<T> = T | null

export type OutputType = 'markup' | 'text' | 'image' | 'feed' | 'other'

export interface Field {
  type: 'text' | 'markup' | 'base64' | 'url' | 'date'
  name: string
}

export function isDefined(v: any | undefined): boolean {
  return !isNull(v) && !isUndefined(v);
}

const fieldsInFeed: Field[] = [
  {
    name: 'title',
    type: 'text'
  },
  {
    name: 'description',
    type: 'text'
  },
  {
    name: 'link',
    type: 'url'
  },
  {
    name: 'createdAt',
    type: 'date'
  }
];

export type ResponseMapper = 'readability' | 'nativeFeed' |  'feed' | 'fragment' | 'pageScreenshot' | 'pageMarkup'

// function isFeed(contentType: string): boolean {
//   return contentType && [
//     'application/atom+xml',
//     'application/rss+xml',
//     'application/xml',
//     'text/xml'
//   ].some(feedMime => contentType.toLowerCase().startsWith(feedMime));
// }

// function mapMimeToProviderType(contentType: string): Artefact[] {
//     if (contentType) {
//       if (isFeed(contentType)) {
//         return [{ type: 'feed', label: 'Feed', fields: fieldsInFeed }]
//       }
//       if (contentType.startsWith('image/')) {
//         return [{ type: 'image', label: 'Image', fields: [] }]
//       }
//       if (contentType.startsWith('text/html')) {
//         return [{ type: 'markup', label: 'Markup', fields: [] }]
//       }
//     }
//     return []
// }

