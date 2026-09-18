import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  effect,
  inject,
  input,
  linkedSignal,
  OnInit,
  output,
  PLATFORM_ID,
} from '@angular/core';

import {
  ActionSheetController,
  AlertController,
  IonButton,
  IonButtons,
  IonCol,
  IonItem,
  IonLabel,
  IonList,
  IonProgressBar,
  IonRow,
  IonSearchbar,
  IonText,
  IonToolbar,
} from '@ionic/angular/standalone';
import { relativeTimeOrElse } from '../agents/agents.component';
import { BubbleColor, BubbleComponent } from '@feedless/ui';
import {
  GqlSortOrder,
  GqlSourcesWhereInput,
  RepositoryFull,
  RepositorySource,
} from '@feedless/graphql-api';
import { FetchPolicy } from '@apollo/client/core';
import { ArrayElement, Nullable } from '@feedless/core';
import { cloneDeep } from 'lodash-es';
import {
  FeedOrRepository,
  tagsToString,
} from '../feed-builder/feed-builder.component';
import { describeNextHarvest, RepositoryService, Source, SourceWithLastHarvest } from '@feedless/data-access';
import { PaginationComponent } from '@feedless/ui';
import { addIcons } from 'ionicons';
import { addOutline, cloudUploadOutline, refreshOutline } from 'ionicons/icons';
import dayjs from 'dayjs';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounce, interval } from 'rxjs';
import { CodeEditorModalComponent, CodeEditorModalDetail } from '../../modals/code-editor-modal/code-editor-modal.component';
import { FeedBuilderModalComponent } from '../../modals/feed-builder-modal/feed-builder-modal.component';
import { ModalProvider } from '../../modals/modal-provider.service';
import { SearchAddressModalComponent } from '../../modals/search-address-modal/search-address-modal.component';
import { SourceImportService } from '../../modals/source-import.service';
import { TagsModalComponent } from '../../modals/tags-modal/tags-modal.component';
import { isPlatformBrowser } from '@angular/common';
import { IconComponent } from '@feedless/ui';

@Component({
  selector: 'app-sources',
  templateUrl: './sources.component.html',
  styleUrls: ['./sources.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    BubbleComponent,
    IonButton,
    IonButtons,
    IonCol,
    IonItem,
    IonLabel,
    IonList,
    IonProgressBar,
    IonRow,
    IonText,
    PaginationComponent,
    IconComponent,
    IonToolbar,
    IonSearchbar,
    ReactiveFormsModule,
  ],
  standalone: true,
})
export class SourcesComponent implements OnInit {
  private readonly alertCtrl = inject(AlertController);
  private readonly repositoryService = inject(RepositoryService);
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly modalProvider = inject(ModalProvider);
  private readonly actionSheetCtrl = inject(ActionSheetController);
  private readonly sourceImportService = inject(SourceImportService);
  sourceChange = output<Source[]>();

  readonly repository = input.required<RepositoryFull>();
  readonly sourcesFilter = input<Nullable<GqlSourcesWhereInput>>();

