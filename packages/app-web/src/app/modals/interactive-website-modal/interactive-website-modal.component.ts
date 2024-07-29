import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { debounce, interval, Subscription } from 'rxjs';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import {
  GqlScrapeActionInput,
  GqlScrapeRequestInput,
  GqlXyPosition,
} from '../../../generated/graphql';
import { ItemReorderEventDetail, ModalController } from '@ionic/angular';
import { ScrapeResponse } from '../../graphql/types';
import { ServerConfigService } from '../../services/server-config.service';
import { ScrapeController } from '../../components/interactive-website/scrape-controller';

type BrowserActionType = keyof GqlScrapeActionInput;

export type BrowserAction = {
  type: FormControl<BrowserActionType>;
  clickParams?: FormControl<GqlXyPosition>;
  raw?: FormControl<GqlScrapeActionInput>;
};

export interface FeedBuilderData {
  request: GqlScrapeRequestInput;
  response: ScrapeResponse;
}

export type InteractiveWebsiteModalComponentProps = {
  scrapeRequest: GqlScrapeRequestInput;
};

@Component({
  selector: 'app-interactive-website-modal',
  templateUrl: './interactive-website-modal.component.html',
  styleUrls: ['./interactive-website-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InteractiveWebsiteModalComponent
  implements OnInit, OnDestroy, InteractiveWebsiteModalComponentProps
{
  @Input({ required: true })
  scrapeRequest: GqlScrapeRequestInput;

  protected scrapeController: ScrapeController;

  formFg = new FormGroup({
    prerendered: new FormControl<boolean>(false),
    prerenderingOptions: new FormGroup({
      resolutionX: new FormControl<number>(1024),
      resolutionY: new FormControl<number>(768),
      mobile: new FormControl<boolean>(false),
      landscape: new FormControl<boolean>(false),
    }),
  });

  actionsFg = new FormArray<FormGroup<BrowserAction>>([]);
  private subscriptions: Subscription[] = [];
  actionTypes: (keyof GqlScrapeActionInput)[] = [
    'click',
    'type',
    'fetch',
    'execute',
    'extract',
    'header',
    'select',
    'wait',
  ];

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly modalCtrl: ModalController,
    protected readonly serverConfig: ServerConfigService,
  ) {}

  ngOnInit() {
    console.log(this.scrapeRequest);
    this.scrapeController = new ScrapeController(this.scrapeRequest);

    this.scrapeRequest.flow.sequence
      .map((action) => this.convertScrapeActionToActionFg(action))
      .forEach((actionFg) => {
        this.actionsFg.push(actionFg);
      });
    this.changeRef.detectChanges();

    this.subscriptions.push(
      this.actionsFg.valueChanges
        .pipe(debounce(() => interval(800)))
        .subscribe(() => {
          if (this.actionsFg.valid) {
            console.log('this.actionsFg.valueChanges');
            this.scrapeController.scrapeRequest.flow.sequence =
              this.getActionsRequestFragment();
            // console.log('this.scrapeController.scrapeRequest.flow.sequence', this.scrapeController.scrapeRequest.flow.sequence);
            this.scrapeController.actionsChanges.emit();
          }
        }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  addAction() {
    if (this.actionsFg.valid) {
      const index = this.getActionFgs().findIndex(
        (action) => action.value.type === 'execute',
      );
      this.actionsFg.insert(
        index,
        new FormGroup<BrowserAction>({
          type: new FormControl<BrowserActionType>('click'),
          clickParams: new FormControl<GqlXyPosition>(null, [
            Validators.required,
          ]),
        }),
      );
    }
  }

  getActionFgs(): FormGroup<BrowserAction>[] {
    const actions: FormGroup<BrowserAction>[] = [];
    for (let i = 0; i < this.actionsFg.length; i++) {
      actions.push(this.actionsFg.at(i));
    }

    return actions;
  }

  removeAction(index: number) {
    this.actionsFg.removeAt(index);
  }

  getPositionLabel(action: FormGroup<BrowserAction>) {
    const clickParams = action.value.clickParams;
    if (clickParams) {
      return `(${clickParams.x}, ${clickParams.y})`;
    } else {
      return 'Click on Screenshot';
    }
  }

  dismissModal() {
    return this.modalCtrl.dismiss();
  }

  applyChanges() {
    const data: FeedBuilderData = {
      request: this.scrapeController.getScrapeRequest(),
      response: this.scrapeController.response,
    };
    return this.modalCtrl.dismiss(data);
  }

  private getActionsRequestFragment(): GqlScrapeActionInput[] {
    return this.getActionFgs().map((action) => {
      if (action.value.type === 'click') {
        return {
          click: {
            position: {
              x: action.value.clickParams.x,
              y: action.value.clickParams.y,
            },
          },
        };
      } else {
        return {
          [action.value.type]: action.value.raw[action.value.type],
        };
      }
    });
  }

  private convertScrapeActionToActionFg(
    action: GqlScrapeActionInput,
  ): FormGroup<BrowserAction> | undefined {
    if (action.click?.position) {
      return new FormGroup<BrowserAction>({
        type: new FormControl<BrowserActionType>(this.getActionType(action)),
        clickParams: new FormControl<GqlXyPosition>(action.click.position, [
          Validators.required,
        ]),
      });
    } else {
      return new FormGroup<BrowserAction>({
        type: new FormControl<BrowserActionType>(this.getActionType(action)),
        raw: new FormControl<GqlScrapeActionInput>(action),
      });
    }
  }

  pickPosition(action: FormGroup<BrowserAction>) {
    this.scrapeController.pickPoint.emit((position) =>
      action.patchValue({
        clickParams: {
          x: position.x,
          y: position.y,
        },
      }),
    );
  }

  private getActionType(action: GqlScrapeActionInput): BrowserActionType {
    const keys = Object.keys(action) as (keyof GqlScrapeActionInput)[];
    return keys.find((key) => action[key]);
  }
}
