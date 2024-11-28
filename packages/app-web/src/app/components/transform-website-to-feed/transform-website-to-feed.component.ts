import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit, ViewChild, inject, output, input } from '@angular/core';
import {
  GqlExtendContentOptions,
  GqlFeedlessPlugins,
  GqlLogStatement,
  GqlRemoteNativeFeed,
  GqlTransientGenericFeed,
} from '../../../generated/graphql';
import { Record, Selectors } from '../../graphql/types';
import { scaleLinear, ScaleLinear } from 'd3-scale';
import { assign, last, max, min, omit } from 'lodash-es';
import {
  FormControl,
  FormGroup,
  Validators,
  FormsModule,
  ReactiveFormsModule,
} from '@angular/forms';
import { format } from 'prettier/standalone';
import htmlPlugin from 'prettier/plugins/html';
import { ModalService } from '../../services/modal.service';
import { NativeOrGenericFeed } from '../feed-builder/feed-builder.component';
import { debounce as rxDebounce, interval, Subscription } from 'rxjs';
import { SourceBuilder } from '../interactive-website/source-builder';
import { CodeEditorModalComponentProps } from '../../modals/code-editor-modal/code-editor-modal.component';
import { InteractiveWebsiteComponent } from '../interactive-website/interactive-website.component';
import { ActivatedRoute, Router } from '@angular/router';
import { Location, NgStyle } from '@angular/common';
import { ScrapeService } from '../../services/scrape.service';
import { addIcons } from 'ionicons';
import {
  chevronForward,
  searchOutline,
  chevronForwardOutline,
  refreshOutline,
} from 'ionicons/icons';
import { ResponsiveColumnsComponent } from '../responsive-columns/responsive-columns.component';
import {
  IonAccordionGroup,
  IonAccordion,
  IonItem,
  IonLabel,
  IonIcon,
  IonInput,
  IonButton,
  IonNote,
  IonCheckbox,
  IonList,
  IonSegmentButton,
  IonProgressBar,
  IonSpinner,
} from '@ionic/angular/standalone';
import { BubbleComponent } from '../bubble/bubble.component';
import { RemoteFeedPreviewComponent } from '../remote-feed-preview/remote-feed-preview.component';
import { ConsoleButtonComponent } from '../console-button/console-button.component';

export type TypedFormControls<TControl> = {
  [K in keyof TControl]: FormControl<TControl[K]>;
};

export type ComponentStatus = 'valid' | 'invalid';

