import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
  ViewEncapsulation,
} from '@angular/core';
import { isFunction, isNull, isUndefined } from 'lodash';
import {
  GqlImporterCreateInput,
  GqlPuppeteerWaitUntil,
} from '../../../../generated/graphql';
import {
  FeedDiscoveryResult,
  TransientGenericFeed,
} from '../../../services/feed.service';
import { ModalController } from '@ionic/angular';
import { AuthService } from 'src/app/services/auth.service';

export enum WizardStepId {
  source,
  feeds,
  output,
  finalize,
  refineGenericFeed,
  refineNativeFeed,
  pageChange,
}

interface WizardButton {
  label: string;
  toStepId: WizardStepId;
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

export enum WizardMode {
  feedFromPageChange = 'feedFromPageChange',
  feedFromWebsite = 'feedFromWebsite',
  feedFromFeed = 'feedFromFeed',
  undecided = 'undecided',
}

export interface WizardContext {
  importer?: GqlImporterCreateInput;
  genericFeed?: TransientGenericFeed;
  feedUrl: string;
  url: string;
  prerender: boolean;
  discovery?: FeedDiscoveryResult;
  prerenderWaitUntil: GqlPuppeteerWaitUntil;
  prerenderScript: string;
  wizardMode: WizardMode;
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
export class WizardComponent implements OnInit {
  currentStepIndex = 0;

  wizardStepIds = WizardStepId;

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
      visible: (context) => context.wizardMode === WizardMode.feedFromWebsite,
      buttons: (context) => [
        {
          label: 'Next',
          color: 'success',
          isHidden: isNullish(context.genericFeed),
          toStepId: WizardStepId.refineGenericFeed,
        },
        {
          label: 'Next',
          color: 'success',
          isDisabled: true,
          isHidden:
            !isNullish(context.genericFeed) || !isBlank(context.feedUrl),
          toStepId: WizardStepId.refineGenericFeed,
        },
        {
          label: 'Next',
          color: 'success',
          isHidden: isBlank(context.feedUrl),
          toStepId: WizardStepId.refineNativeFeed,
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
          toStepId: WizardStepId.finalize,
        },
      ],
    },
    {
      id: WizardStepId.pageChange,
      visible: (context) =>
        context.wizardMode === WizardMode.feedFromPageChange,
      headerLabel: 'Page Change',
      buttons: () => [
        {
          label: 'Next',
          color: 'success',
          toStepId: WizardStepId.refineNativeFeed,
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
          toStepId: WizardStepId.finalize,
        },
      ],
    },
    {
      id: WizardStepId.finalize,
      headerLabel: 'Finalize',
      buttons: () => [
        {
          label: 'Save',
          color: 'success',
          toStepId: WizardStepId.finalize,
        },
      ],
    },
  ];

  context: WizardContext = {
    url: 'https://www.telepolis.de/news-atom.xml',
    feedUrl: '',
    wizardMode: WizardMode.undecided,
    prerender: false,
    prerenderWaitUntil: GqlPuppeteerWaitUntil.Load,
    prerenderScript: '',
  };
  history: WizardStepId[] = [];
  viewCode = false;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly authService: AuthService,
    private readonly modalCtrl: ModalController
  ) {}

  async ngOnInit(): Promise<void> {
    await this.authService.requireAnyAuthToken();
  }

  isActiveStep(stepId: WizardStepId): boolean {
    return this.getIndexForStepId(stepId) === this.currentStepIndex;
  }

  updateContext(event: Partial<WizardContext>) {
    this.context = {
      ...this.context,
      ...event,
    };
    this.changeRef.detectChanges();
  }

  getCurrentButtons(): WizardButton[] {
    if (isFunction(this.steps[this.currentStepIndex].buttons)) {
      return this.steps[this.currentStepIndex].buttons(this.context);
    }
    return [];
  }

  goToStep(stepId: WizardStepId) {
    this.history.push(this.steps[this.currentStepIndex].id);
    this.currentStepIndex = this.getIndexForStepId(stepId);
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
    return !this.steps[this.currentStepIndex].hideHeader;
  }

  goBack() {
    const previousId = this.history.pop();
    this.currentStepIndex = this.getIndexForStepId(previousId);
  }

  closeModal() {
    this.modalCtrl.dismiss();
  }

  hasExited(step: WizardStep): boolean {
    return this.history.includes(step.id);
  }

  getContextJson(): string {
    return JSON.stringify(this.context, null, 2);
  }

  updateContextJson($event: any) {
    console.log($event);
  }

  private getIndexForStepId(stepId: WizardStepId): number {
    return this.steps.findIndex((step) => step.id === stepId);
  }
}
