import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
  ViewEncapsulation,
} from '@angular/core';
import { isArray, isNull, isUndefined } from 'lodash-es';
import {
  GqlBucketCreateOrConnectInput,
  GqlImporterAttributesInput,
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
import { debounce, interval } from 'rxjs';
import { FetchOptions } from '../../../graphql/types';

export enum WizardStepId {
  source = 'Source',
  feeds = 'Feeds',
  bucket = 'Bucket',
  refineGenericFeed = 'Refine Feed (Generic)',
  refineNativeFeed = 'Refine Feed',
  pageFragmentWatch = 'Page Change',
}

interface WizardButton {
  label: string;
  // toStepId: WizardStepId;
  handler: (event: MouseEvent) => void;
  color?: string;
}

interface WizardStep {
  id: WizardStepId;
  placeholder?: string;
  nextButtons?: WizardButton | WizardButton[];
}

export interface WizardContext {
  // source
  feedUrl: string;
  modalTitle: string;

  // feeds
  isCurrentStepValid: boolean;

  // fetch-options
  fetchOptions?: FetchOptions;
  bucket?: GqlBucketCreateOrConnectInput;
  feed?: GqlNativeFeedCreateOrConnectInput;
  importer?: Pick<
    GqlImporterAttributesInput,
    'filter' | 'autoRelease' | 'plugins'
  >;

  history: WizardStepId[];
  busy: boolean;
  stepId: WizardStepId;
  exitAfterStep?: WizardStepId[];
}

export interface WizardComponentProps {
  initialContext: Partial<WizardContext>;
}

export const isNullish = (value: any) => isUndefined(value) || isNull(value);

export const defaultWizardContext: WizardContext = {
  feedUrl: '',
  isCurrentStepValid: false,
  modalTitle: 'Create Feed',
  busy: false,

  fetchOptions: {
    prerender: false,
    prerenderScript: '',
    prerenderWaitUntil: GqlPuppeteerWaitUntil.Load,
    websiteUrl: '',
  },
  history: [],
  stepId: WizardStepId.source,
};

export enum WizardExitRole {
  dismiss = 'dismiss',
  persistBucket = 'persistBucket',
  persistFeed = 'persistFeed',
  login = 'login',
}

@Component({
  selector: 'app-wizard',
  templateUrl: './wizard.component.html',
  styleUrls: ['./wizard.component.scss'],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardComponent
  implements OnInit, WizardComponentProps, OnDestroy
{
  wizardStepIds = WizardStepId;
  initialContext: Partial<WizardContext>;
  steps: WizardStep[] = [
    {
      id: WizardStepId.source,
    },
    {
      id: WizardStepId.feeds,
      nextButtons: {
        label: 'Next',
        color: 'success',
        handler: () => {
          const refineNativeFeed =
            !isNullish(this.handler.getContext().feed?.create?.nativeFeed) ||
            !isNullish(this.handler.getContext().feed?.connect);
          const refineGenericFeed = !isNullish(
            this.handler.getContext().feed?.create?.genericFeed
          );
          if (refineNativeFeed) {
            this.goToStep(WizardStepId.refineNativeFeed);
          } else {
            if (refineGenericFeed) {
              this.goToStep(WizardStepId.refineGenericFeed);
            }
          }
        },
      },
    },
    {
      id: WizardStepId.refineGenericFeed,
      nextButtons: this.profileService.isAuthenticated()
        ? [
            {
              label: 'Save Feed',
              handler: () => this.finalize(WizardExitRole.persistFeed),
            },
            {
              label: 'Aggregate in Bucket',
              color: 'success',
              handler: () => this.goToStep(WizardStepId.bucket),
            },
          ]
        : {
            label: 'Login to Continue',
            color: 'success',
            handler: async () => {
              await this.modalCtrl.dismiss(
                this.handler.getContext(),
                WizardExitRole.login
              );
            },
          },
    },
    {
      id: WizardStepId.pageFragmentWatch,
      nextButtons: this.profileService.isAuthenticated()
        ? {
            label: 'Save',
            color: 'success',
            handler: () => this.finalize(WizardExitRole.persistFeed),
          }
        : {
            label: 'Login to Continue',
            color: 'success',
            handler: async () => {
              await this.modalCtrl.dismiss(
                this.handler.getContext(),
                WizardExitRole.login
              );
            },
          },
    },
    {
      id: WizardStepId.refineNativeFeed,
      nextButtons: this.profileService.isAuthenticated()
        ? [
            {
              label: 'Save Feed',
              handler: () => this.finalize(WizardExitRole.persistFeed),
            },
            {
              label: 'Aggregate in Bucket',
              color: 'success',
              handler: () => this.goToStep(WizardStepId.bucket),
            },
          ]
        : {
            label: 'Login to Continue',
            color: 'success',
            handler: async () => {
              await this.modalCtrl.dismiss(
                this.handler.getContext(),
                WizardExitRole.login
              );
            },
          },
    },
    {
      id: WizardStepId.bucket,
      nextButtons: {
        label: 'Save',
        color: 'success',
        handler: () => this.finalize(WizardExitRole.persistBucket),
      },
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

  ngOnDestroy(): void {
    this.handler.destroy();
  }

  async ngOnInit(): Promise<void> {
    await this.authService.requireAnyAuthToken();
    await this.initWizard(this.initialContext);
  }

  isActiveStep(stepId: WizardStepId): boolean {
    return stepId === this.handler.getCurrentStepId();
  }

  nextButton(): WizardButton[] {
    const currentStepId = this.handler.getCurrentStepId();
    if (this.handler.getContext().exitAfterStep?.includes(currentStepId)) {
      return [
        {
          label: 'Save',
          color: 'success',
          handler: () => this.finalize(WizardExitRole.persistFeed),
        },
      ];
    } else {
      const step = this.findStepById(currentStepId);
      if (step.nextButtons) {
        if (isArray(step.nextButtons)) {
          return step.nextButtons;
        } else {
          return [step.nextButtons];
        }
      }
      return [];
    }
  }

  goToStep(stepId: WizardStepId) {
    console.log('goToStep', stepId);
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
    return this.modalCtrl.dismiss(this.handler.getContext());
  }

  getContextJson(): string {
    return JSON.stringify(this.handler.getContext(), null, 2);
  }

  async updateContextJson(initialContextStr: string) {
    await this.initWizard(JSON.parse(initialContextStr));
    this.viewCode = false;
  }

  private finalize(role: WizardExitRole) {
    return this.modalCtrl.dismiss(this.handler.getContext(), role);
  }

  private async initWizard(initialContext: Partial<WizardContext>) {
    this.changeRef.detectChanges();
    this.handler = new WizardHandler(
      {
        ...defaultWizardContext,
        ...initialContext,
      },
      this.feedService,
      this.serverSettingsService
    );
    await this.handler.init();
    this.changeRef.detectChanges();
    this.handler
      .onContextChange()
      .pipe(debounce(() => interval(200)))
      .subscribe((_) => {
        // if (!isUndefined(change.busy) || !isUndefined(change.stepId)) {
        this.changeRef.detectChanges();
        // }
      });
  }

  private findStepById(stepId: WizardStepId): WizardStep {
    return this.steps.find((step) => step.id === stepId);
  }
}
