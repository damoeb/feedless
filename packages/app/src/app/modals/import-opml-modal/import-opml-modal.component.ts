import { Component, Input, OnInit } from '@angular/core';
import { Outline } from '../../services/opml.service';
import { ModalController } from '@ionic/angular';
import { FormControl } from '@angular/forms';
import { GqlNativeFeedCreateOrConnectInput } from '../../../generated/graphql';
import { pick } from 'lodash';

export interface ImportOpmlModalComponentProps {
  outlines: Outline[];
}

export type FcOutline = {
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

  ngOnInit() {
    this.fcOutlines = this.outlines.map((outline) =>
      this.addFormControl(outline)
    );
  }

  importFeeds() {
    const selected: FcOutline[] = [];
    this.filterSelectedOutlines(selected, this.fcOutlines);
    const feeds = selected.map<Outline>(
      (outline) => pick<Outline>(outline, 'xmlUrl', 'title', 'text', 'htmlUrl') as Outline
    );
    return this.modalCtrl.dismiss(feeds, 'success');
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
          this.addFormControl(childOutline)
        ),
      };
    }
    return {
      ...outline,
      outlines: outline.outlines?.map((childOutline) =>
        this.addFormControl(childOutline)
      ),
    };
  }

  private filterSelectedOutlines(selected: FcOutline[], outlines: FcOutline[]) {
    selected.push(...outlines?.filter((o) => o.fc?.value));
    outlines
      ?.filter((o) => o.outlines)
      .forEach((o) => this.filterSelectedOutlines(selected, o.outlines));
  }
}
