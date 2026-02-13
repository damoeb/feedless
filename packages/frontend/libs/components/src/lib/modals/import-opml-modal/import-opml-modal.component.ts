import { Component, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { Outline, RepositoryService } from '../../services';
import {
  IonButton,
  IonButtons,
  IonCheckbox,
  IonContent,
  IonFooter,
  IonHeader,
  IonItem,
  IonLabel,
  IonList,
  IonNote,
  IonTitle,
  IonToolbar,
  ModalController,
} from '@ionic/angular/standalone';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
} from '@angular/forms';
import {
  GqlFeedlessPlugins,
  GqlPluginExecutionInput,
  GqlScrapeActionInput,
  GqlVertical,
  GqlVisibility,
} from '@feedless/graphql-api';
import { addIcons } from 'ionicons';
import { closeOutline } from 'ionicons/icons';
import { isPlatformBrowser } from '@angular/common';
import { IconComponent } from '../../components/icon/icon.component';

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
  standalone: true,
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IconComponent,
    IonContent,
    IonList,
    IonItem,
    IonCheckbox,
    FormsModule,
    ReactiveFormsModule,
    IonLabel,
    IonFooter,
    IonNote,
  ],
})
export class ImportOpmlModalComponent
  implements OnInit, ImportOpmlModalComponentProps
{
  private readonly modalCtrl = inject(ModalController);
  private readonly repositoryService = inject(RepositoryService);

  outlines: Outline[];
  fcOutlines: FcOutline[];

  formFg = new FormGroup({
    applyFulltextPlugin: new FormControl<boolean>(false),
    applyPrivacyPlugin: new FormControl<boolean>(false),
  });

  private formControls: FormControl<boolean>[] = [];
  private readonly platformId = inject(PLATFORM_ID);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ closeOutline });
    }
  }

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
            summary: true,
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

    await this.repositoryService.createRepositories(
      this.fcOutlines
        .filter((outline) => outline.fc.value)
        .map((fc) => {
          const actions: GqlScrapeActionInput[] = [
            {
              fetch: {
                get: {
                  url: {
                    literal: fc.xmlUrl,
                  },
                },
              },
            },
            {
              execute: {
                pluginId: GqlFeedlessPlugins.OrgFeedlessFeed,
                params: {},
              },
            },
          ];
          return {
            product: GqlVertical.RssProxy,
            sources: [
              {
                title: fc.title,
                flow: {
                  sequence: actions,
                },
              },
            ],
            title: fc.title,
            withShareKey: true,
            refreshCron: '0 0 0 * * *',
            description: `${fc.text} ${fc.htmlUrl}`.trim(),
            visibility: GqlVisibility.IsPrivate,
            plugins,
          };
        }),
    );
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
