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
  GqlSourceInput,
  GqlXyPosition,
} from '../../../generated/graphql';
import { ModalController } from '@ionic/angular';
import { ServerConfigService } from '../../services/server-config.service';
import { SourceBuilder } from '../../components/interactive-website/source-builder';
import { ScrapeService } from '../../services/scrape.service';

type BrowserActionType = keyof GqlScrapeActionInput;

export type BrowserAction = {
  type: FormControl<BrowserActionType>;
  clickParams?: FormControl<GqlXyPosition>;
  raw?: FormControl<GqlScrapeActionInput>;
};

export type InteractiveWebsiteModalComponentProps = {
  source: GqlSourceInput;
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
  source: GqlSourceInput;

  protected sourceBuilder: SourceBuilder;

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
    'waitFor',
  ];
  hideNonUiActions: boolean = true;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly modalCtrl: ModalController,
    private readonly scrapeService: ScrapeService,
    protected readonly serverConfig: ServerConfigService,
  ) {}

  ngOnInit() {
    this.sourceBuilder = SourceBuilder.fromSource(
      this.source,
      this.scrapeService,
    );
    this.source.flow.sequence
      .map((action) => this.convertScrapeActionToActionFg(action))
      .forEach((actionFg) => {
        this.actionsFg.push(actionFg);
      });
    this.changeRef.detectChanges();

    this.subscriptions.push(
      this.actionsFg.valueChanges.subscribe(() => {
        if (this.actionsFg.valid) {
          this.sourceBuilder.overwriteFlow(this.getActionsRequestFragment());
          this.sourceBuilder.events.actionsChanges.emit();
        }
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  addAction() {
    if (this.actionsFg.valid) {
      const firstExecuteIndex = this.getActionFgs().findIndex(
        (action) => action.value.type === 'execute',
      );
      const insertAt =
        firstExecuteIndex == -1
          ? this.getActionFgs().length
          : firstExecuteIndex;
      this.actionsFg.insert(
        insertAt,
        new FormGroup<BrowserAction>({
          type: new FormControl<BrowserActionType>('click'),
          clickParams: new FormControl<GqlXyPosition>(null, [
            Validators.required,
          ]),
        }),
      );
    } else {
      console.log(this.actionsFg.errors);
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
    return this.modalCtrl.dismiss(this.sourceBuilder);
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
      const actionFg = new FormGroup<BrowserAction>({
        type: new FormControl<BrowserActionType>(this.getActionType(action)),
        raw: new FormControl<GqlScrapeActionInput>(action),
      });
      // debugger
      // actionFg.disable()
      return actionFg;
    }
  }

  pickPosition(action: FormGroup<BrowserAction>) {
    this.sourceBuilder.events.pickPoint.emit((position) =>
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