  protected loadingSources = false;
  currentSourcesPage = 0;
  sources: SourceWithLastHarvest[] = [];
  protected readonly fromNow = relativeTimeOrElse;
  protected readonly describeNextHarvest = describeNextHarvest;
  protected readonly nextUpdateAt = linkedSignal(
    () => this.repository().nextUpdateAt,
  );
  protected pageSize = 10;
  protected queryFc = new FormControl<string>('');
  private readonly platformId = inject(PLATFORM_ID);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({
        addOutline,
        cloudUploadOutline,
        refreshOutline,
      });
    }
    effect(() => {
      console.log('change', this.sourcesFilter());
      this.fetchCurrentPage();
    });
  }

  ngOnInit() {
    this.queryFc.valueChanges
      .pipe(debounce(() => interval(400)))
      .subscribe((query) => {
        this.fetchCurrentPage();
      });
    this.fetchCurrentPage();
  }

  async importFeedlessJson(uploadEvent: Event) {
    return this.sourceImportService.uploadFeedlessJson(
      uploadEvent,
      this.repository().id,
    );
  }

  async deleteSource(source: RepositorySource) {
    console.log('deleteSource', source);
    const alert = await this.alertCtrl.create({
      header: 'Delete Source?',
      message: `You won't be able to recover it.`,
      // cssClass: 'fatal-alert',
      buttons: [
        {
          text: 'Cancel',
          role: 'cancel',
        },
        {
          text: 'Yes, Delete',
          role: 'confirm',
          cssClass: 'confirm-button',
          handler: async () => {
            await this.repositoryService.updateRepository({
              where: {
                id: this.repository().id,
              },
              data: {
                sources: {
                  remove: [source.id],
                },
              },
            });
            this.fetchSources(this.currentSourcesPage, 'network-only');
            this.changeRef.detectChanges();
          },
        },
      ],
    });
    await alert.present();
  }

  async fetchSources(page: number, fetchPolicy: FetchPolicy = 'cache-first') {
    this.currentSourcesPage = page;
    this.loadingSources = true;
    this.sources = [];
    this.changeRef.detectChanges();
    try {
      this.sources = await this.repositoryService.getSourcesByRepository(
        this.repository().id,
        {
          page,
          pageSize: this.pageSize,
        },
        this.queryFc.value
          ? {
              like: this.queryFc.value,
            }
          : this.sourcesFilter(),
        [
          { lastRecordsRetrieved: GqlSortOrder.Asc },
          { lastRefreshedAt: GqlSortOrder.Asc },
        ],
        fetchPolicy,
      );
      this.sourceChange.emit(this.sources);
    } finally {
      this.loadingSources = false;
    }
    this.changeRef.detectChanges();
  }

  getHealthColorForSource(
    source: ArrayElement<RepositoryFull['sources']>,
  ): BubbleColor {
    if (source.disabled || source.lastRecordsRetrieved === 0) {
      return 'red';
    } else {
      return 'green';
    }
  }

  // a queued or running harvest has not counted its new items yet
  protected lastItemsAdded(source: SourceWithLastHarvest): number | undefined {
    const harvest = source.harvests?.[0];
    return harvest?.finishedAt ? harvest.itemsAdded : undefined;
  }

  async editLatLon(source: ArrayElement<RepositoryFull['sources']>) {
    const geoTag = await this.modalProvider.openSearchAddressModal(
      SearchAddressModalComponent,
    );
    if (geoTag) {
      await this.repositoryService.updateRepository({
        where: {
          id: this.repository().id,
        },
        data: {
          sources: {
            update: [
              {
                where: {
                  id: source.id,
                },
                data: {
                  latLng: geoTag
                    ? {
                        set: {
                          lat: parseFloat(`${geoTag.lat}`),
                          lng: parseFloat(`${geoTag.lng}`),
                        },
                      }
                    : null,
                },
              },
            ],
          },
        },
      });
      await this.fetchSources(this.currentSourcesPage, 'network-only');
      this.changeRef.detectChanges();
    }
  }

  async editTags(source: ArrayElement<RepositoryFull['sources']>) {
    const tags = await this.modalProvider.openTagModal(TagsModalComponent, {
      tags: source.tags || [],
    });
    await this.repositoryService.updateRepository({
      where: {
        id: this.repository().id,
      },
      data: {
        sources: {
          update: [
            {
              where: {
                id: source.id,
              },
              data: {
                tags: {
                  set: tags,
                },
              },
            },
          ],
        },
      },
    });
    await this.fetchSources(this.currentSourcesPage, 'network-only');
    this.changeRef.detectChanges();
  }

  stringifyTags(source: ArrayElement<RepositoryFull['sources']>) {
    return tagsToString(source.tags) || 'Add tags';
  }

  stringifyLocalization(source: ArrayElement<RepositoryFull['sources']>) {
    const { latLng } = source;
    return latLng
      ? `(${latLng.lat.toFixed(4)},${latLng.lng.toFixed(4)})`
      : 'Add geo tag';
  }

  async setDisabledForSource(source: RepositorySource, isDisabled: boolean) {
    await this.repositoryService.updateRepository({
      where: {
        id: this.repository().id,
      },
      data: {
        sources: {
          update: [
            {
              where: {
                id: source.id,
              },
              data: {
                disabled: {
                  set: isDisabled,
                },
              },
            },
          ],
        },
      },
    });
    await this.fetchSources(this.currentSourcesPage, 'network-only');
    this.changeRef.detectChanges();
  }

  async forkSource(source: RepositorySource) {
    const fork = cloneDeep(source);
    fork.id = null;
    await this.editOrAddSource(fork);
  }

  async editOrAddSource(source: Nullable<RepositorySource> = null) {
    const toSource = async () => {
      if (source) {
        return this.repositoryService.toSourceInput(
          await this.repositoryService.getSourceFullByRepository(
            this.repository().id,
            source.id,
          ),
        );
      }
      return undefined;
    };

    await this.modalProvider.openFeedBuilder(
      FeedBuilderModalComponent,
      {
        source: await toSource(),
      },
      async (data: FeedOrRepository) => {
        if (data?.repository) {
          console.warn('not implemented');
        }
        if (data?.feed) {
          if (source?.id) {
            await this.repositoryService.updateRepository({
              where: {
                id: this.repository().id,
              },
              data: {
                sources: {
                  update: [
                    {
                      where: {
                        id: source.id,
                      },
                      data: {
                        latLng: {
                          set: data.feed.source.latLng,
                        },
                        tags: {
                          set: data.feed.source.tags,
                        },
                        title: {
                          set: data.feed.source.title,
                        },
                        flow: {
                          set: data.feed.source.flow,
                        },
                      },
                    },
                  ],
                },
              },
            });
            await this.fetchSources(this.currentSourcesPage, 'network-only');
          } else {
            await this.repositoryService.updateRepository({
              where: {
                id: this.repository().id,
              },
              data: {
                sources: {
                  add: [
                    {
                      title: data.feed.source.title,
                      latLng: data.feed.source.latLng,
                      tags: data.feed.source.tags,
                      flow: data.feed.source.flow,
                    },
                  ],
                },
              },
            });
            await this.fetchSources(0, 'network-only');
          }
          this.changeRef.detectChanges();
        }
      },
    );
  }

  async fetchCurrentPage(fetchPolicy: FetchPolicy = 'cache-first') {
    await this.fetchSources(this.currentSourcesPage, fetchPolicy);
  }

  async showSourceOptions(source: ArrayElement<RepositoryFull['sources']>) {
    const actionSheet = await this.actionSheetCtrl.create({
      header: 'Source Actions',
      buttons: [
        {
          text: 'Fork',
          role: 'destructive',
          handler: () => {
            this.forkSource(source);
          },
        },
        {
          text: 'Disable',
          disabled: source.disabled,
          role: 'destructive',
          handler: () => {
            this.setDisabledForSource(source, true);
          },
        },
        {
          text: 'Delete Source',
          handler: () => {
            this.deleteSource(source);
          },
        },
        {
          text: 'Cancel',
          role: 'cancel',
        },
      ],
    });

    await actionSheet.present();
  }

  async handlePageSizeChange(pageSize: number) {
    this.pageSize = pageSize;
    await this.fetchSources(this.currentSourcesPage);
  }

  async showLogs(source: SourceWithLastHarvest) {
    const repositoryId = this.repository().id;
    const [harvest, sourceFull] = await Promise.all([
      this.repositoryService.getLastHarvestFromSourcesByRepository(
        repositoryId,
        source.id,
      ),
      this.repositoryService.getSourceFullByRepository(repositoryId, source.id),
    ]);
    const url = sourceFull?.flow?.sequence?.find((a) => a.fetch)?.fetch.get.url
      .literal;
    const formatDate = (date: number) =>
      date ? dayjs(date).format('YYYY-MM-DD HH:mm:ss') : '-';
    const details: CodeEditorModalDetail[] = [
      { label: 'Source', value: source.title },
      ...(url ? [{ label: 'URL', value: url, href: url }] : []),
      {
        label: 'Status',
        value: source.disabled ? 'Disabled' : 'Enabled',
      },
      ...(source.lastErrorMessage
        ? [{ label: 'Last error', value: source.lastErrorMessage }]
        : []),
      ...(source.tags?.length > 0
        ? [{ label: 'Tags', value: tagsToString(source.tags) }]
        : []),
      ...(source.latLng
        ? [{ label: 'Geo tag', value: this.stringifyLocalization(source) }]
        : []),
      { label: 'Items total', value: `${source.recordCount}` },
      { label: 'Harvest', value: harvest.ok ? 'Succeeded' : 'Failed' },
      { label: 'Started', value: formatDate(harvest.startedAt) },
      { label: 'Finished', value: formatDate(harvest.finishedAt) },
      {
        label: 'Items',
        value: `${source.lastRecordsRetrieved} found · ${harvest.itemsAdded} new · ${harvest.itemsIgnored} filtered`,
      },
    ];
    await this.modalProvider.openCodeEditorModal(CodeEditorModalComponent, {
      readOnly: true,
      contentType: 'text',
      text: harvest.logs,
      details,
      title: 'Harvest Logs',
    });
  }

  async forceSync() {
    this.nextUpdateAt.set(
      await this.repositoryService.forceSourceSync(this.repository().id),
    );
  }
}
