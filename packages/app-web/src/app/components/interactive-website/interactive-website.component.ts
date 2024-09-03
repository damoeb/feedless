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
import { GqlScrapeEmit } from '../../../generated/graphql';
import { ScrapeService } from '../../services/scrape.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ScrapeController } from './scrape-controller';
import { debounce, interval, map, merge, Subscription } from 'rxjs';
import { ServerConfigService } from '../../services/server-config.service';
import { Embeddable } from '../embedded-image/embedded-image.component';
import { ScrapeResponse } from '../../graphql/types';

type ViewMode = 'markup' | 'image' | 'logs';

@Component({
  selector: 'app-interactive-website',
  templateUrl: './interactive-website.component.html',
  styleUrls: ['./interactive-website.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InteractiveWebsiteComponent implements OnInit, OnDestroy {
  @Input({ required: true })
  scrapeController: ScrapeController;

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
  protected logs: string;
  protected hasScreenshot: boolean;
  protected hasMarkup: boolean;

  constructor(
    private readonly scrapeService: ScrapeService,
    protected readonly serverConfig: ServerConfigService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  zoomOut() {
    this.scaleFactor = Math.min(this.scaleFactor + 0.05, 1.3);
  }

  zoomIn() {
    this.scaleFactor = Math.max(this.scaleFactor - 0.05, 0.5);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  protected readonly parseInt = parseInt;

  viewModeFc = new FormControl<ViewMode | string>('image');
  viewModeImage: ViewMode = 'image';
  viewModeMarkup: ViewMode = 'markup';
  viewLogs: ViewMode = 'logs';
  pickMode = false;

  async ngOnInit() {
    this.formFg.patchValue({
      url: this.scrapeController.getUrl(),
    });
    if (this.scrapeController.response) {
      this.handleScrapeResponse(this.scrapeController.response);
    } else {
      this.scrape();
    }

    this.subscriptions.push(
      merge(
        this.formFg.valueChanges,
        this.formFg.controls.prerenderingOptions.controls.additionalWait.valueChanges.pipe(
          map((wait) =>
            this.scrapeController.patchFetch({ additionalWaitSec: wait }),
          ),
        ),
      )
        .pipe(debounce(() => interval(800)))
        .subscribe(() => {
          this.scrape();
        }),
      this.viewModeFc.valueChanges.subscribe((value) =>
        this.segmentChange.emit(value),
      ),
      this.scrapeController.pickPoint.subscribe(() => {
        this.viewModeFc.patchValue(this.viewModeImage);
      }),
      this.scrapeController.showElements.subscribe(() => {
        this.viewModeFc.patchValue(this.viewModeMarkup);
      }),
      this.scrapeController.pickElement.subscribe(() => {
        this.viewModeFc.patchValue(this.viewModeMarkup);
      }),
      this.scrapeController.pickArea.subscribe(() => {
        this.viewModeFc.patchValue(this.viewModeImage);
      }),
      this.scrapeController.actionsChanges.subscribe(() => {
        this.scrape();
      }),
      this.scrapeController.cancel.subscribe(() => {
        this.pickMode = false;
        this.changeRef.detectChanges();
      }),
    );
  }

  private async scrape() {
    console.log('scrape');
    this.loading = true;
    this.loadingChange.emit(this.loading);
    this.changeRef.detectChanges();

    try {
      const scrapeResponse = await this.scrapeService.scrape(
        this.scrapeController.getScrapeRequest([
          {
            extract: {
              fragmentName: 'full-page',
              selectorBased: {
                fragmentName: '',
                xpath: {
                  value: '/',
                },
                emit: [
                  GqlScrapeEmit.Html,
                  GqlScrapeEmit.Text,
                  GqlScrapeEmit.Pixel,
                ],
              },
            },
          },
        ]),
      );

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
    this.scrapeController.cancel.emit();
  }

  private handleScrapeResponse(scrapeResponse: ScrapeResponse) {
    const url = this.scrapeController.getUrl();

    this.embedScreenshot = null;
    this.embedMarkup = null;
    this.changeRef.detectChanges();

    this.logs = scrapeResponse.logs
      .map(
        (log) => `${new Date(log.time).toLocaleTimeString()}\t ${log.message}`,
      )
      .join('\n');

    if (scrapeResponse.failed) {
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

    this.scrapeController.response = scrapeResponse;
    this.changeRef.detectChanges();
  }

  selectTab(tab: string) {
    this.viewModeFc.patchValue(tab);
  }
}
