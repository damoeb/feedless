import { Component, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
  ActionSheetButton,
  ActionSheetController,
  ModalController,
} from '@ionic/angular';
import { Pagination } from '../../services/pagination.service';
import {
  FilterData,
  Filters,
} from '../filter-toolbar/filter-toolbar.component';
import {
  GqlContentCategoryTag,
  GqlGenericFeed,
  GqlNativeFeedStatus,
  Maybe,
} from '../../../generated/graphql';
import { BucketService } from '../../services/bucket.service';
import {
  BasicImporter,
  ImporterService,
} from '../../services/importer.service';
import { BasicNativeFeed } from '../../services/feed.service';
import { FilteredList } from '../filtered-list';
import {
  WizardComponent,
  WizardComponentProps,
} from '../wizard/wizard/wizard.component';
import { FormControl } from '@angular/forms';
import { enumToMap, toOrderBy } from '../../pages/feeds/feeds.page';

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
      options: enumToMap(GqlContentCategoryTag),
    },
    status: {
      name: 'status',
      control: new FormControl<GqlNativeFeedStatus[]>([
        GqlNativeFeedStatus.Ok,
        GqlNativeFeedStatus.NotFound,
        GqlNativeFeedStatus.Disabled,
        GqlNativeFeedStatus.ServiceUnavailable
      ]),
      options: enumToMap(GqlNativeFeedStatus),
    },
  };

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly modalCtrl: ModalController,
    private readonly bucketService: BucketService,
    private readonly importerService: ImporterService,
    readonly actionSheetCtrl: ActionSheetController
  ) {
    super('source', actionSheetCtrl);
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
        modalTitle: 'Add Source',
      },
    };
    const modal = await this.modalCtrl.create({
      component: WizardComponent,
      componentProps: updateFeed,
      backdropDismiss: false,
    });
    await modal.present();
  }

  async handleImporterAction(importer: Importer, event: any) {
    switch (event.detail.value) {
      case 'delete':
        await this.importerService.deleteImporter(importer.id);
        break;
    }
  }

  hasStatusNotFound(status: GqlNativeFeedStatus): boolean {
    return GqlNativeFeedStatus.NotFound === status;
  }

  hasNeverBeenFetched(status: GqlNativeFeedStatus): boolean {
    return GqlNativeFeedStatus.NeverFetched === status;
  }
}
