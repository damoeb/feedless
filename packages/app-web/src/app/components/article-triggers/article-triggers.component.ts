import { Component, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
  ActionSheetButton,
  ActionSheetController,
  ModalController,
  ToastController,
} from '@ionic/angular';
import { Pagination } from '../../services/pagination.service';
import {
  FilterData,
  Filters,
} from '../filter-toolbar/filter-toolbar.component';
import {
  GqlArticleTrigger,
  GqlContentCategoryTag,
  GqlGenericFeed,
  GqlNativeFeedStatus,
  Maybe
} from '../../../generated/graphql';
import { BucketService } from '../../services/bucket.service';
import {
  BasicImporter,
  ImporterService,
} from '../../services/importer.service';
import { BasicNativeFeed } from '../../services/feed.service';
import { FilteredList } from '../filtered-list';
import {
  isNullish,
  WizardComponent,
  WizardComponentProps,
  WizardContext,
  WizardStepId,
} from '../wizard/wizard/wizard.component';
import { FormControl } from '@angular/forms';
import { enumToKeyValue, toOrderBy } from '../../pages/feeds/feeds.page';
import { OverlayEventDetail } from '@ionic/core';
import { isUndefined } from 'lodash-es';
import { ImporterFilterValues } from '../importers/importers.component';

export type ArticleTrigger = GqlArticleTrigger;

@Component({
  selector: 'app-article-triggers',
  templateUrl: './article-triggers.component.html',
  styleUrls: ['./article-triggers.component.scss'],
})
export class ArticleTriggersComponent extends FilteredList<
  ArticleTrigger,
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

  async deleteImporter(importer: EditImporterParams) {
    await this.importerService.deleteImporter(importer.id);
    await this.toast('Deleted', 'primary');
    await this.refetch();
  }

  async editImporter(importer: EditImporterParams) {
    const updateFeed: WizardComponentProps = this.generateWizardProps(importer);
    const response = await this.openWizardModal(updateFeed);
    console.log('data', response.data, 'role', response.role);
    if (response.role) {
      // await this.importerService.updateImporter({
      //   where: {
      //     id: importer.id,
      //   },
      // });
    } else {
      await this.toast('Canceled');
    }
  }

  private async toast(message: string, color?: string) {
    const toast = await this.toastCtrl.create({
      message,
      color,
      duration: 3000,
    });

    await toast.present();
  }

}
