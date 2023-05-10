import { Component } from '@angular/core';
import { OpmlService, Outline } from '../../services/opml.service';
import { ModalController, ToastController } from '@ionic/angular';
import { ImportOpmlModalComponent, ImportOpmlModalComponentProps } from '../import-opml-modal/import-opml-modal.component';
import {
  ImportFromMarkupModalComponent,
  ImportFromMarkupModalComponentProps
} from '../import-from-markup-modal/import-from-markup-modal.component';
import { WizardComponent, WizardComponentProps, WizardContext, WizardStepId } from '../../components/wizard/wizard/wizard.component';
import { ImporterService } from '../../services/importer.service';
import {
  GqlExtendContentOptions,
  GqlNativeGenericOrFragmentWatchFeedCreateInput,
  GqlPuppeteerWaitUntil,
  GqlVisibility
} from '../../../generated/graphql';
import { FeedService } from '../../services/feed.service';
import { BucketService } from '../../services/bucket.service';
import { OverlayEventDetail } from '@ionic/core';

export enum ImporterModalRole {
  feedsOnly = 'feedsOnly',
  multipleBuckets = 'multipleBuckets',
  bucket = 'bucket',
}

export interface OpmlBucket {
  title: string;
  description?: string;
  websiteUrl?: string;
  outlines: Outline[];
}

export interface ImportModalComponentProps {
  dismissModal?: (data: ImportModalData, role: ImporterModalRole) => void;
}

export interface ImportModalData {
  feeds?: GqlNativeGenericOrFragmentWatchFeedCreateInput[];
  buckets?: OpmlBucket[];
}

@Component({
  selector: 'app-import-modal',
  templateUrl: './import-modal.component.html',
  styleUrls: ['./import-modal.component.scss']
})
export class ImportModalComponent {
  constructor(
    private readonly opmlService: OpmlService,
    private readonly feedService: FeedService,
    private readonly bucketService: BucketService,
    private readonly importerService: ImporterService,
    private readonly toastCtrl: ToastController,
    private readonly modalCtrl: ModalController
  ) {
  }

  async uploadOpmlFile(uploadEvent: Event) {
    const componentProps: ImportOpmlModalComponentProps = {
      outlines: await this.opmlService.convertOpmlToJson(uploadEvent)
    };
    const modal = await this.modalCtrl.create({
      component: ImportOpmlModalComponent,
      componentProps,
      backdropDismiss: true
    });
    await modal.present();
    const modalDismissal = await modal.onDidDismiss<ImportModalData>();
    await this.handleModalDismissal(modalDismissal);
  }

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  async importNativeFeedUrls() {
    const componentProps: ImportFromMarkupModalComponentProps = {
      convertToGraphqlStatement: (urls: string[]) =>
        urls.map((url) => ({
          nativeFeed: {
            title: `Native Feed of ${url}`,
            feedUrl: url
          }
        })),
      kind: 'native feeds'
    };
    await this.importFromAny(componentProps);
  }

  async importRssProxyUrls() {
    const componentProps: ImportFromMarkupModalComponentProps = {
      kind: 'rss-proxy',
      convertToGraphqlStatement: (urls: string[]) =>
        urls.map((url) => {
          const rpUrl = new URL(url);
          const websiteUrl = rpUrl.searchParams.get('url');
          return {
            genericFeed: {
              title: `Generic Feed for ${new URL(websiteUrl).hostname}`,
              harvestItems: false,
              specification: {
                selectors: {
                  contextXPath: rpUrl.searchParams.get('context'),
                  linkXPath: rpUrl.searchParams.get('link'),
                  extendContext: GqlExtendContentOptions.None
                },
                fetchOptions: {
                  websiteUrl,
                  prerender: rpUrl.searchParams.get('pp') === 'true',
                  prerenderWaitUntil: GqlPuppeteerWaitUntil.Load
                },
                refineOptions: {
                  filter: rpUrl.searchParams.get('q')
                }
              }
            }
          };
        })
    };
    await this.importFromAny(componentProps);
  }

  async importFromAny(componentProps: ImportFromMarkupModalComponentProps) {
    const modal = await this.modalCtrl.create({
      component: ImportFromMarkupModalComponent,
      componentProps,
      backdropDismiss: true
    });
    await modal.present();
    const modalDismissal = await modal.onDidDismiss<ImportModalData>();
    await this.handleModalDismissal(modalDismissal);
  }

  private async showCancelToast(message?: string) {
    await this.showToast(message || 'Canceled', undefined);
  }

  private async showToast(
    message: string,
    color: 'danger' | 'success' | undefined
  ) {
    const toast = await this.toastCtrl.create({
      message,
      duration: 3000,
      color
    });
    await toast.present();
  }

  private async importBucketWithFeeds(
    feeds: GqlNativeGenericOrFragmentWatchFeedCreateInput[]
  ) {
    if (feeds.length > 0) {
      const wizardProps: WizardComponentProps = {
        initialContext: {
          modalTitle: 'Pick a Bucket',
          stepId: WizardStepId.bucket,
          bucket: {
            create: {
              title: 'New Bucket',
              description: '',
              visibility: GqlVisibility.IsPublic,
              tags: []
            }
          }
        }
      };
      const bucketModal = await this.modalCtrl.create({
        component: WizardComponent,
        componentProps: wizardProps,
        backdropDismiss: false
      });
      await bucketModal.present();
      const bucketModalResponse = await bucketModal.onDidDismiss<WizardContext>();

      if (bucketModalResponse.role) {
        const wizardContext = bucketModalResponse.data;

        await this.importerService.createImporters({
          bucket: wizardContext.bucket,
          feeds: feeds.map((feed) => ({
            create: feed
          }))
        });

        await this.showToast(`Saved`, 'success');
      } else {
        await this.showCancelToast();
      }
    } else {
      await this.showCancelToast();
    }
  }

  private async importFeeds(
    feeds: GqlNativeGenericOrFragmentWatchFeedCreateInput[]
  ) {
    if (feeds.length > 0) {
      await this.feedService.createNativeFeeds({
        feeds
      });
      await this.showToast('Saved', 'success');
      await this.modalCtrl.dismiss();
    } else {
      await this.showCancelToast();
    }
  }

  private async handleModalDismissal(
    modalDismissal: OverlayEventDetail<ImportModalData>
  ) {
    switch (modalDismissal.role) {
      case ImporterModalRole.feedsOnly:
        await this.importFeeds(modalDismissal.data.feeds);
        break;
      case ImporterModalRole.bucket:
        await this.importBucketWithFeeds(modalDismissal.data.feeds);
        break;
      case ImporterModalRole.multipleBuckets:
        const buckets = modalDismissal.data.buckets;
        if (buckets.length > 0) {
          await this.bucketService.createBuckets({
            buckets: buckets.map(bucket => ({
                title: bucket.title,
                visibility: GqlVisibility.IsPrivate,
                description: bucket.description,
                websiteUrl: bucket.websiteUrl,
                importers: bucket.outlines.map(o => ({
                    title: o.title,
                    feeds: [
                      {
                        create: {
                          nativeFeed: {
                            title: o.title,
                            feedUrl: o.xmlUrl
                          }
                        }
                      }
                    ]
                  }))
            }))
          });
          await this.showToast(`Saved`, 'success');
        } else {
          await this.showCancelToast();
        }
        break;
      default:
        await this.showCancelToast();
    }
  }
}
