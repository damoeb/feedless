import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  input,
  OnDestroy,
  OnInit,
  output,
  PLATFORM_ID,
} from '@angular/core';
import { first, last, parseInt } from 'lodash-es';
import { GqlLogStatement, ScrapeResponse } from '@feedless/graphql-api';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { SourceBuilder } from '@feedless/source';
import { debounce, interval, map, merge, Subscription } from 'rxjs';
import { ServerConfigService } from '@feedless/services';
import { AnnotateImageComponent } from '../annotate-image/annotate-image.component';
import { addIcons } from 'ionicons';
import { addOutline, removeOutline } from 'ionicons/icons';
import {
  IonButton,
  IonButtons,
  IonCol,
  IonInput,
  IonItem,
  IonLabel,
  IonProgressBar,
  IonRange,
  IonRow,
  IonSegment,
  IonSegmentButton,
  IonText,
  IonToolbar,
} from '@ionic/angular/standalone';
import { isPlatformBrowser, NgStyle } from '@angular/common';
import { EmbeddedMarkupComponent } from '../embedded-markup/embedded-markup.component';
import { ConsoleButtonComponent } from '../console-button/console-button.component';
import { BlockElementComponent } from '../block-element/block-element.component';
import { Embeddable } from '@feedless/core';
import { IconComponent } from '../icon/icon.component';

type ViewMode = 'markup' | 'image';

@Component({
  selector: 'app-interactive-website',
  templateUrl: './interactive-website.component.html',
  styleUrls: ['./interactive-website.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonToolbar,
    IonRow,
    IonCol,
    IonInput,
    FormsModule,
    ReactiveFormsModule,
    IonSegment,
    IonSegmentButton,
    IonButtons,
    IonButton,
    IconComponent,
    IonRange,
    IonLabel,
    IonText,
    NgStyle,
    AnnotateImageComponent,
    EmbeddedMarkupComponent,
    ConsoleButtonComponent,
    IonItem,
    IonProgressBar,
    BlockElementComponent,
  ],
  standalone: true,
})
export class InteractiveWebsiteComponent implements OnInit, OnDestroy {
  protected readonly serverConfig = inject(ServerConfigService);
  private readonly changeRef = inject(ChangeDetectorRef);

  readonly sourceBuilder = input.required<SourceBuilder>();

  readonly showUrl = input<boolean>(false);

  readonly loadingChange = output<boolean>();

  readonly segmentChange = output<string>();

  scaleFactor = 0.7;
  minScaleFactor = 0.5;
  maxScaleFactor = 1.3;
  loading = false;
  shouldScrape = false;

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
  private readonly platformId = inject(PLATFORM_ID);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ removeOutline, addOutline });
    }
  }

  viewModeFc = new FormControl<ViewMode | string>('markup');
  viewModeImage: ViewMode = 'image';
  viewModeMarkup: ViewMode = 'markup';
  pickMode = false;

  async ngOnInit() {
    this.formFg.patchValue({
      url: this.sourceBuilder().getUrl(),
    });
    this.segmentChange.emit(this.viewModeFc.value);

    if (this.sourceBuilder().response) {
      this.handleScrapeResponse(this.sourceBuilder().response);
    } else {
      this.scrape();
    }

    this.subscriptions.push(
      merge(
        this.formFg.valueChanges,
        this.formFg.controls.prerenderingOptions.controls.additionalWait.valueChanges.pipe(
          map((wait) =>
            this.sourceBuilder().patchFetch({ additionalWaitSec: wait }),
          ),
        ),
        merge(
          this.formFg.controls.prerenderingOptions.controls.resolutionX
            .valueChanges,
          this.formFg.controls.prerenderingOptions.controls.resolutionY
            .valueChanges,
          this.formFg.controls.prerenderingOptions.controls.mobile.valueChanges,
          this.formFg.controls.prerenderingOptions.controls.landscape
            .valueChanges,
        )
          .pipe(debounce(() => interval(100)))
          .pipe(
            map(() =>
              this.sourceBuilder().patchFetch({
                viewport: {
                  height: this.formFg.value.prerenderingOptions.resolutionY,
                  width: this.formFg.value.prerenderingOptions.resolutionX,
                  isLandscape: this.formFg.value.prerenderingOptions.landscape,
                  isMobile: this.formFg.value.prerenderingOptions.mobile,
                },
              }),
            ),
          ),
      )
        .pipe(debounce(() => interval(100)))
        .subscribe(() => {
          this.sourceBuilder().events.actionsChanges.emit();
        }),
      this.viewModeFc.valueChanges.subscribe((value) =>
        this.segmentChange.emit(value),
      ),
      this.sourceBuilder().events.pickPoint.subscribe(() => {
        this.viewModeFc.patchValue(this.viewModeImage);
      }),
      // this.sourceBuilder().events.showElements.subscribe(() => {
      //   this.viewModeFc.patchValue(this.viewModeMarkup);
      // }),
      this.sourceBuilder().events.actionsChanges.subscribe(() => {
        this.shouldScrape = true;
        this.changeRef.detectChanges();
      }),
      this.sourceBuilder().events.pickElement.subscribe(() => {
        this.viewModeFc.patchValue(this.viewModeMarkup);
      }),
      this.sourceBuilder().events.pickArea.subscribe(() => {
        this.viewModeFc.patchValue(this.viewModeImage);
      }),
      this.sourceBuilder().events.cancel.subscribe(() => {
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

  protected async scrape() {
    this.shouldScrape = false;
    if (this.loading) {
      return;
    }

    this.loading = true;
    this.loadingChange.emit(this.loading);
    this.changeRef.detectChanges();

    try {
      const scrapeResponse =
        await this.sourceBuilder().fetchFeedsUsingBrowser();

      this.handleScrapeResponse(scrapeResponse);
    } catch (e: any) {
      console.error(e);
      this.errorMessage = e?.message;
    }
    this.loading = false;
    this.loadingChange.emit(this.loading);
    this.changeRef.detectChanges();
  }

  cancelPickMode() {
    this.sourceBuilder().events.cancel.emit();
  }

  private handleScrapeResponse(scrapeResponse: ScrapeResponse) {
    const url = this.sourceBuilder().getUrl();

    this.embedScreenshot = null;
    this.embedMarkup = null;
    this.changeRef.detectChanges();

    if (!scrapeResponse) {
      this.errorMessage = 'No response received from scraper';
      return;
    }

    this.logs = scrapeResponse.logs || [];
    if (!scrapeResponse.ok) {
      this.errorMessage = scrapeResponse.errorMessage;
    } else {
      const fetchActionOutput = scrapeResponse.outputs?.find(
        (o) => o.response?.fetch,
      );
      const fetchAction = fetchActionOutput?.response?.fetch;
      const extractAction = scrapeResponse.outputs?.find(
        (o) => o.response?.extract?.fragments,
      )?.response?.extract;

      if (!fetchAction) {
        this.errorMessage = 'No fetch action found in response';
        return;
      }

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
