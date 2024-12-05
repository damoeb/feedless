import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  input,
  OnDestroy,
  OnInit,
  viewChild,
} from '@angular/core';
import {
  GqlFeedlessPlugins,
  GqlRecordField,
  GqlRepositoryCreateInput,
  GqlSourceInput,
  GqlVertical,
  GqlVisibility,
} from '../../../generated/graphql';
import {
  Annotation,
  Record,
  RepositoryFull,
  RepositorySource,
} from '../../graphql/types';
import {
  RepositoryModalAccordion,
  RepositoryModalComponentProps,
} from '../../modals/repository-modal/repository-modal.component';
import { ModalName, ModalService } from '../../services/modal.service';
import {
  AlertController,
  IonBadge,
  IonButton,
  IonButtons,
  IonCheckbox,
  IonChip,
  IonCol,
  IonContent,
  IonFooter,
  IonHeader,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonModal,
  IonNote,
  IonPopover,
  IonProgressBar,
  IonRow,
  IonSegment,
  IonSegmentButton,
  IonSpinner,
  IonText,
  IonTitle,
  IonToolbar,
  ModalController,
  PopoverController,
  ToastController,
} from '@ionic/angular/standalone';
import {
  FeedOrRepository,
  tagsToString,
} from '../feed-builder/feed-builder.component';
import { RepositoryService } from '../../services/repository.service';
import { ArrayElement } from '../../types';
import { BubbleColor, BubbleComponent } from '../bubble/bubble.component';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { dateFormat, SessionService } from '../../services/session.service';
import { RecordService } from '../../services/record.service';
import { ServerConfigService } from '../../services/server-config.service';
import { isUndefined, sortBy, without } from 'lodash-es';
import { Subscription } from 'rxjs';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { relativeTimeOrElse } from '../agents/agents.component';
import dayjs from 'dayjs';
import { AnnotationService } from '../../services/annotation.service';
import { AuthGuardService } from '../../guards/auth-guard.service';
import { FetchPolicy } from '@apollo/client/core';
import { addIcons } from 'ionicons';
import {
  addOutline,
  closeOutline,
  cloudDownloadOutline,
  cloudUploadOutline,
  codeOutline,
  flagOutline,
  gitBranchOutline,
  listOutline,
  locationOutline,
  logoRss,
  pencilOutline,
  pulseOutline,
  refreshOutline,
  settingsOutline,
  star,
  starOutline,
  trashOutline,
} from 'ionicons/icons';
import { FileService } from '../../services/file.service';
import { SelectableEntity } from '../../modals/selection-modal/selection-modal.component';
import { DatePipe, NgClass } from '@angular/common';
import { PaginationComponent } from '../pagination/pagination.component';
import { RemoveIfProdDirective } from '../../directives/remove-if-prod/remove-if-prod.directive';
import { HistogramComponent } from '../histogram/histogram.component';
import { ImageDiffComponent } from '../image-diff/image-diff.component';
import { TextDiffComponent } from '../text-diff/text-diff.component';
import { PlayerComponent } from '../player/player.component';
import { FeedBuilderModalModule } from '../../modals/feed-builder-modal/feed-builder-modal.module';
import { TagsModalModule } from '../../modals/tags-modal/tags-modal.module';
import { SelectionModalModule } from '../../modals/selection-modal/selection-modal.module';
import { CodeEditorModalModule } from '../../modals/code-editor-modal/code-editor-modal.module';
import { SearchAddressModalModule } from '../../modals/search-address-modal/search-address-modal.module';
import { RepositoryModalModule } from '../../modals/repository-modal/repository-modal.module';

export type RecordWithFornmControl = Record & {
  fc: FormControl<boolean>;
};

type ViewMode = 'list' | 'diff' | 'histogram';

type Pair<A, B> = {
  a: A;
  b: B;
};

