import { Component, Input, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { compact, split } from 'lodash-es';
import {
  ImporterModalRole,
  ImportModalComponentProps,
  ImportModalData,
} from '../import-modal/import-modal.component';
import { GqlNativeGenericOrFragmentWatchFeedCreateInput } from '../../../generated/graphql';

// type UrlConverter = (urls: string[]) => GqlNativeGenericOrFragmentWatchFeedCreateInput[];

export interface ImportFromMarkupModalComponentProps
  extends ImportModalComponentProps {
  kind: string;
  convertToGraphqlStatement: (
    urls: string[]
  ) => GqlNativeGenericOrFragmentWatchFeedCreateInput[];
}

@Component({
  selector: 'app-import-from-markup-modal',
  templateUrl: './import-from-markup-modal.component.html',
  styleUrls: ['./import-from-markup-modal.component.scss'],
})
export class ImportFromMarkupModalComponent
  implements OnInit, ImportFromMarkupModalComponentProps
{
  @Input()
  kind: string;
  @Input()
  convertToGraphqlStatement: (
    urls: string[]
  ) => GqlNativeGenericOrFragmentWatchFeedCreateInput[];

  constructor(private readonly modalCtrl: ModalController) {}

  ngOnInit() {}

  cancel() {
    return this.modalCtrl.dismiss();
  }

  dismissModal(data: ImportModalData, role: ImporterModalRole) {
    return this.modalCtrl.dismiss(data, role);
  }

  async importFeedsOnly(data: string) {
    await this.dismissModal(this.parse(data), ImporterModalRole.feedsOnly);
  }

  importFeedsIntoOneBucket(data: string) {
    return this.dismissModal(this.parse(data), ImporterModalRole.bucket);
  }

  private parse(data: string): ImportModalData {
    const urls = compact(split(data, new RegExp('[\n \t]'))).map((url) =>
      url.trim()
    );
    return {
      feeds: this.convertToGraphqlStatement(urls),
    };
  }
}
