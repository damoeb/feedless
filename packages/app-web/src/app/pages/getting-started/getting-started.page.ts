import { Component, ViewChild } from '@angular/core';
import { GqlScrapeEmitType } from '../../../generated/graphql';
import { PageHeaderComponent } from '../../components/page-header/page-header.component';
import '@justinribeiro/lite-youtube';
import { Router } from '@angular/router';
import { ModalController } from '@ionic/angular';
import { FeedBuilderCardComponentProps, FeedBuilderModalComponent } from '../../modals/feed-builder-modal/feed-builder-modal.component';

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

export const fixUrl = (value: string): string => {
  const potentialUrl = value.trim();
  if (
    potentialUrl.toLowerCase().startsWith('http://') ||
    potentialUrl.toLowerCase().startsWith('https://')
  ) {
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
    private readonly modalCtrl: ModalController,
    private readonly router: Router,
  ) {}

  openReader(url: string) {
    return this.router.navigateByUrl(`/reader?url=${fixUrl(url)}`);
  }

  async openFeedBuilder(url: string) {
    const componentProps: FeedBuilderCardComponentProps = {
      scrapeBuilderSpec: {
        sources: [
          {
            request: {
              page: {
                url: fixUrl(url)
              },
              emit: [GqlScrapeEmitType.Feeds],
              elements: ['/'],
              debug: {
                html: true,
              }
            },
            responseMapper: {
              feed: {}
            }
          },
        ],
        sink: {
          targets: [
            {
              feed: {}
            }
          ],
        }
      }
    };
    const modal = await this.modalCtrl.create({
      component: FeedBuilderModalComponent,
      componentProps,
      cssClass: 'modal-dialog',
      showBackdrop: true,
      backdropDismiss: false,
    });
    await modal.present();
  }
}
