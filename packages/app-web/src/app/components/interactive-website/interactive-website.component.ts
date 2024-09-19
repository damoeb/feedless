import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { first, last, parseInt } from 'lodash-es';
import { GqlLogStatement } from '../../../generated/graphql';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { SourceBuilder } from './source-builder';
import { map, merge, Subscription } from 'rxjs';
import { ServerConfigService } from '../../services/server-config.service';
import { Embeddable } from '../embedded-image/embedded-image.component';
import { ScrapeResponse } from '../../graphql/types';

type ViewMode = 'markup' | 'image';

@Component({
  selector: 'app-interactive-website',
  templateUrl: './interactive-website.component.html',
  styleUrls: ['./interactive-website.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InteractiveWebsiteComponent implements OnInit, OnDestroy {
  @Input({ required: true })
  sourceBuilder: SourceBuilder;

  @Input()
  showUrl: boolean = false;

  @Output()
  loadingChange: EventEmitter<boolean> = new EventEmitter<boolean>(false);

  @Output()
  segmentChange: EventEmitter<string> = new EventEmitter<string>();

  scaleFactor: number = 0.7;
  minScaleFactor: number = 0.5;
  maxScaleFactor: number = 1.3;
  loading = false;

  embedScreenshot: Embeddable;
  embedMarkup: Embeddable;

  additionalWaitMin = 0;
  additionalWaitMax = 10;

  private subscriptions: Subscription[] = [];

  formFg = new FormGroup({
    url: new FormControl<string>(''),
    prerendered: new FormControl<boolean>(false),
    prerenderingOptions: new FormGroup({
      additionalWait: new FormControl<number>(0, [
        Validators.required,
        Validators.min(this.additionalWaitMin),
        Validators.max(this.additionalWaitMax),
      ]),
      resolutionX: new FormControl<number>(1024),
      resolutionY: new FormControl<number>(768),
      mobile: new FormControl<boolean>(false),
      landscape: new FormControl<boolean>(false),
    }),
  });
  protected errorMessage: string;
  protected hasScreenshot: boolean;
  protected hasMarkup: boolean;
  protected logs: GqlLogStatement[];

  protected readonly parseInt = parseInt;

  constructor(
    protected readonly serverConfig: ServerConfigService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  viewModeFc = new FormControl<ViewMode | string>('markup');
  viewModeImage: ViewMode = 'image';
  viewModeMarkup: ViewMode = 'markup';
  pickMode = false;

  async ngOnInit() {
    this.formFg.patchValue({
      url: this.sourceBuilder.getUrl(),
    });
    this.segmentChange.emit(this.viewModeFc.value);

    if (this.sourceBuilder.response) {
      this.handleScrapeResponse(this.sourceBuilder.response);
    } else {
      this.scrape();
    }

    this.subscriptions.push(
      merge(
        this.formFg.valueChanges,
        this.formFg.controls.prerenderingOptions.controls.additionalWait.valueChanges.pipe(
          map((wait) =>
            this.sourceBuilder.patchFetch({ additionalWaitSec: wait }),
          ),
        ),
      ).subscribe(() => {
        this.sourceBuilder.events.actionsChanges.emit();
      }),
      this.viewModeFc.valueChanges.subscribe((value) =>
        this.segmentChange.emit(value),
      ),
      this.sourceBuilder.events.pickPoint.subscribe(() => {
        this.viewModeFc.patchValue(this.viewModeImage);
      }),
      // this.sourceBuilder.events.showElements.subscribe(() => {
      //   this.viewModeFc.patchValue(this.viewModeMarkup);
      // }),
      this.sourceBuilder.events.actionsChanges.subscribe(() => {
        this.scrape();
      }),
      this.sourceBuilder.events.pickElement.subscribe(() => {
        this.viewModeFc.patchValue(this.viewModeMarkup);
      }),
      this.sourceBuilder.events.pickArea.subscribe(() => {
        this.viewModeFc.patchValue(this.viewModeImage);
      }),
      this.sourceBuilder.events.cancel.subscribe(() => {
        this.pickMode = false;
        this.changeRef.detectChanges();
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  zoomOut() {
    this.scaleFactor = Math.min(this.scaleFactor + 0.05, 1.3);
  }

  zoomIn() {
    this.scaleFactor = Math.max(this.scaleFactor - 0.05, 0.5);
  }

  private async scrape() {
    console.log('scrape');
    if (this.loading) {
      return;
    }

    this.loading = true;
    this.loadingChange.emit(this.loading);
    this.changeRef.detectChanges();

    try {
      const scrapeResponse = await this.sourceBuilder.fetchFeedsUsingBrowser();

      this.handleScrapeResponse(scrapeResponse);
    } catch (e) {
      console.error(e);
      this.errorMessage = e.message;
    }
    this.loading = false;
    this.loadingChange.emit(this.loading);
    this.changeRef.detectChanges();
  }

  cancelPickMode() {
    this.sourceBuilder.events.cancel.emit();
  }

  private handleScrapeResponse(scrapeResponse: ScrapeResponse) {
    const url = this.sourceBuilder.getUrl();

    this.embedScreenshot = null;
    this.embedMarkup = null;
    this.changeRef.detectChanges();

    this.logs = scrapeResponse.logs;
    if (!scrapeResponse.ok) {
      this.errorMessage = scrapeResponse.errorMessage;
    } else {
      const fetchAction = scrapeResponse.outputs.find((o) => o.response.fetch)
        .response.fetch;
      const extractAction = scrapeResponse.outputs.find(
        (o) => o.response.extract?.fragments,
      )?.response?.extract;

      this.formFg.controls.prerenderingOptions.patchValue(
        {
          landscape: fetchAction.debug.viewport.isLandscape,
          mobile: fetchAction.debug.viewport.isMobile,
          resolutionY: fetchAction.debug.viewport.height,
          resolutionX: fetchAction.debug.viewport.width,
        },
        { emitEvent: false },
      );

      if (extractAction) {
        this.hasScreenshot = true;
        this.embedScreenshot = {
          mimeType: extractAction.fragments[0].data.mimeType,
          data: extractAction.fragments[0].data.data,
          url,
          viewport: fetchAction.debug.viewport,
        };
        this.hasMarkup = true;
        this.embedMarkup = {
          mimeType: 'text/html',
          data: extractAction.fragments[0].html.data,
          url,
          viewport: fetchAction.debug.viewport,
        };
      } else {
        const extractAction = first(
          last(
            scrapeResponse.outputs.filter((o) => o.response.extract?.fragments),
          )?.response?.extract?.fragments,
        )?.html;

        this.viewModeFc.patchValue('markup');
        this.hasMarkup = true;
        if (extractAction) {
          this.embedMarkup = {
            mimeType: 'text/html',
            data: extractAction.data,
            url,
          };
        } else {
          this.embedMarkup = {
            data: fetchAction.data,
            mimeType: fetchAction.debug.contentType,
            url,
            // viewport: null,
          };
        }
      }
    }

    this.changeRef.detectChanges();
  }

  selectTab(tab: string) {
    if (tab !== this.viewModeFc.value) {
      this.viewModeFc.patchValue(tab);
    }
  }
}
