import { Component, Input, OnInit } from '@angular/core';
import { Outline } from '../../services/opml.service';
import { ModalController } from '@ionic/angular';
import { FormControl, FormGroup } from '@angular/forms';
import { SourceSubscriptionService } from '../../services/source-subscription.service';
import { GqlFeedlessPlugins, GqlPluginExecutionInput, GqlProductName, GqlVisibility } from '../../../generated/graphql';

export interface ImportOpmlModalComponentProps {
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
  styleUrls: ['./import-opml-modal.component.scss']
})
export class ImportOpmlModalComponent
  implements OnInit, ImportOpmlModalComponentProps {
  @Input()
  outlines: Outline[];
  fcOutlines: FcOutline[];

  formFg = new FormGroup({
    applyFulltextPlugin: new FormControl<boolean>(false),
    applyPrivacyPlugin: new FormControl<boolean>(false)
  });

  private formControls: FormControl<boolean>[] = [];

  constructor(private readonly modalCtrl: ModalController,
              private readonly sourceSubscriptionService: SourceSubscriptionService) {
  }

  ngOnInit() {
    this.fcOutlines = this.outlines.map((outline) =>
      this.addFormControl(outline)
    );
  }

  async importSelected() {
    const plugins: GqlPluginExecutionInput[] = [];
    if (this.formFg.value.applyFulltextPlugin) {
      plugins.push({
        pluginId: GqlFeedlessPlugins.OrgFeedlessFulltext,
        params: {
          fulltext: {
            readability: true
          }
        }
      });
    }
    if (this.formFg.value.applyPrivacyPlugin) {
      plugins.push({
          pluginId: GqlFeedlessPlugins.OrgFeedlessPrivacy,
          params: {}
        }
      );
    }

    await this.sourceSubscriptionService.createSubscriptions({
      subscriptions: this.fcOutlines.filter(outline => outline.fc.value).map(fc => ({
          product: GqlProductName.RssBuilder,
          sources: [
            {
              page: {
                url: fc.xmlUrl
              },
              emit: [
                {
                  selectorBased: {
                    xpath: {
                      value: '/'
                    },
                    expose: {
                      transformers: [
                        {
                          pluginId: GqlFeedlessPlugins.OrgFeedlessFeed,
                          params: {}
                        }
                      ]
                    }
                  }
                }
              ]
            }
          ],
          sourceOptions: {
            refreshCron: '0 0 0 * * *'
          },
          sinkOptions: {
            title: fc.title,
            description: `${fc.text} ${fc.htmlUrl}`.trim(),
            visibility: GqlVisibility.IsPrivate,
            plugins
          }
        }
      ))
    });
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
        )
      };
    }
    return {
      ...outline,
      outlines: outline.outlines?.map((childOutline) =>
        this.addFormControl(childOutline)
      )
    };
  }

  // private filterSelectedOutlines(selected: FcOutline[], outlines: FcOutline[]) {
  //   selected.push(...outlines?.filter((o) => o.fc?.value));
  //   outlines
  //     ?.filter((o) => o.outlines)
  //     .forEach((o) => this.filterSelectedOutlines(selected, o.outlines));
  // }
  // private getSelectedOutlines(): GqlNativeGenericOrFragmentFeedCreateInput[] {
  //   const selected: FcOutline[] = [];
  //   this.filterSelectedOutlines(selected, this.fcOutlines);
  //   return selected
  //     .map<Outline>(
  //       (outline) =>
  //         pick<Outline>(
  //           outline,
  //           'xmlUrl',
  //           'title',
  //           'text',
  //           'htmlUrl',
  //         ) as Outline,
  //     )
  //     .map<GqlNativeGenericOrFragmentFeedCreateInput>((outline) => ({
  //       nativeFeed: {
  //         title: outline.title,
  //         feedUrl: outline.xmlUrl,
  //         websiteUrl: outline.htmlUrl,
  //         description: outline.text,
  //       },
  //     }));
  // }
}
