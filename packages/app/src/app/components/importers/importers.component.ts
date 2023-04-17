import { Component, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ActionSheetButton, ActionSheetController, ModalController, ToastController } from '@ionic/angular';
import { Pagination } from '../../services/pagination.service';
import { FilterData, Filters } from '../filter-toolbar/filter-toolbar.component';
import { GqlContentCategoryTag, GqlGenericFeed, GqlNativeFeedStatus, Maybe } from '../../../generated/graphql';
import { BucketService } from '../../services/bucket.service';
import { BasicImporter, ImporterService } from '../../services/importer.service';
import { BasicNativeFeed } from '../../services/feed.service';
import { FilteredList } from '../filtered-list';
import { WizardComponent, WizardComponentProps, WizardContext, WizardStepId } from '../wizard/wizard/wizard.component';
import { FormControl } from '@angular/forms';
import { enumToKeyValue, toOrderBy } from '../../pages/feeds/feeds.page';
import { OverlayEventDetail } from '@ionic/core';

type Importer = BasicImporter & {
  nativeFeed: BasicNativeFeed & {
    genericFeed?: Maybe<Pick<GqlGenericFeed, 'id'>>;
  };
};

export interface ImporterFilterValues {
  tag: GqlContentCategoryTag;
  status: GqlNativeFeedStatus;
}

@Component({
  selector: 'app-importers',
  templateUrl: './importers.component.html',
  styleUrls: ['./importers.component.scss'],
})
export class ImportersComponent extends FilteredList<
  Importer,
  FilterData<ImporterFilterValues>
> {
  @Input()
  bucketId: string;
  filters: Filters<ImporterFilterValues> = {
    tag: {
      name: 'tag',
      control: new FormControl<GqlContentCategoryTag[]>([]),
      options: enumToKeyValue(GqlContentCategoryTag),
    },
    status: {
      name: 'status',
      control: new FormControl<GqlNativeFeedStatus[]>([
        GqlNativeFeedStatus.Ok,
        GqlNativeFeedStatus.NotFound,
        GqlNativeFeedStatus.Disabled,
        GqlNativeFeedStatus.ServiceUnavailable,
      ]),
      options: enumToKeyValue(GqlNativeFeedStatus),
    },
  };

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly modalCtrl: ModalController,
    private readonly toastCtrl: ToastController,
    private readonly bucketService: BucketService,
    private readonly importerService: ImporterService,
    readonly actionSheetCtrl: ActionSheetController
  ) {
    super(actionSheetCtrl);
  }

  getBulkActionButtons(): ActionSheetButton<any>[] {
    return [
      {
        text: 'Delete',
        role: 'destructive',
        handler: () => {},
      },
      {
        text: 'Disable',
        role: 'destructive',
        handler: () => {},
      },
    ];
  }

  async fetch(
    filterData: FilterData<ImporterFilterValues>,
    page: number
  ): Promise<[Importer[], Pagination]> {
    const { importers, pagination } = await this.importerService.getImporters({
      page,
      where: {
        // query: '',
        buckets: {
          oneOf: [this.bucketId],
        },
        status: {
          oneOf: filterData.filters.status,
        },
      },
      orderBy: toOrderBy(filterData.sortBy),
    });
    return [importers, pagination];
  }

  async openAddSourceModal() {
    const updateFeed: WizardComponentProps = {
      initialContext: {
        bucket: {
          connect: {
            id: this.bucketId,
          },
        },
        exitAfterStep: WizardStepId.refineNativeFeed,
        modalTitle: 'Add Source',
      },
    };
    const response = await this.openWizardModal(updateFeed);
    if (response.role) {
    }
  }

  hasStatusNotFound(status: GqlNativeFeedStatus): boolean {
    return GqlNativeFeedStatus.NotFound === status;
  }

  hasNeverBeenFetched(status: GqlNativeFeedStatus): boolean {
    return GqlNativeFeedStatus.NeverFetched === status;
  }

  async deleteImporter(
    importer: BasicImporter & {
      nativeFeed: BasicNativeFeed & {
        genericFeed?: Maybe<Pick<GqlGenericFeed, 'id'>>;
      };
    }
  ) {
    await this.importerService.deleteImporter(importer.id);
    const toast = await this.toastCtrl.create({
      message: 'Deleted',
      color: 'primary',
      duration: 3000,
    });

    await toast.present();
    await this.refetch();
  }

  async editImporter(
    importer: BasicImporter & {
      nativeFeed: BasicNativeFeed & {
        genericFeed?: Maybe<Pick<GqlGenericFeed, 'id'>>;
      };
    }
  ) {
    const updateFeed: WizardComponentProps = {
      initialContext: {
        feed: {
          connect: {
            id: importer.nativeFeed.id,
          },
        },
        importer,
        stepId: WizardStepId.refineNativeFeed,
        exitAfterStep: WizardStepId.refineNativeFeed,
        modalTitle: 'Edit Importer',
      },
    };
    const response = await this.openWizardModal(updateFeed);
    if (response.role) {
      // this.importerService.updateImporter(response.data);
    } else {
      const toast = await this.toastCtrl.create({
        message: 'Canceled',
        duration: 3000,
      });

      await toast.present();
    }
  }

  private async openWizardModal(
    props: WizardComponentProps
  ): Promise<OverlayEventDetail<WizardContext>> {
    const modal = await this.modalCtrl.create({
      component: WizardComponent,
      componentProps: props,
      backdropDismiss: false,
    });
    await modal.present();
    return modal.onDidDismiss<WizardContext>();
  }
}
