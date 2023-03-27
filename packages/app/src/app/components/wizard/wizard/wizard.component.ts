import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
  ViewEncapsulation,
} from '@angular/core';
import { isFunction, isNull, isUndefined } from 'lodash-es';
import {
  GqlBucketCreateOrConnectInput,
  GqlFetchOptionsInput,
  GqlImportersCreateInput,
  GqlNativeFeedCreateOrConnectInput,
  GqlPuppeteerWaitUntil,
} from '../../../../generated/graphql';
import { FeedService } from '../../../services/feed.service';
import { ModalController } from '@ionic/angular';
import { AuthService } from 'src/app/services/auth.service';
import { ProfileService } from '../../../services/profile.service';
import { Router } from '@angular/router';
import { WizardHandler } from '../wizard-handler';
import { ServerSettingsService } from '../../../services/server-settings.service';

export enum WizardStepId {
  source = 'source',
  feeds = 'feeds',
  bucket = 'bucket',
  refineGenericFeed = 'refineGenericFeed',
  refineNativeFeed = 'refineNativeFeed',
  pageChange = 'pageChange',
}

interface WizardButton {
  label: string;
  // toStepId: WizardStepId;
  handler: (event: MouseEvent) => void;
  color?: string;
  isHidden?: boolean;
}

interface WizardStep {
  id: WizardStepId;
  placeholder?: string;
  buttons?: (context: WizardContext) => WizardButton[];
}

export interface WizardContext {
  // source
  feedUrl: string;
  modalTitle: string;

  // feeds
  isCurrentStepValid: boolean;

  // fetch-options
  fetchOptions?: GqlFetchOptionsInput;
  bucket?: GqlBucketCreateOrConnectInput;
  feed?: GqlNativeFeedCreateOrConnectInput;
  importer?: Pick<
    GqlImportersCreateInput,
    'filter' | 'webhook' | 'email' | 'autoRelease'
  >;

  history: WizardStepId[];
  stepId: WizardStepId;
  exitAfterStep?: WizardStepId;
}

export interface WizardComponentProps {
  initialContext: Partial<WizardContext>;
}

const isNullish = (value: any) => isUndefined(value) || isNull(value);

const defaultContext: WizardContext = {
  feedUrl: '',
  isCurrentStepValid: false,
  modalTitle: 'Create Feed',

  fetchOptions: {
    prerender: false,
    prerenderScript: '',
    prerenderWaitUntil: GqlPuppeteerWaitUntil.Load,
    prerenderWithoutMedia: false,
    websiteUrl: '',
  },
  history: [],
  stepId: WizardStepId.source,
};

@Component({
  selector: 'app-wizard',
  templateUrl: './wizard.component.html',
  styleUrls: ['./wizard.component.scss'],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardComponent implements OnInit, WizardComponentProps {
  wizardStepIds = WizardStepId;
  initialContext: Partial<WizardContext>;
  steps: WizardStep[] = [
    {
      id: WizardStepId.source,
    },
    {
      id: WizardStepId.feeds,
      buttons: (context) => [
        {
          label: 'Next',
          color: 'success',
          handler: () => {
            if (
              !isNullish(context.feed.create.nativeFeed) ||
              !isNullish(context.feed.connect)
            ) {
              this.goToStep(WizardStepId.refineNativeFeed);
            } else {
              if (!isNullish(context.feed.create.genericFeed)) {
                this.goToStep(WizardStepId.refineGenericFeed);
              }
            }
          },
        },
      ],
    },
    {
      id: WizardStepId.refineGenericFeed,
      buttons: () => [
        {
          label: 'Next',
          color: 'success',
          handler: () => this.goToStep(WizardStepId.bucket),
        },
      ],
    },
    {
      id: WizardStepId.pageChange,
      buttons: () => [
        {
          label: 'Next',
          color: 'success',
          handler: () => this.goToStep(WizardStepId.refineGenericFeed),
        },
      ],
    },
    {
      id: WizardStepId.refineNativeFeed,
      buttons: () => [
        {
          label: 'Next',
          color: 'success',
          isHidden: !this.profileService.isAuthenticated(),
          handler: () => this.goToStep(WizardStepId.bucket),
        },
        {
          label: 'Save and Login',
          color: 'success',
          isHidden: this.profileService.isAuthenticated(),
          handler: async () => {
            await this.modalCtrl.dismiss(this.handler.getContext(), 'login');
          },
        },
      ],
    },
    {
      id: WizardStepId.bucket,
      buttons: () => [
        {
          label: 'Save',
          color: 'success',
          handler: () => this.finalize(),
        },
      ],
    },
  ];

  handler: WizardHandler;

  viewCode = false;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly authService: AuthService,
    private readonly feedService: FeedService,
    private readonly router: Router,
    private readonly profileService: ProfileService,
    private readonly serverSettingsService: ServerSettingsService,
    private readonly modalCtrl: ModalController
  ) {}

  async ngOnInit(): Promise<void> {
    await this.authService.requireAnyAuthToken();
    await this.initWizard(this.initialContext);
  }

  isActiveStep(stepId: WizardStepId): boolean {
    return stepId === this.handler.getCurrentStepId();
  }

  nextButtons(): WizardButton[] {
    const currentStepId = this.handler.getCurrentStepId();
    if (this.handler.getContext().exitAfterStep === currentStepId) {
      return [
        {
          label: 'Save',
          color: 'success',
          handler: () => this.finalize(),
        },
      ];
    } else {
      const step = this.findStepById(currentStepId);
      if (isFunction(step.buttons)) {
        return step.buttons(this.handler.getContext());
      }
      return [];
    }
  }

  goToStep(stepId: WizardStepId) {
    const { history } = this.handler.getContext();
    history.push(this.handler.getCurrentStepId());
    return this.handler.updateContext({
      stepId,
    });
  }

  async goBack(): Promise<void> {
    const { history } = this.handler.getContext();
    await this.handler.updateContext({
      stepId: history.pop(),
    });
  }

  closeModal() {
    return this.modalCtrl.dismiss(this.handler.getContext(), 'cancel');
  }

  getContextJson(): string {
    return JSON.stringify(this.handler.getContext(), null, 2);
  }

  async updateContextJson(initialContextStr: string) {
    await this.initWizard(JSON.parse(initialContextStr));
    this.viewCode = false;
  }

  private finalize() {
    return this.modalCtrl.dismiss(this.handler.getContext(), 'persist');
  }

  private async initWizard(initialContext: Partial<WizardContext>) {
    this.handler = new WizardHandler(
      {
        ...defaultContext,
        ...initialContext,
      },
      this.feedService,
      this.serverSettingsService,
      this.changeRef
    );
    await this.handler.init();
    this.changeRef.detectChanges();
  }

  private findStepById(stepId: WizardStepId): WizardStep {
    return this.steps.find((step) => step.id === stepId);
  }
}
