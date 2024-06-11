import { Component, Input, OnInit } from '@angular/core';
import { Outline } from '../../services/opml.service';
import { ModalController } from '@ionic/angular';
import { FormControl, FormGroup } from '@angular/forms';
import { RepositoryService } from '../../services/repository.service';
import { GqlFeedlessPlugins, GqlPluginExecutionInput, GqlProductCategory, GqlVisibility } from '../../../generated/graphql';

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
  styleUrls: ['./import-opml-modal.component.scss'],
})
export class ImportOpmlModalComponent
  implements OnInit, ImportOpmlModalComponentProps
{
  @Input()
  outlines: Outline[];
  fcOutlines: FcOutline[];

  formFg = new FormGroup({
    applyFulltextPlugin: new FormControl<boolean>(false),
    applyPrivacyPlugin: new FormControl<boolean>(false),
  });

  private formControls: FormControl<boolean>[] = [];

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly repositoryService: RepositoryService,
  ) {}

  ngOnInit() {
    this.fcOutlines = this.outlines.map((outline) =>
      this.addFormControl(outline),
    );
  }

  async importSelected() {
    const plugins: GqlPluginExecutionInput[] = [];
    if (this.formFg.value.applyFulltextPlugin) {
      plugins.push({
        pluginId: GqlFeedlessPlugins.OrgFeedlessFulltext,
        params: {
          org_feedless_fulltext: {
            readability: true,
            inheritParams: false,
          },
        },
      });
    }
    if (this.formFg.value.applyPrivacyPlugin) {
      plugins.push({
        pluginId: GqlFeedlessPlugins.OrgFeedlessPrivacy,
        params: {},
      });
    }

    await this.repositoryService.createRepositories({
      repositories: this.fcOutlines
        .filter((outline) => outline.fc.value)
        .map((fc) => ({
          product: GqlProductCategory.RssProxy,
          sources: [
            {
              page: {
                url: fc.xmlUrl,
              },
              emit: [
                {
                  selectorBased: {
                    xpath: {
                      value: '/',
                    },
                    expose: {
                      transformers: [
                        {
                          pluginId: GqlFeedlessPlugins.OrgFeedlessFeed,
                          params: {},
                        },
                      ],
                    },
                  },
                },
              ],
            },
          ],
          sourceOptions: {
            refreshCron: '0 0 0 * * *',
          },
          sinkOptions: {
            title: fc.title,
            description: `${fc.text} ${fc.htmlUrl}`.trim(),
            visibility: GqlVisibility.IsPrivate,
            plugins,
          },
        })),
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
}
