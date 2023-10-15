import { Component, Input, OnInit } from '@angular/core';
import { Outline } from '../../services/opml.service';
import { ModalController } from '@ionic/angular';
import { FormControl } from '@angular/forms';
import { flatten, pick } from 'lodash-es';
import {
  ImporterModalRole,
  ImportModalComponentProps,
  ImportModalData,
} from '../import-modal/import-modal.component';
import { GqlNativeGenericOrFragmentFeedCreateInput } from '../../../generated/graphql';

export interface ImportOpmlModalComponentProps
  extends ImportModalComponentProps {
  outlines: Outline[];
}

type FcOutline = {
  title: string;
  text?: string;
  xmlUrl?: string;
  htmlUrl?: string;
  fc?: FormControl<boolean>;
  outlines?: FcOutline[];
};

@Component({
  selector: 'app-import-opml',
  templateUrl: './import-opml-modal.component.html',
  styleUrls: ['./import-opml-modal.component.scss'],
})
export class ImportOpmlModalComponent
  implements OnInit, ImportOpmlModalComponentProps
{
  @Input()
  outlines: Outline[];
  fcOutlines: FcOutline[];

  private formControls: FormControl<boolean>[] = [];

  constructor(private readonly modalCtrl: ModalController) {}

  dismissModal(data: ImportModalData, role: ImporterModalRole) {
    return this.modalCtrl.dismiss(data, role);
  }

  ngOnInit() {
    this.fcOutlines = this.outlines.map((outline) =>
      this.addFormControl(outline),
    );
  }

  importFeedsAndAggregate() {
    const dismissal: ImportModalData = {
      feeds: this.getSelectedOutlines(),
    };
    return this.dismissModal(dismissal, ImporterModalRole.bucket);
  }

  importOpml() {
    const dismissal: ImportModalData = {
      buckets: this.outlines.map((outline) => ({
        title: outline.title,
        description: outline.text,
        outlines: flatten(outline.outlines),
      })),
    };
    return this.dismissModal(dismissal, ImporterModalRole.multipleBuckets);
  }

  importFeeds() {
    const dismissal: ImportModalData = {
      feeds: this.getSelectedOutlines(),
    };
    return this.dismissModal(dismissal, ImporterModalRole.feedsOnly);
  }

  selectAll() {
    this.formControls.forEach((formControl) => formControl.setValue(true));
  }

  selectNone() {
    this.formControls.forEach((formControl) => formControl.setValue(false));
  }

  cancel() {
    return this.modalCtrl.dismiss();
  }

  private addFormControl(outline: Outline): FcOutline {
    if (outline.xmlUrl) {
      const fc = new FormControl<boolean>(false);
      this.formControls.push(fc);
      return {
        ...outline,
        fc,
        outlines: outline.outlines?.map((childOutline) =>
          this.addFormControl(childOutline),
        ),
      };
    }
    return {
      ...outline,
      outlines: outline.outlines?.map((childOutline) =>
        this.addFormControl(childOutline),
      ),
    };
  }

  private filterSelectedOutlines(selected: FcOutline[], outlines: FcOutline[]) {
    selected.push(...outlines?.filter((o) => o.fc?.value));
    outlines
      ?.filter((o) => o.outlines)
      .forEach((o) => this.filterSelectedOutlines(selected, o.outlines));
  }

  private getSelectedOutlines(): GqlNativeGenericOrFragmentFeedCreateInput[] {
    const selected: FcOutline[] = [];
    this.filterSelectedOutlines(selected, this.fcOutlines);
    return selected
      .map<Outline>(
        (outline) =>
          pick<Outline>(
            outline,
            'xmlUrl',
            'title',
            'text',
            'htmlUrl',
          ) as Outline,
      )
      .map<GqlNativeGenericOrFragmentFeedCreateInput>((outline) => ({
        nativeFeed: {
          title: outline.title,
          feedUrl: outline.xmlUrl,
          websiteUrl: outline.htmlUrl,
          description: outline.text,
        },
      }));
  }
}
