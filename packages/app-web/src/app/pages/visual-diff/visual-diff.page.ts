import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ProfileService } from '../../services/profile.service';
import { Subscription } from 'rxjs';
import { Embeddable } from '../../components/embedded-website/embedded-website.component';
import {
  GqlPuppeteerWaitUntil,
  GqlScrapeActionInput,
} from '../../../generated/graphql';
import { ScrapeService } from '../../services/scrape.service';
import { fixUrl } from '../getting-started/getting-started.page';
import { FormControl, FormGroup, Validators } from '@angular/forms';

type View = 'screenshot' | 'markup';

interface ScreenResolution {
  name: string;
  width: number;
  height: number;
}

type CompareBy = 'pixel' | 'text' | 'markup';

@Component({
  selector: 'app-visual-diff',
  templateUrl: './visual-diff.page.html',
  styleUrls: ['./visual-diff.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VisualDiffPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];

  isDarkMode: boolean;
  embedMarkup: Embeddable;
  embedScreenshot: Embeddable;

  resolutions: ScreenResolution[] = [
    {
      name: 'XGA',
      width: 1024,
      height: 768,
    },
    {
      name: 'HD720',
      width: 1280,
      height: 720,
    },
    {
      name: 'WXGA',
      width: 1280,
      height: 800,
    },
    {
      name: 'SXGA',
      width: 1280,
      height: 1024,
    },
  ];

  resolution = this.resolutions[0];

  url: string;
  loading = false;
  actions: GqlScrapeActionInput[] = [];
  view: View = 'screenshot';
  pickElementDelegate: (xpath: string) => void;

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
  totalTime: string;

  constructor(
    readonly profile: ProfileService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly scrapeService: ScrapeService,
  ) {}

  ngOnInit() {
    this.url = 'https://twitter.com/damoeb';
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

  async scrapeUrl() {
    this.url = fixUrl(this.url);
    this.changeRef.detectChanges();

    if (this.loading) {
      return;
    }

    this.loading = true;
    this.changeRef.detectChanges();

    const response = await this.scrapeService.scrape({
      page: {
        url: this.url,
        actions: this.actions,
        timeout: 60000,
        prerender: {
          waitUntil: GqlPuppeteerWaitUntil.Load,
          viewport: {
            isMobile: false,
            height: this.resolution.height,
            width: this.resolution.width,
          },
        },
      },
      debug: {
        screenshot: true,
        console: true,
        cookies: true,
        html: true,
      },
      waitFor: 1000,
      emit: [],
      elements: [],
    });

    if (response.failed) {
      console.error(response.errorMessage);
    } else {
      this.totalTime =
        (
          (response.debug.metrics.queue + response.debug.metrics.render) /
          1000
        ).toFixed(2) + 's';
      this.embedMarkup = {
        mimeType: response.debug.contentType,
        data: response.debug.html,
        url: this.url,
        viewport: response.debug.viewport,
      };
      this.embedScreenshot = {
        mimeType: 'image/png',
        data: response.debug.screenshot,
        url: this.url,
        viewport: response.debug.viewport,
      };
    }

    this.loading = false;
    this.changeRef.detectChanges();
  }

  handlePickedXpath(xpath: string) {
    if (this.pickElementDelegate) {
      this.pickElementDelegate(xpath);
      this.pickElementDelegate = null;
    }
  }

  async handleActionChanged(actions: GqlScrapeActionInput[]) {
    this.actions = actions;
    console.log('actions', JSON.stringify(actions));
    // await this.scrapeUrl();
  }

  registerPickElementDelegate(callback: (xpath: string) => void) {
    this.view = 'markup';
    this.pickElementDelegate = callback;
  }

  pickElement(fc: FormControl<string | null>) {
    this.registerPickElementDelegate((xpath) => fc.setValue(xpath));
  }
}