@Component({
  selector: 'app-feed-details',
  templateUrl: './feed-details.component.html',
  styleUrls: ['./feed-details.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonSpinner,
    IonRow,
    IonChip,
    IonToolbar,
    IonButtons,
    IonModal,
    IonHeader,
    IonTitle,
    IonButton,
    IonIcon,
    IonLabel,
    BubbleComponent,
    IonContent,
    IonList,
    IonItem,
    IonNote,
    IonText,
    IonFooter,
    PaginationComponent,
    IonBadge,
    RemoveIfProdDirective,
    RouterLink,
    IonPopover,
    NgClass,
    IonCol,
    IonSegment,
    FormsModule,
    ReactiveFormsModule,
    IonSegmentButton,
    HistogramComponent,
    ImageDiffComponent,
    TextDiffComponent,
    IonCheckbox,
    PlayerComponent,
    DatePipe,
    FeedBuilderModalModule,
    TagsModalModule,
    SelectionModalModule,
    CodeEditorModalModule,
    SearchAddressModalModule,
    RepositoryModalModule,
    IonProgressBar,
  ],
  standalone: true,
})
export class FeedDetailsComponent implements OnInit, OnDestroy {
  private readonly modalService = inject(ModalService);
  private readonly authGuard = inject(AuthGuardService);
  private readonly fileService = inject(FileService);
  private readonly alertCtrl = inject(AlertController);
  private readonly annotationService = inject(AnnotationService);
  private readonly popoverCtrl = inject(PopoverController);
  private readonly recordService = inject(RecordService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly toastCtrl = inject(ToastController);
  private readonly router = inject(Router);
  protected readonly serverConfig = inject(ServerConfigService);
  private readonly sessionService = inject(SessionService);
  private readonly repositoryService = inject(RepositoryService);
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly modalCtrl = inject(ModalController);

  readonly repository = input.required<RepositoryFull>();
  readonly sourcesModal = viewChild<IonModal>('sourcesModalRef');

  readonly track = input<boolean>();

  protected documents: RecordWithFornmControl[] = [];
  protected feedUrl: string;

  protected readonly GqlVisibility = GqlVisibility;
  protected readonly dateFormat = dateFormat;
  showFullDescription: boolean = false;
  protected playDocument: Record;
  private currentUserId: string;
  private subscriptions: Subscription[] = [];
  currentDocumentsPage: number;
  currentSourcesPage: number = 0;
  sources: ArrayElement<RepositoryFull['sources']>[];

  fromNow = relativeTimeOrElse;

  protected loading: boolean;
  protected isOwner: boolean;
  protected selectAllFc = new FormControl<boolean>(false);
  protected selectedCount: number = 0;
  viewModeFc = new FormControl<ViewMode>('list');
  viewModeList: ViewMode = 'list';
  viewModeHistogram: ViewMode = 'histogram';
  viewModeDiff: ViewMode = 'diff';
  protected compareByField: GqlRecordField | undefined;
  protected readonly GqlProductName = GqlVertical;
  protected readonly compareByPixel: GqlRecordField = GqlRecordField.Pixel;

  private seed = Math.random();
  // harvestsModalId: string = `open-harvests-modal-${this.seed}`;
  settingsModalId: string = `open-settings-modal-${this.seed}`;
  protected loadingSources: boolean = false;

  constructor() {
    addIcons({
      closeOutline,
      addOutline,
      listOutline,
      pulseOutline,
      gitBranchOutline,
      flagOutline,
      starOutline,
      star,
      logoRss,
      settingsOutline,
      codeOutline,
      cloudDownloadOutline,
      trashOutline,
      refreshOutline,
      pencilOutline,
      locationOutline,
      cloudUploadOutline,
    });
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async ngOnInit() {
    await this.fetchRepository();
    this.subscriptions.push(
      this.sessionService.getSession().subscribe((session) => {
        this.currentUserId = session.user?.id;
        this.assessIsOwner();
      }),
      this.activatedRoute.queryParams.subscribe((queryParams) => {
        if (queryParams.modal) {
          if (queryParams.modal === ModalName.editRepository) {
            this.editRepository(
              queryParams.accordion ? [queryParams.accordion] : [],
            );
          }
        }
      }),
      this.selectAllFc.valueChanges.subscribe((isChecked) => {
        this.documents.forEach((document) =>
          document.fc.setValue(isChecked, { emitEvent: false }),
        );
        if (isChecked) {
          this.selectedCount = this.documents.length;
        } else {
          this.selectedCount = 0;
        }
        this.changeRef.detectChanges();
      }),
    );
    await this.fetchPage();
    this.changeRef.detectChanges();
  }

  private async fetchRepository(fetchPolicy: FetchPolicy = 'cache-first') {
    const repository = this.repository();
    if (repository.product === GqlVertical.VisualDiff) {
      this.viewModeFc.setValue('diff');
    }
    this.compareByField = repository.plugins.find(
      (plugin) =>
        plugin.pluginId === GqlFeedlessPlugins.OrgFeedlessDiffEmailForward,
    )?.params?.org_feedless_diff_email_forward?.compareBy?.field;

    if (
      repository.visibility === GqlVisibility.IsPrivate &&
      repository.shareKey?.length > 0
    ) {
      this.feedUrl = `${this.serverConfig.apiUrl}/f/${repository.id}/atom?skey=${repository.shareKey}`;
    } else {
      this.feedUrl = `${this.serverConfig.apiUrl}/f/${repository.id}/atom`;
    }
    this.changeRef.detectChanges();
  }

  hostname(url: string): string {
    try {
      return new URL(url).hostname;
    } catch (e) {
      return 'Unknown';
    }
  }

  async editRepository(accordions: RepositoryModalAccordion[] = []) {
    const componentProps: RepositoryModalComponentProps = {
      repository: this.repository(),
      openAccordions: accordions,
    };
    await this.modalService.openRepositoryEditor(componentProps);
    await this.popoverCtrl.dismiss();
  }

  dismissModal() {
    this.modalCtrl.dismiss();
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

  protected async fetchPage(page: number = 0) {
    this.currentDocumentsPage = page;
    this.selectAllFc.setValue(false);
    this.loading = true;
    this.changeRef.detectChanges();
    const documents = await this.recordService.findAllByRepositoryId(
      {
        cursor: {
          page,
          pageSize: 10,
        },
        where: {
          repository: {
            id: this.repository().id,
          },
        },
      },
      'network-only',
    );
    this.documents = documents.map((document) => {
      const fc = new FormControl<boolean>(false);
      this.subscriptions.push(
        fc.valueChanges.subscribe((isChecked) => {
          if (isChecked) {
            this.selectedCount++;
          } else {
            this.selectedCount--;
          }
          this.selectAllFc.setValue(this.selectedCount !== 0, {
            emitEvent: false,
          });
        }),
      );
      return {
        ...document,
        fc: fc,
      };
    });
    this.loading = false;
    this.changeRef.detectChanges();
  }

  getRetentionStrategy(): string {
    const repository = this.repository();
    if (repository.retention.maxAgeDays || repository.retention.maxCapacity) {
      if (repository.retention.maxAgeDays && repository.retention.maxCapacity) {
        return `${repository.retention.maxAgeDays} days, ${repository.retention.maxCapacity} items`;
      } else {
        if (repository.retention.maxAgeDays) {
          return `${repository.retention.maxAgeDays} days`;
        } else {
          return `${repository.retention.maxCapacity} items`;
        }
      }
    } else {
      return 'Auto';
    }
  }

  async deleteRepository() {
    await this.popoverCtrl.dismiss();
    const alert = await this.alertCtrl.create({
      header: 'Delete Feed?',
      message: `You won't be able to recover it.`,
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
            await this.repositoryService.deleteRepository({
              id: this.repository().id,
            });
            await this.router.navigateByUrl('/feeds?reload=true');
          },
        },
      ],
    });
    await alert.present();
  }

