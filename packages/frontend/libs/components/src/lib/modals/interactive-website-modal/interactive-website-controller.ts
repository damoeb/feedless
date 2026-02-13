import { ChangeDetectorRef } from '@angular/core';
import { Subscription } from 'rxjs';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import {
  GqlScrapeActionInput,
  GqlSourceInput,
  GqlXyPosition,
} from '@feedless/graphql-api';
import { ScrapeService, ServerConfigService } from '../../services';
import { SourceBuilder } from '../../source/source-builder';

export type BrowserActionType = keyof GqlScrapeActionInput;

export type BrowserAction = {
  type: FormControl<BrowserActionType>;
  clickParams?: FormControl<GqlXyPosition>;
  raw?: FormControl<GqlScrapeActionInput>;
};

export abstract class InteractiveWebsiteController {
  source: GqlSourceInput;

  sourceBuilder: SourceBuilder;
  abstract changeRef: ChangeDetectorRef;
  abstract scrapeService: ScrapeService;
  abstract serverConfig: ServerConfigService;

  protected actionsFg = new FormArray<FormGroup<BrowserAction>>([]);
  protected subscriptions: Subscription[] = [];
  actionTypes: (keyof GqlScrapeActionInput)[] = [
    'click',
    // 'type',
    // 'fetch',
    // 'execute',
    // 'extract',
    // 'header',
    // 'select',
    // 'waitFor',
  ];

  initializeController() {
    this.sourceBuilder = SourceBuilder.fromSource(
      this.source,
      this.scrapeService,
    );
    this.sourceBuilder.patchFetch({
      forcePrerender: true,
    });
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

  destroyController(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  addAction() {
    if (this.actionsFg.valid) {
      const firstExecuteOrExtractIndex = this.getActionFgs().findIndex(
        (action) => ['execute', 'extract'].includes(action.value.type),
      );
      const insertAt =
        firstExecuteOrExtractIndex == -1
          ? this.getActionFgs().length
          : firstExecuteOrExtractIndex;
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

  getActionsRequestFragment(): GqlScrapeActionInput[] {
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
