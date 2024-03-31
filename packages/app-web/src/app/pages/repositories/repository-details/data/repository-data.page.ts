import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertController, ToastController } from '@ionic/angular';
import { FetchPolicy } from '@apollo/client/core';
import { ServerSettingsService } from '../../../../services/server-settings.service';
import { dateTimeFormat, ProfileService } from '../../../../services/profile.service';
import { Subscription } from 'rxjs';
import { WebDocument } from '../../../../graphql/types';
import { SourceSubscriptionService } from '../../../../services/source-subscription.service';
import { ModalService } from '../../../../services/modal.service';
import { WebDocumentService } from '../../../../services/web-document.service';

@Component({
  selector: 'app-repository-data-page',
  templateUrl: './repository-data.page.html',
  styleUrls: ['./repository-data.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RepositoryDataPage implements OnInit, OnDestroy {
  loadingSource: boolean;
  repositoryId: string;
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
        this.repositoryId = params.repositoryId;
        this.fetch();
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
              id: this.repositoryId,
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

  protected readonly dateTimeFormat = dateTimeFormat;
}