@Component({
  selector: 'app-transform-website-to-feed',
  templateUrl: './transform-website-to-feed.component.html',
  styleUrls: ['./transform-website-to-feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ResponsiveColumnsComponent,
    IonAccordionGroup,
    IonAccordion,
    IonItem,
    IonLabel,
    IonIcon,
    NgStyle,
    IonInput,
    FormsModule,
    ReactiveFormsModule,
    IonButton,
    IonNote,
    IonCheckbox,
    IonList,
    BubbleComponent,
    InteractiveWebsiteComponent,
    IonSegmentButton,
    IonProgressBar,
    IonSpinner,
    RemoteFeedPreviewComponent,
    ConsoleButtonComponent
],
  standalone: true,
})
export class TransformWebsiteToFeedComponent implements OnInit, OnDestroy {
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly scrapeService = inject(ScrapeService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly location = inject(Location);
  private readonly modalService = inject(ModalService);

  protected readonly CUSTOM_HASH = 'custom-hash';

  @Input({ required: true })
  sourceBuilder: SourceBuilder;

  @ViewChild('interactiveWebsite')
  interactiveWebsiteComponent: InteractiveWebsiteComponent;

  readonly feed = input<NativeOrGenericFeed>();

  readonly statusChange = output<ComponentStatus>();

  readonly selectedFeedChange = output<NativeOrGenericFeed>();

  readonly genFeedXpathsFg: FormGroup<TypedFormControls<Selectors>> =
    new FormGroup<TypedFormControls<Selectors>>(
      {
        contextXPath: new FormControl<string>('', {
          nonNullable: true,
          validators: [Validators.required, Validators.minLength(1)],
        }),
        dateXPath: new FormControl<string>('', []),
        paginationXPath: new FormControl<string>('', []),
        linkXPath: new FormControl<string>('', {
          nonNullable: true,
        }),
        dateIsStartOfEvent: new FormControl<boolean>(false, {
          nonNullable: true,
          validators: [Validators.required],
        }),
        extendContext: new FormControl<GqlExtendContentOptions>(
          GqlExtendContentOptions.None,
          [],
        ),
      },
      { updateOn: 'change' },
    );

  genericFeeds: GqlTransientGenericFeed[] = [];
  nativeFeeds: GqlRemoteNativeFeed[] = [];
  currentNativeFeed: GqlRemoteNativeFeed;
  currentGenericFeed: GqlTransientGenericFeed;
  busy = false;
  protected selectedFeed: NativeOrGenericFeed;
  private scaleScore: ScaleLinear<number, number, never>;
  private subscriptions: Subscription[] = [];
  feedItems: Record[];
  feedLogs: Array<Pick<GqlLogStatement, 'time' | 'message'>>;
  // protected useCustomSelectors: boolean;
  protected shouldRefresh: boolean = false;
  activeSegment: string;
  private customSelectorsFgChangeSubscription: Subscription;
  protected loadingFeedPreview: boolean;

  constructor() {
    addIcons({
      chevronForward,
      searchOutline,
      chevronForwardOutline,
      refreshOutline,
    });
  }

  async ngOnInit() {
    try {
      const genericFeedParams = this.sourceBuilder.findFirstByPluginsId(
        GqlFeedlessPlugins.OrgFeedlessFeed,
      )?.execute?.params?.org_feedless_feed?.generic;

      if (genericFeedParams) {
        this.customizeGenericFeed({
          selectors: genericFeedParams,
          count: 0,
          score: 0,
          hash: '',
        });
      } else {
        this.genFeedXpathsFg.patchValue(
          this.activatedRoute.snapshot.queryParams,
        );
        if (this.genFeedXpathsFg.valid) {
          this.customizeGenericFeed({
            hash: '',
            score: 0,
            count: 0,
            selectors: {
              linkXPath: this.genFeedXpathsFg.value.linkXPath,
              contextXPath: this.genFeedXpathsFg.value.contextXPath,
              paginationXPath: this.genFeedXpathsFg.value.paginationXPath,
              dateIsStartOfEvent: this.genFeedXpathsFg.value.dateIsStartOfEvent,
              extendContext: this.genFeedXpathsFg.value.extendContext,
              dateXPath: this.genFeedXpathsFg.value.dateXPath,
            },
          });
        }
      }
      this.changeRef.detectChanges();

      this.subscriptions.push(
        this.sourceBuilder.events.stateChange
          .pipe(rxDebounce(() => interval(200)))
          .subscribe((state) => {
            this.shouldRefresh = state === 'DIRTY';
            this.changeRef.detectChanges();
          }),
        this.genFeedXpathsFg.valueChanges
          .pipe(rxDebounce(() => interval(200)))
          .subscribe(() => {
            this.patchCurrentUrl(this.genFeedXpathsFg.value);
            this.emitSelectedFeed();
          }),
        this.genFeedXpathsFg.controls.contextXPath.valueChanges
          .pipe(rxDebounce(() => interval(500)))
          .subscribe((xpath) => {
            if (this.activeSegment !== 'feed') {
              this.sourceBuilder.events.showElements.next(`${xpath}`);
            }
          }),
      );
      const elementWithFeeds = this.sourceBuilder.response.outputs.find(
        (o) => o.response?.extract?.feeds,
      );
      if (elementWithFeeds) {
        const feeds = elementWithFeeds.response.extract.feeds;
        this.genericFeeds = feeds.genericFeeds;
        this.nativeFeeds = feeds.nativeFeeds as GqlRemoteNativeFeed[]; // todo
        const scores = feeds.genericFeeds.map((gf) => gf.score);
        const maxScore = max(scores);
        const minScore = min(scores);
        this.scaleScore = scaleLinear()
          .domain([minScore, maxScore])
          .range([0, 100]);
      } else {
        throw new Error('not supported');
      }
      const feed = this.feed();
      if (feed) {
        if (feed.nativeFeed) {
          await this.pickNativeFeed(feed.nativeFeed);
        } else if (feed.genericFeed) {
          await this.pickGenericFeed(feed.genericFeed);
        } else {
          throw new Error('not supported');
        }
      }
      this.statusChange.emit(this.isValid() ? 'valid' : 'invalid');
    } catch (e) {
      console.error(e);
    }
  }

  async pickNativeFeed(feed: GqlRemoteNativeFeed) {
    this.resetSelection();
    console.log('pickNativeFeed', feed);
    if (this.currentNativeFeed !== feed) {
      this.currentNativeFeed = feed;
      // await assignNativeFeedToContext(feed, this.handler);
      this.selectedFeed = {
        nativeFeed: this.currentNativeFeed,
      };
      this.sourceBuilder
        .patchFetch({
          url: {
            literal: feed.feedUrl,
          },
        })
        .addOrUpdatePluginById(GqlFeedlessPlugins.OrgFeedlessFeed, {
          execute: {
            pluginId: GqlFeedlessPlugins.OrgFeedlessFeed,
            params: {},
          },
        });

      this.emitSelectedFeed();
      this.patchCurrentUrl({ url: this.currentNativeFeed.feedUrl });
    }
    this.changeRef.detectChanges();
  }

  async pickGenericFeed(genericFeed: GqlTransientGenericFeed) {
    this.resetSelection();
    this.customizeGenericFeed(genericFeed);
  }

  private patchCurrentUrl(queryParams: object) {
    const url = this.router
      .createUrlTree(this.activatedRoute.snapshot.url, {
        queryParams: {
          url: this.sourceBuilder.getUrl(),
          ...queryParams,
        },
        relativeTo: this.activatedRoute,
      })
      .toString();
    this.location.replaceState(url);
  }

  getRelativeScore(genericFeed: GqlTransientGenericFeed): number {
    return this.scaleScore ? this.scaleScore(genericFeed.score) : 0;
  }

  private resetSelection() {
    this.currentGenericFeed = null;
    this.currentNativeFeed = null;
  }

  private isValid(): boolean {
    if (this.selectedFeed) {
      if (this.selectedFeed.nativeFeed) {
        return true;
      } else {
        return this.genFeedXpathsFg.valid;
      }
    }
    return false;
  }

  private async emitSelectedFeed() {
    console.log('emitSelectedFeed');
    this.statusChange.emit(this.isValid() ? 'valid' : 'invalid');
    this.selectedFeedChange.emit(this.selectedFeed);
    if (this.isValid()) {
      this.sourceBuilder.events.stateChange.next('DIRTY');
    }
    if (!this.feedItems) {
      this.fetchFeedPreview(false);
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async pickElementWithin(xpath: string, fc: FormControl<string>) {
    this.sourceBuilder.events.extractElements.next({
      xpath,
      callback: async (elements: HTMLElement[]) => {
        const componentProps: CodeEditorModalComponentProps = {
          title: 'HTML Editor',
          text: await format(elements[0].innerHTML, {
            parser: 'html',
            plugins: [htmlPlugin],
          }),
          contentType: 'html',
        };
        await this.modalService.openCodeEditorModal(componentProps);
      },
    });
  }

  protected async fetchFeedPreview(switchTab: boolean) {
    console.log('fetchFeedPreview');
    if (this.selectedFeed) {
      this.genFeedXpathsFg.markAsPristine();
      if (switchTab) {
        this.interactiveWebsiteComponent?.selectTab('feed');
      }

      try {
        this.loadingFeedPreview = true;
        const response = await this.scrapeService.scrape(
          this.sourceBuilder.build(),
        );
        this.feedItems = last(response.outputs).response.extract.items;
        this.feedLogs = response.logs;

        this.sourceBuilder.events.stateChange.next('PRISTINE');
      } finally {
        this.loadingFeedPreview = false;
      }
    }
    this.changeRef.detectChanges();
  }

  customizeGenericFeed(genericFeed: GqlTransientGenericFeed) {
    this.genFeedXpathsFg.patchValue(
      {
        contextXPath: genericFeed.selectors.contextXPath,
        linkXPath: genericFeed.selectors.linkXPath,
        paginationXPath: genericFeed.selectors.paginationXPath,
        dateIsStartOfEvent: genericFeed.selectors.dateIsStartOfEvent,
        dateXPath: genericFeed.selectors.dateXPath,
        extendContext: genericFeed.selectors.extendContext,
      },
      { emitEvent: false },
    );
    this.genFeedXpathsFg.markAsPristine();

    this.sourceBuilder.events.showElements.next(
      genericFeed.selectors.contextXPath,
    );

    if (this.customSelectorsFgChangeSubscription) {
      this.customSelectorsFgChangeSubscription.unsubscribe();
    }

    this.propagateCurrentGenFeed(genericFeed.hash);

    this.customSelectorsFgChangeSubscription = this.genFeedXpathsFg.valueChanges
      .pipe(rxDebounce(() => interval(200)))
      .subscribe(() => {
        this.propagateCurrentGenFeed();
      });

    this.changeRef.detectChanges();
  }

  private propagateCurrentGenFeed(hash: string | undefined = undefined) {
    const value = this.genFeedXpathsFg.value;
    this.currentGenericFeed = assign(
      {},
      {
        selectors: {
          linkXPath: value.linkXPath,
          contextXPath: value.contextXPath,
          dateIsStartOfEvent: value.dateIsStartOfEvent,
          extendContext: value.extendContext,
          dateXPath: value.dateXPath,
          paginationXPath: value.paginationXPath,
        },
        hash: '',
        score: 0,
        count: 0,
      },
      {
        hash: hash || this.CUSTOM_HASH,
      },
    );
    this.selectedFeed = {
      genericFeed: this.currentGenericFeed,
    };

    this.sourceBuilder.addOrUpdatePluginById(
      GqlFeedlessPlugins.OrgFeedlessFeed,
      {
        execute: {
          pluginId: GqlFeedlessPlugins.OrgFeedlessFeed,
          params: {
            org_feedless_feed: {
              generic: this.currentGenericFeed.selectors,
            },
          },
        },
      },
    );

    this.patchCurrentUrl(omit(this.currentGenericFeed.selectors, '__typename'));
    this.changeRef.detectChanges();
    this.emitSelectedFeed();
  }

  handleSegmentChange(segment: string) {
    if (this.activeSegment != segment) {
      this.activeSegment = segment;

      if (segment === 'feed' && this.shouldRefresh) {
        this.fetchFeedPreview(false);
      }
    }
  }
}
