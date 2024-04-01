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
import { Embeddable } from '../../components/embedded-website/embedded-website.component';
import { XyPosition } from '../../components/embedded-image/embedded-image.component';
import {
  GqlFeedlessPlugins,
  GqlScrapeActionInput,
  GqlScrapeRequestInput,
  GqlXyPosition,
} from '../../../generated/graphql';
import { isNull, isUndefined } from 'lodash-es';
import { ModalController } from '@ionic/angular';
import { ScrapeService } from '../../services/scrape.service';
import { Router } from '@angular/router';
import { ScrapeResponse } from '../../graphql/types';

type BrowserActionType = 'click';

interface BrowserAction {
  type: FormControl<BrowserActionType>;
  clickParams: FormControl<GqlXyPosition>;
}

// export type FeedBuilderScrapeResponse = Pick<
//   GqlScrapeResponse,
//   'url' | 'failed' | 'errorMessage'
// > & {
//   debug: Pick<
//     GqlScrapeDebugResponse,
//     'console' | 'cookies' | 'contentType' | 'statusCode' | 'screenshot' | 'html'
//   > & {
//     metrics: Pick<GqlScrapeDebugTimes, 'queue' | 'render'>;
//     viewport?: Maybe<Pick<GqlViewPort, 'width' | 'height'>>;
//   };
//   elements: Array<ScrapedElement>;
// };

export interface FeedBuilderData {
  request: GqlScrapeRequestInput;
  response: ScrapeResponse;
}

@Component({
  selector: 'app-feed-builder-actions-modal',
  templateUrl: './feed-builder-actions-modal.component.html',
  styleUrls: ['./feed-builder-actions-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedBuilderActionsModalComponent implements OnInit, OnDestroy {
  @Input({ required: true })
  url: string;
  embedScreenshot: Embeddable;
  embedMarkup: Embeddable;
  pickPositionDelegate: (position: GqlXyPosition | null) => void;
  additionalWait = new FormControl<number>(0, [
    Validators.required,
    Validators.min(0),
    Validators.max(10),
  ]);
  actions = new FormArray<FormGroup<BrowserAction>>([]);
  busy = false;
  private subscriptions: Subscription[] = [];
  scrapeResponse: ScrapeResponse;
  errorMessage: string;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly modalCtrl: ModalController,
    private readonly router: Router,
    private readonly scrapeService: ScrapeService,
  ) {}

  ngOnInit() {
    this.subscriptions.push(
      this.actions.valueChanges
        .pipe(debounce(() => interval(800)))
        .subscribe(() => {
          return this.scrape();
        }),
    );

    this.scrape();

    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  handlePickedPosition(position: XyPosition | null) {
    if (this.pickPositionDelegate) {
      this.pickPositionDelegate(position);
      this.pickPositionDelegate = null;
      this.changeRef.detectChanges();
    }
  }

  async scrape() {
    if (this.busy) {
      return;
    }
    this.busy = true;
    this.errorMessage = null;
    this.changeRef.detectChanges();

    const url = this.url;

    try {
      const scrapeResponse = await this.scrapeService.scrape(
        this.getScrapeRequest(true),
      );

      this.embedScreenshot = null;
      this.changeRef.detectChanges();

      this.embedScreenshot = {
        mimeType: 'image/png',
        data: scrapeResponse.debug.screenshot,
        url,
        viewport: scrapeResponse.debug.viewport,
      };
      this.embedMarkup = {
        mimeType: scrapeResponse.debug.contentType,
        data: scrapeResponse.debug.html,
        url,
        viewport: scrapeResponse.debug.viewport,
      };
      this.scrapeResponse = scrapeResponse;
    } catch (e) {
      this.errorMessage = e.message;
    } finally {
      this.busy = false;
    }
    this.changeRef.detectChanges();
  }

  addAction() {
    if (this.actions.valid) {
      this.actions.push(
        new FormGroup<BrowserAction>({
          type: new FormControl<BrowserActionType>('click'),
          clickParams: new FormControl<GqlXyPosition>(null, [
            Validators.required,
          ]),
        }),
      );
    }
  }

  getActions(): FormGroup<BrowserAction>[] {
    const actions: FormGroup<BrowserAction>[] = [];
    for (let i = 0; i < this.actions.length; i++) {
      actions.push(this.actions.at(i));
    }

    return actions;
  }

  pickPosition(action: FormGroup<BrowserAction>) {
    this.pickPositionDelegate = (position: XyPosition) => {
      action.controls.clickParams.patchValue(position);
      this.changeRef.detectChanges();
    };
  }

  removeAction(index: number) {
    this.actions.removeAt(index);
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
      request: this.getScrapeRequest(),
      response: this.scrapeResponse,
    };
    return this.modalCtrl.dismiss(data);
  }

  isPickMode() {
    return this.isDefined(this.pickPositionDelegate);
  }

  cancelPickMode() {
    this.pickPositionDelegate = null;
  }

  protected isDefined(v: any | undefined): boolean {
    return !isNull(v) && !isUndefined(v);
  }

  private getActionsRequestFragment(): GqlScrapeActionInput[] {
    return this.getActions()
      .filter((action) => action.valid)
      .map((action) => {
        return {
          click: {
            position: {
              x: action.value.clickParams.x,
              y: action.value.clickParams.y,
            },
          },
        };
      });
  }

  private getScrapeRequest(debug = false): GqlScrapeRequestInput {
    return {
      page: {
        url: this.url,
        prerender: {
          additionalWaitSec: this.additionalWait.value,
        },
        actions: this.getActionsRequestFragment(),
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
                  pluginId: GqlFeedlessPlugins.OrgFeedlessFeeds,
                  params: {},
                },
              ],
            },
          },
        },
      ],
      debug: {
        screenshot: debug,
        html: debug,
      },
    };
  }
}
