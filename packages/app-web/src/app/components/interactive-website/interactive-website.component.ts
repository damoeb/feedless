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
import { parseInt } from 'lodash-es';
import { GqlScrapeEmit } from '../../../generated/graphql';
import { ScrapeService } from '../../services/scrape.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ScrapeController } from './scrape-controller';
import { debounce, interval, Subscription } from 'rxjs';
import { ServerConfigService } from '../../services/server-config.service';
import { Embeddable } from '../embedded-image/embedded-image.component';

type ViewMode = 'markup' | 'image';

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

  viewModeFc = new FormControl<ViewMode>('image');
  viewModeImage: ViewMode = 'image';
  viewModeMarkup: ViewMode = 'markup';
  pickMode = false;

  async ngOnInit() {
    this.formFg.patchValue({
      url: this.scrapeController.getUrl(),
    });
    if (!this.scrapeController.response) {
      this.scrape();
    }

    this.subscriptions.push(
      this.formFg.valueChanges
        .pipe(debounce(() => interval(800)))
        .subscribe(() => {
          this.scrape();
        }),
      this.scrapeController.pickPoint.subscribe(() => {
        this.viewModeFc.patchValue(this.viewModeImage);
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
      const url = this.scrapeController.getUrl();
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

      this.embedScreenshot = null;
      this.embedMarkup = null;
      this.changeRef.detectChanges();

      const fetchAction = scrapeResponse.outputs.find((o) => o.response.fetch)
        .response.fetch;
      const extractAction = scrapeResponse.outputs.find(
        (o) => o.response.extract,
      ).response.extract;

      this.formFg.controls.prerenderingOptions.patchValue(
        {
          landscape: fetchAction.debug.viewport.isLandscape,
          mobile: fetchAction.debug.viewport.isMobile,
          resolutionY: fetchAction.debug.viewport.height,
          resolutionX: fetchAction.debug.viewport.width,
        },
        { emitEvent: false },
      );

      this.embedScreenshot = {
        mimeType: extractAction.fragments[0].data.mimeType,
        data: extractAction.fragments[0].data.data,
        url,
        viewport: fetchAction.debug.viewport,
      };
      this.embedMarkup = {
        mimeType: 'text/html',
        data: extractAction.fragments[0].html.data,
        url,
        viewport: fetchAction.debug.viewport,
      };
      this.scrapeController.response = scrapeResponse;
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
}
