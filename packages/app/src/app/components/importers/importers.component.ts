import { Component, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
  ActionSheetButton,
  ActionSheetController,
  ModalController,
} from '@ionic/angular';
import { Pagination } from '../../services/pagination.service';
import {
  FilterQuery,
  Filters,
} from '../filter-toolbar/filter-toolbar.component';
import {
  GqlArticleReleaseStatus,
  GqlArticleType,
  GqlContentCategoryTag,
  GqlContentTypeTag,
  GqlGenericFeed,
  Maybe,
} from '../../../generated/graphql';
import { BucketService } from '../../services/bucket.service';
import {
  BasicImporter,
  ImporterService,
} from '../../services/importer.service';
import { BasicNativeFeed } from '../../services/feed.service';
import { FilteredList } from '../filtered-list';
import { WizardComponent } from '../wizard/wizard/wizard.component';
import { FormControl } from '@angular/forms';

type Importer = BasicImporter & {
  nativeFeed: BasicNativeFeed & {
    genericFeed?: Maybe<Pick<GqlGenericFeed, 'id'>>;
  };
};

@Component({
  selector: 'app-importers',
  templateUrl: './importers.component.html',
  styleUrls: ['./importers.component.scss'],
})
export class ImportersComponent extends FilteredList<Importer, FilterQuery> {
  @Input()
  bucketId: string;
  filters: Filters = {
    tag: {
      name: 'tag',
      control: new FormControl<GqlContentCategoryTag[]>([]),
      options: Object.values(GqlContentCategoryTag),
    },
    content: {
      name: 'content',
      control: new FormControl<GqlContentTypeTag[]>(
        Object.values(GqlContentTypeTag)
      ),
      options: Object.values(GqlContentTypeTag),
    },
    status: {
      name: 'status',
      control: new FormControl<GqlArticleReleaseStatus[]>([
        GqlArticleReleaseStatus.Released,
      ]),
      options: Object.values(GqlArticleReleaseStatus),
    },
    type: {
      name: 'type',
      control: new FormControl<GqlArticleType[]>([GqlArticleType.Feed]),
      options: Object.values(GqlArticleType),
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

  async fetch(filterData: FilterQuery): Promise<[Importer[], Pagination]> {
    const { importers } = await this.bucketService.getBucketById(this.bucketId);
    const pagination: Pagination = {
      isLast: true,
      isFirst: true,
      isEmpty: false,
      page: 0,
    };
    return [importers, pagination];
  }

  async openAddSourceModal() {
    // const updateFeed: WizardComponentProps = {
    //   withContext: {
    //     data: {
    //       bucket: await this.bucketService.getBucketById(this.bucketId)
    //     },
    //     title: 'Add Source',
    //     wizardFlow: WizardFlow.undecided,
    //   }
    // };
    // const modal = await this.modalCtrl.create({
    //   component: WizardComponent,
    //   componentProps: updateFeed,
    //   backdropDismiss: false,
    // });
    // await modal.present();
  }

  async handleImporterAction(importer: Importer, event: any) {
    switch (event.detail.value) {
      case 'delete':
        await this.importerService.deleteImporter(importer.id);
        break;
    }
  }
}