  stringifyTags(source: ArrayElement<RepositoryFull['sources']>) {
    return tagsToString(source.tags) || 'Add tags';
  }

  stringifyLocalization(source: ArrayElement<RepositoryFull['sources']>) {
    const { latLng } = source;
    return latLng
      ? `(${latLng.lat.toFixed(4)},${latLng.lon.toFixed(4)})`
      : 'Add geo tag';
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
            this.assessIsOwner();
            this.fetchSources(this.currentSourcesPage, 'network-only');
            this.changeRef.detectChanges();
          },
        },
      ],
    });
    await alert.present();
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
                          lon: parseFloat(`${geoTag.lon}`),
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

  getTags(document: Record) {
    return tagsToString(document.tags);
  }

  async editSource(source: RepositorySource = null) {
    await this.modalService.openFeedBuilder(
      {
        source: this.repositoryService.toSourceInput(
          await this.repositoryService.getSourceFullByRepository(
            this.repository().id,
            source.id,
          ),
        ),
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
          }
          this.changeRef.detectChanges();
        }
      },
    );
  }

  playAudio(document: Record): void {
    this.playDocument = document;
  }

  getDocumentUrl(document: Record): string {
    if (this.track()) {
      return `${this.serverConfig.apiUrl}/article/${document.id}`;
    } else {
      return document.url;
    }
  }

  private assessIsOwner() {
    this.isOwner = this.repository()?.ownerId === this.currentUserId;
    this.changeRef.detectChanges();
  }

  async deleteAllSelected() {
    const selected = this.documents.filter((document) => document.fc.value);
    await this.recordService.removeById({
      where: {
        repository: {
          id: this.repository().id,
        },
        id: {
          in: selected.map((document) => document.id),
        },
      },
    });
    this.documents = without(this.documents, ...selected);
    this.selectAllFc.setValue(false);
    this.fetchPage(this.currentDocumentsPage);
    this.changeRef.detectChanges();
  }

  getDocumentPairs() {
    const pairs: Pair<Record, Record>[] = [];
    for (let i = 0; i < this.documents.length - 1; i++) {
      pairs.push({
        a: this.documents[i + 1],
        b: this.documents[i],
      });
    }
    return pairs;
  }

  async refreshSources() {
    await this.repositoryService.updateRepository({
      where: {
        id: this.repository().id,
      },
      data: {
        nextUpdateAt: {
          set: null,
        },
      },
    });
    this.changeRef.detectChanges();

    const toast = await this.toastCtrl.create({
      message: 'Refresh scheduled',
      duration: 3000,
      color: 'success',
    });

    await toast.present();
  }

  async showCode() {
    await this.modalService.openCodeEditorModal({
      title: 'JSON Editor',
      text: JSON.stringify(
        await this.repositoryService.getRepositoryInputWithSourcesAndFlow(
          this.repository(),
        ),
        null,
        2,
      ),
      contentType: 'json',
    });
    // todo use editor data
  }

  // openLogsModal(
  //   harvest: Pick<
  //     GqlHarvest,
  //     'startedAt' | 'finishedAt' | 'itemsAdded' | 'itemsIgnored' | 'logs'
  //   >,
  // ) {
  //   const props: CodeEditorModalComponentProps = {
  //     title: 'Log Output',
  //     contentType: 'text',
  //     readOnly: true,
  //     controls: false,
  //     text: harvest.logs,
  //   };
  //   return this.modalService.openCodeEditorModal(props);
  // }

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

  diffInSeconds(a: number, b: number) {
    return dayjs(a).diff(b, 'seconds');
  }

  async starRepository() {
    await this.authGuard.assertLoggedIn();
    await this.annotationService.createAnnotation({
      where: {
        repository: {
          id: this.repository().id,
        },
      },
      annotation: {
        upVote: {
          set: true,
        },
      },
    });
    await this.fetchRepository('network-only');
  }

  async unstarRepository() {
    await this.annotationService.deleteAnnotation({
      where: {
        id: this.getUpvoteAnnotation().id,
      },
    });
    await this.fetchRepository('network-only');
  }

  getStartCount(): string | number {
    const upVotes = this.repository()?.annotations?.upVotes || 0;
    if (upVotes >= 1000) {
      return (upVotes / 1000.0).toFixed(1) + 'k';
    } else {
      return upVotes;
    }
  }

  hasCurrentUserStarred(): boolean {
    return !isUndefined(this.getUpvoteAnnotation());
  }

  private getUpvoteAnnotation(): Annotation | undefined {
    return this.repository().annotations?.votes?.find((v) => v.upVote);
  }

  getText(document: Record) {
    if (this.currentUserId) {
      return document.text;
    } else {
      return document.text.substring(0, 150);
    }
  }

  async exportRepository() {
    await this.repositoryService.downloadRepositories(
      [this.repository()],
      `feedless-repo-${this.repository().id}.json`,
    );
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

  async fetchSources(page: number, fetchPolicy: FetchPolicy = 'cache-first') {
    this.currentSourcesPage = page;
    this.loadingSources = true;
    this.sources = [];
    this.changeRef.detectChanges();
    try {
      this.sources = await this.repositoryService.getSourcesByRepository(
        this.repository().id,
        {
          cursor: {
            page,
          },
        },
        fetchPolicy,
      );
    } finally {
      this.loadingSources = false;
    }
    this.changeRef.detectChanges();
  }

  async openSourcesModal() {
    await Promise.all([
      this.fetchSources(this.currentSourcesPage),
      this.sourcesModal().present(),
    ]);
  }
}
