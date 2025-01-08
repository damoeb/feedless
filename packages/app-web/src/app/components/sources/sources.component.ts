import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  effect,
  inject,
  input,
  OnInit,
  output,
} from '@angular/core';

import {
  ActionSheetController,
  AlertController,
  IonButton,
  IonButtons,
  IonChip,
  IonCol,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonProgressBar,
  IonRow,
  IonText,
  IonTitle,
  IonToolbar,
  ToastController,
} from '@ionic/angular/standalone';
import { relativeTimeOrElse } from '../agents/agents.component';
import { BubbleColor, BubbleComponent } from '../bubble/bubble.component';
import { RepositoryFull, RepositorySource } from '../../graphql/types';
import { FetchPolicy } from '@apollo/client/core';
import { ArrayElement, Nullable } from '../../types';
import {
  GqlRepositoryCreateInput,
  GqlSortOrder,
  GqlSourceInput,
  GqlSourcesWhereInput,
} from '../../../generated/graphql';
import { SelectableEntity } from '../../modals/selection-modal/selection-modal.component';
import { sortBy } from 'lodash-es';
import {
  FeedOrRepository,
  tagsToString,
} from '../feed-builder/feed-builder.component';
import { RepositoryService, Source } from '../../services/repository.service';
import { FileService } from '../../services/file.service';
import { ModalService } from '../../services/modal.service';
import { PaginationComponent } from '../pagination/pagination.component';
import { addIcons } from 'ionicons';
import {
  addOutline,
  cloudUploadOutline,
  ellipsisHorizontalOutline,
  refreshOutline,
} from 'ionicons/icons';
import { FeedBuilderModalModule } from '../../modals/feed-builder-modal/feed-builder-modal.module';

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
    IonIcon,
    IonTitle,
    IonToolbar,
    FeedBuilderModalModule,
  ],
  standalone: true,
})
export class SourcesComponent implements OnInit {
  private readonly alertCtrl = inject(AlertController);
  private readonly repositoryService = inject(RepositoryService);
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly fileService = inject(FileService);
  private readonly modalService = inject(ModalService);
  private readonly toastCtrl = inject(ToastController);
  private readonly actionSheetCtrl = inject(ActionSheetController);
  sourceChange = output<Source[]>();

  readonly repository = input.required<RepositoryFull>();
  readonly sourcesFilter = input<Nullable<GqlSourcesWhereInput>>();

  protected loadingSources: boolean = false;
  currentSourcesPage: number = 0;
  sources: Source[] = [];
  protected readonly fromNow = relativeTimeOrElse;
  protected pageSize = 20;

  constructor() {
    addIcons({
      addOutline,
      cloudUploadOutline,
      refreshOutline,
      ellipsisHorizontalOutline,
    });
    effect(() => {
      console.log('change', this.sourcesFilter());
      this.fetchCurrentPage();
    });
  }

  ngOnInit() {
    this.fetchCurrentPage();
  }

  async importFeedlessJson(uploadEvent: Event) {
    const data = await this.fileService.uploadAsText(uploadEvent);
    const repositories = JSON.parse(data) as GqlRepositoryCreateInput[];
    const selectables: SelectableEntity<GqlSourceInput>[] = sortBy(
      repositories
        .filter((r) => r.sources)
        .flatMap((r) => r.sources)
        .map<SelectableEntity<GqlSourceInput>>((source) => {
          const disabled = this.repository().sources.some(
            (existingSource) => existingSource.title == source.title,
          );
          return {
            entity: source,
            disabled,
            note: disabled ? 'Already exists by name' : null,
            label: source.title,
          };
        }),
      (selectable) => selectable.entity.title,
    );

    const selected = await this.modalService.openSelectionModal<GqlSourceInput>(
      {
        selectables,
        title: 'Import new sources',
        description: 'Select those sources you want to import',
      },
    );

    if (selected.length > 0) {
      await this.repositoryService.updateRepository({
        where: {
          id: this.repository().id,
        },
        data: {
          sources: {
            add: selected,
          },
        },
      });

      await this.fetchSources(this.currentSourcesPage, 'network-only');
      this.changeRef.detectChanges();

      const toast = await this.toastCtrl.create({
        message: `Added ${selected.length} sources`,
        duration: 3000,
        color: 'success',
      });

      await toast.present();
    }
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
        this.sourcesFilter(),
        {
          lastRecordsRetrieved: GqlSortOrder.Asc,
        },
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

  async editLatLon(source: ArrayElement<RepositoryFull['sources']>) {
    const geoTag = await this.modalService.openSearchAddressModal();
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
    const tags = await this.modalService.openTagModal({
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
    };

    await this.modalService.openFeedBuilder(
      {
        source: await toSource(),
      },
      async (data: FeedOrRepository) => {
        if (data?.repository) {
          console.warn('not implemented');
        }
        if (data?.feed) {
          if (source) {
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
      header: 'Actions',
      buttons: [
        {
          text: 'Disable',
          role: 'destructive',
          handler: () => {
            this.setDisabledForSource(source, true);
          },
        },
        {
          text: 'Delete',
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
}
