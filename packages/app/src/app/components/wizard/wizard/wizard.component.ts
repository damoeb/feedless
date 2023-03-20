import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, ViewEncapsulation } from '@angular/core';
import { isFunction, isNull, isUndefined } from 'lodash';
import { GqlPuppeteerWaitUntil } from '../../../../generated/graphql';
import { FeedService, TransientGenericFeed } from '../../../services/feed.service';
import { ModalController } from '@ionic/angular';
import { AuthService } from 'src/app/services/auth.service';
import { ProfileService } from '../../../services/profile.service';
import { Router } from '@angular/router';
import { WizardHandler } from '../wizard-handler';

export enum WizardStepId {
  source,
  feeds,
  output,
  bucket,
  refineGenericFeed,
  refineNativeFeed,
  pageChange,
}

interface WizardButton {
  label: string;
  // toStepId: WizardStepId;
  handler: (event: MouseEvent) => void;
  color?: string;
  isDisabled?: boolean;
  isHidden?: boolean;
}

interface WizardStep {
  id: WizardStepId;
  headerLabel: string;
  hideHeader?: boolean;
  visible?: (context: WizardContext) => boolean;
  placeholder?: string;
  buttons?: (context: WizardContext) => WizardButton[];
}

export enum WizardFlow {
  feedFromPageChange = 'feedFromPageChange',
  feedFromWebsite = 'feedFromWebsite',
  feedFromFeed = 'feedFromFeed',
  undecided = 'undecided',
}

export interface WizardContext {
  wizardFlow: WizardFlow;

  // source
  feedUrl: string;

  // feeds
  genericFeed?: TransientGenericFeed;

  // fetch-options
  url: string;
  prerender: boolean;
  prerenderWaitUntil: GqlPuppeteerWaitUntil;
  prerenderScript: string;

  // internal
  history: WizardStepId[];
  currentStepId: WizardStepId;
}

export interface WizardComponentProps {
  initialContext: Partial<WizardContext>;
}

const isBlank = (value: string) =>
  isUndefined(value) || isNull(value) || value.length === 0;
const isNullish = (value: any) => isUndefined(value) || isNull(value);

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
      headerLabel: 'Source',
      visible: () => false,
      hideHeader: true,
    },
    {
      id: WizardStepId.feeds,
      headerLabel: 'Feeds',
      visible: (context) => context.wizardFlow === WizardFlow.feedFromWebsite,
      buttons: (context) => [
        {
          label: 'Next',
          color: 'success',
          isHidden: isNullish(context.genericFeed),
          handler: () => this.goToStep(WizardStepId.refineGenericFeed)
        },
        {
          label: 'Next',
          color: 'success',
          isDisabled: true,
          isHidden:
            !isNullish(context.genericFeed) || !isBlank(context.feedUrl),
          handler: () => this.goToStep(WizardStepId.refineGenericFeed)
        },
        {
          label: 'Next',
          color: 'success',
          isHidden: isBlank(context.feedUrl),
          handler: () => this.goToStep(WizardStepId.refineGenericFeed)
        },
      ],
    },
    {
      id: WizardStepId.refineGenericFeed,
      visible: (context) => !isUndefined(context.genericFeed),
      headerLabel: 'Refine',
      buttons: () => [
        {
          label: 'Next',
          color: 'success',
          handler: () => this.goToStep(WizardStepId.bucket)
        },
      ],
    },
    {
      id: WizardStepId.pageChange,
      visible: (context) =>
        context.wizardFlow === WizardFlow.feedFromPageChange,
      headerLabel: 'Page Change',
      buttons: () => [
        {
          label: 'Next',
          color: 'success',
          handler: () => this.goToStep(WizardStepId.refineGenericFeed)
        },
      ],
    },
    {
      id: WizardStepId.refineNativeFeed,
      headerLabel: 'Refine',
      buttons: () => [
        {
          label: 'Next',
          color: 'success',
          isHidden: !this.profileService.isAuthenticated(),
          handler: () => this.goToStep(WizardStepId.bucket)
        },
        {
          label: 'Save and Login',
          color: 'success',
          isHidden: this.profileService.isAuthenticated(),
          handler: async () => {
            await this.modalCtrl.dismiss(this.handler.getContext(), 'login');
          }
        },
      ],
    },
    {
      id: WizardStepId.bucket,
      headerLabel: 'Finalize',
      buttons: () => [
        {
          label: 'Save',
          color: 'success',
          handler: () => this.finalize()
        },
      ],
    },
  ];

  context: WizardContext = {
    url: 'https://www.telepolis.de/news-atom.xml',
    feedUrl: '',
    wizardFlow: WizardFlow.undecided,
    prerender: false,
    prerenderWaitUntil: GqlPuppeteerWaitUntil.Load,
    prerenderScript: '',
    history: [],
    currentStepId: WizardStepId.source
  };

  handler: WizardHandler;

  viewCode = false;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly authService: AuthService,
    private readonly feedService: FeedService,
    private readonly router: Router,
    private readonly profileService: ProfileService,
    private readonly modalCtrl: ModalController
  ) {}

  async ngOnInit(): Promise<void> {
    await this.authService.requireAnyAuthToken();
    this.initHandler(this.initialContext);
  }

  isActiveStep(stepId: WizardStepId): boolean {
    return stepId === this.handler.getCurrentStepId();
  }

  getCurrentButtons(): WizardButton[] {
    const step = this.findStepById(this.handler.getCurrentStepId());
    if (isFunction(step.buttons)) {
      return step.buttons(this.context);
    }
    return [];
  }

  goToStep(stepId: WizardStepId) {
    const { history } = this.handler.getContext();
    history.push(this.handler.getCurrentStepId());
    this.handler.updateContext({
      currentStepId: stepId
    });
  }

  isReachableByIndex(stepIndex: number): boolean {
    if (isFunction(this.steps[stepIndex].visible)) {
      return this.steps[stepIndex].visible(this.context);
    }
    return true;
  }

  activateStep(step: WizardStep) {
    // this.history.pop()
  }

  hideHeader(): boolean {
    if (this.handler?.getContext()) {
      const { currentStepId } = this.handler.getContext();
    return !this.findStepById(currentStepId).hideHeader;
    }
    return false;
  }

  async goBack() {
    const { history } = this.handler.getContext();
    await this.handler.updateContext({
      currentStepId: history.pop()
    });
  }

  closeModal() {
    return this.modalCtrl.dismiss();
  }

  hasExited(step: WizardStep): boolean {
    const { history } = this.handler.getContext();
    return history.includes(step.id);
  }

  getContextJson(): string {
    return JSON.stringify(this.context, null, 2);
  }

  updateContextJson($event: any) {
    console.log($event);
  }

  private finalize() {
    return this.modalCtrl.dismiss(this.handler.getContext());
  }

  private initHandler(initialContext: Partial<WizardContext>) {
    this.handler = new WizardHandler(
      {
        ...this.context,
        ...initialContext
      },
      this.feedService,
      this.changeRef
    );
    this.changeRef.detectChanges();
  }

  private findStepById(stepId: WizardStepId): WizardStep {
    return this.steps.find(step => step.id === stepId);
  }
}
