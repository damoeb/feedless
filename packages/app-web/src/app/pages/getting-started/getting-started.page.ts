import { Component, ViewChild } from '@angular/core';
import { PageHeaderComponent } from '../../components/page-header/page-header.component';
import '@justinribeiro/lite-youtube';
import { Router } from '@angular/router';
import { FeedBuilderModalComponentProps } from '../../modals/feed-builder-modal/feed-builder-modal.component';
import { ModalService } from '../../services/modal.service';

export const isUrl = (value: string): boolean => {
  if (!value || value.length < 3) {
    return false;
  }
  const potentialUrl = value.toLowerCase();
  if (
    potentialUrl.startsWith('http://') ||
    potentialUrl.startsWith('https://')
  ) {
    try {
      new URL(value);

      const urlPattern =
        /[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{2,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)?/gi;
      return !!potentialUrl.match(new RegExp(urlPattern));
    } catch (e) {
      return false;
    }
  } else {
    return isUrl(`https://${potentialUrl}`);
  }
};

export const isValidUrl = (value: string): boolean => {
  const potentialUrl = value.trim();
  return potentialUrl.toLowerCase().startsWith('http://') ||
    potentialUrl.toLowerCase().startsWith('https://')
}
export const fixUrl = (value: string): string => {
  const potentialUrl = value.trim();
  if (isValidUrl(potentialUrl)) {
    return potentialUrl;
  } else {
    try {
      const fixedUrl = `https://${potentialUrl}`;
      new URL(fixedUrl);
      return fixedUrl;
    } catch (e) {
      throw new Error('invalid url')
    }
  }
};

@Component({
  selector: 'app-getting-started',
  templateUrl: './getting-started.page.html',
  styleUrls: ['./getting-started.page.scss'],
})
export class GettingStartedPage {
  @ViewChild('headerComponent')
  headerComponent: PageHeaderComponent;

  constructor(
    private readonly modalService: ModalService,
    private readonly router: Router,
  ) {}

  openReader(url: string) {
    return this.router.navigateByUrl(`/reader?url=${fixUrl(url)}`);
  }

  async openFeedBuilder(url: string) {
    const componentProps: FeedBuilderModalComponentProps = {
      feedBuilder: {
        source: [
          {
            request: {
              page: {
                url: fixUrl(url),
              },
              emit: [
              ]
            },
          },
          // {
          //   request: {
          //     page: {
          //       url: 'https://www.telepolis.de/news-atom.xml',
          //     },
          //     emit: []
          //   },
          // },
        ],
        sink: {
          targets: [
            {
              type: 'bucket',
              oneOf: {
                feed: {
                  type: 'existing',
                  oneOf: {
                    existing: {
                      where: {
                        id: '1'
                      }
                    }
                  }
                }
              }
            }
          ],
          segmented: {
            filter: '',
            size: 10,
            orderBy: 'createdAt',
            digest: false,
            orderAsc: true
          },
          isSegmented: false
        },
        filters: [
          {
            type: 'exclude',
            negate: true,
            value: 'foo',
            field: 'title',
            operator: 'contains'
          }
        ],
        refine: [
          // {
          //   create: {
          //     field: 'body',
          //     aliasAs: 'publishedAt',
          //   }
          // }
        ],
        fetch: {
          cronString: '* * * * *'
        }
      }
    };
    await this.modalService.openFeedBuilder(componentProps);
  }
}
