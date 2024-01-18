import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ProfileService } from '../../services/profile.service';
import { Subscription } from 'rxjs';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Embeddable } from '../../components/embedded-website/embedded-website.component';
import { BoundingBox, XyPosition } from '../../components/embedded-image/embedded-image.component';
import {
  GqlPuppeteerWaitUntil,
  GqlScrapeDebugResponse,
  GqlScrapeDebugTimes,
  GqlScrapeResponse,
  GqlViewPort,
  GqlXyPosition
} from '../../../generated/graphql';
import { isNull, isUndefined } from 'lodash-es';
import { ItemReorderEventDetail } from '@ionic/angular';
import { ScrapeService } from '../../services/scrape.service';
import { ScrapedElement } from '../../graphql/types';
import { Maybe } from 'graphql/jsutils/Maybe';

type CompareBy = 'pixel' | 'text' | 'markup';

type VisualDiffScrapeResponse = Pick<GqlScrapeResponse, 'url' | 'failed' | 'errorMessage'> & {
  debug: Pick<GqlScrapeDebugResponse, 'console' | 'cookies' | 'contentType' | 'statusCode' | 'screenshot' | 'html'> & {
    metrics: Pick<GqlScrapeDebugTimes, 'queue' | 'render'>;
    viewport?: Maybe<Pick<GqlViewPort, 'width' | 'height'>>
  };
  elements: Array<ScrapedElement>
};

@Component({
  selector: 'app-visual-diff',
  templateUrl: './visual-diff.page.html',
  styleUrls: ['./visual-diff.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VisualDiffPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  urlFc = new FormControl<string>('', [Validators.required]);
  isDarkMode: boolean;
  embedScreenshot: Embeddable;
  pickElementDelegate: (xpath: string | null) => void;
  pickPositionDelegate: (position: GqlXyPosition | null) => void;
  pickBoundingBoxDelegate: (boundingBox: BoundingBox | null) => void;

  form = new FormGroup({
    selector: new FormControl<string>('', [
      Validators.required,
      Validators.minLength(1),
    ]),
    compareBy: new FormControl<CompareBy>('pixel', [Validators.required]),
    frequency: new FormControl<string>('day', [Validators.required]),
    subject: new FormControl<string>('', [Validators.required]),
    email: new FormControl<string>('', [Validators.required, Validators.email]),
    // boundingBox: new FormGroup({
    //   leftTop: new FormControl<string>('')
    // })
  });
  private scrapeResponse: VisualDiffScrapeResponse;
  actions: string[] = [];
  busy = false;

  constructor(
    readonly profile: ProfileService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly scrapeService: ScrapeService,
  ) {}

  ngOnInit() {
    this.subscriptions.push(
      this.profile.watchColorScheme().subscribe((isDarkMode) => {
        this.isDarkMode = isDarkMode;
        this.changeRef.detectChanges();
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  handlePickedXpath(xpath: string) {
    if (this.pickElementDelegate) {
      // this.highlightXpath = xpath;
      this.pickElementDelegate(xpath);
      this.pickElementDelegate = null;
    }
  }

  handlePickedPosition(position: XyPosition | null) {
    if (this.pickPositionDelegate) {
      this.pickPositionDelegate(position);
      this.pickPositionDelegate = null;
      this.changeRef.detectChanges();
    }
  }

  protected isDefined(v: any | undefined): boolean {
    return !isNull(v) && !isUndefined(v);
  }

  handlePickedBoundingBox(boundingBox: BoundingBox | null) {
    if (this.pickBoundingBoxDelegate) {
      this.pickBoundingBoxDelegate(boundingBox);
      this.pickBoundingBoxDelegate = null;
      this.changeRef.detectChanges();
    }
  }

  async scrape() {
    if (this.busy) {
      return;
    }
    this.busy = true;
    this.changeRef.detectChanges();

    try {
      const url = 'https://telepolis.de';
      const scrapeResponse = await this.scrapeService.scrape({
        page: {
          url,
          prerender: {}
        },
        emit: [],
        debug: {
          screenshot: true
        }
      });

      this.embedScreenshot = {
        mimeType: 'image/png',
        data: scrapeResponse.debug.screenshot,
        url,
        viewport: scrapeResponse.debug.viewport,
      };
      this.scrapeResponse = scrapeResponse;
    } finally {
      this.busy = false;
    }
    this.changeRef.detectChanges();
  }

  addAction() {
    this.actions.push('1');
  }

  handleReorderActions(ev: CustomEvent<ItemReorderEventDetail>) {
    console.log('Dragged from index', ev.detail.from, 'to', ev.detail.to);
    ev.detail.complete();
  }}
