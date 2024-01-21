import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertController, ToastController } from '@ionic/angular';
import { FetchPolicy } from '@apollo/client/core';
import { GqlVisibility } from '../../../../generated/graphql';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { ProfileService } from '../../../services/profile.service';
import { Subscription } from 'rxjs';
import { SourceSubscription, WebDocument } from '../../../graphql/types';
import { SourceSubscriptionService } from '../../../services/source-subscription.service';
import { ModalService } from '../../../services/modal.service';
import { WebDocumentService } from '../../../services/web-document.service';
import { isArray, isObject, without } from 'lodash-es';
import { isDefined } from '../../../modals/feed-builder-modal/scrape-builder';

export function removeTypenames<T>(data: T): T {
  if (isArray(data)) {
    return data.map((it) => removeTypenames(it)) as T;
  } else {
    if (isObject(data)) {
      return without(Object.keys(data), '__typename').reduce((obj, key) => {
        obj[key] = removeTypenames(data[key]);
        return obj;
      }, {} as Partial<T>) as T;
    } else {
      return data;
    }
  }
}

export const visibilityToLabel = (visibility: GqlVisibility): string => {
  switch (visibility) {
    case GqlVisibility.IsPrivate:
      return 'private';
    case GqlVisibility.IsPublic:
      return 'public';
  }
};

@Component({
  selector: 'app-source-page',
  templateUrl: './source.page.html',
  styleUrls: ['./source.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SourcePage implements OnInit, OnDestroy {
  loadingSource: boolean;
  source: SourceSubscription;
  readonly entityVisibility = GqlVisibility;
  feedUrl: string;
  isLast: boolean;
  entities: WebDocument[] = [];

  private subscriptions: Subscription[] = [];
  private currentPage = 0;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly router: Router,
    private readonly toastCtrl: ToastController,
    private readonly alertCtrl: AlertController,
    private readonly sourceSubscriptionService: SourceSubscriptionService,
    private readonly profileService: ProfileService,
    private readonly serverSettings: ServerSettingsService,
    private readonly modalService: ModalService,
    private readonly webDocumentService: WebDocumentService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe((params) => {
        this.fetchSourceSubscription(params.id);
        this.feedUrl = `${this.serverSettings.apiUrl}/feed/${params.id}`;
      }),
    );
  }
  async fetch(page: number = 0, fetchPolicy: FetchPolicy = 'cache-first') {
    const entities = await this.webDocumentService.findAllByStreamId(
      {
        cursor: {
          page,
        },
        where: {
          sourceSubscription: {
            where: {
              id: this.source.id,
            },
          },
        },
      },
      fetchPolicy,
    );

    this.isLast = entities.length < 10;
    this.entities.push(...entities);
    this.changeRef.detectChanges();
  }

  async nextPage(event: any) {
    this.currentPage += 1;
    await this.fetch(this.currentPage);
    await event.target.complete();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async editSource() {
    console.log(this.source, removeTypenames(this.source));
    await this.modalService.openFeedBuilder(
      {
        feedBuilder: {
          sink: {
            title: this.source.title,
            visibility: this.source.visibility,
            description: this.source.description,
            hasRetention:
              isDefined(this.source.retention.maxItems) ||
              isDefined(this.source.retention.maxAgeDays),
            retention: {
              maxItems: this.source.retention.maxItems,
              maxAgeDays: this.source.retention.maxAgeDays,
            },
          },
          sources: this.source.sources?.map((request) => ({
            request: removeTypenames(request),
          })),
        },
      },
      async (data, role) => {
        console.log('role', role);
        // if (data) {
        //   const toast = await this.toastCtrl.create({
        //     message: 'Updated',
        //     duration: 3000,
        //     color: 'success',
        //   });
        //   await toast.present();
        //   await this.fetchSourceSubscription(this.bucket.id, 'network-only');
        // } else {
        //   const toast = await this.toastCtrl.create({
        //     message: 'Canceled',
        //     duration: 3000,
        //   });
        //
        //   await toast.present();
        // }
      },
    );
  }

  async deleteSource() {
    const alert = await this.alertCtrl.create({
      header: '',
      backdropDismiss: false,
      message: `Delete Source permanently?`,
      buttons: [
        {
          text: 'Delete',
          role: 'confirm',
        },
        {
          text: 'Cancel',
          role: 'cancel',
        },
      ],
    });

    await alert.present();
    const { role } = await alert.onDidDismiss();

    if (role !== 'cancel') {
      await this.sourceSubscriptionService.deleteSubscription({
        id: this.source.id,
      });
      // const toast = await this.toastCtrl.create({
      //   message: 'Deleted',
      //   duration: 3000,
      //   color: 'success',
      // });
      // await toast.present();
      await this.router.navigateByUrl('/sources');
    }
  }

  private async fetchSourceSubscription(
    sourceSubscriptionId: string,
    fetchPolicy: FetchPolicy = 'cache-first',
  ) {
    this.loadingSource = true;
    try {
      this.source = await this.sourceSubscriptionService.getSubscriptionById(
        sourceSubscriptionId,
        fetchPolicy,
      );
      await this.fetch(0, 'network-only');
      this.changeRef.detectChanges();
    } finally {
      this.loadingSource = false;
    }
  }
}
