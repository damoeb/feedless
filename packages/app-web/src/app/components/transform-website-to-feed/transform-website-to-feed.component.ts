import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges, ViewChild
} from '@angular/core';
import {
  GqlExtendContentOptions,
  GqlRemoteNativeFeed,
  GqlScrapeRequest,
  GqlSourceInput,
  GqlTransientGenericFeed,
} from '../../../generated/graphql';
import { RemoteFeed, ScrapeResponse, Selectors } from '../../graphql/types';
import { scaleLinear, ScaleLinear } from 'd3-scale';
import { cloneDeep, max, min, omit } from 'lodash-es';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { format } from 'prettier/standalone';
import htmlPlugin from 'prettier/plugins/html';
import { ModalService } from '../../services/modal.service';
import { FeedService } from '../../services/feed.service';
import { getScrapeRequest } from '../../modals/generate-feed-modal/generate-feed-modal.component';
import { NativeOrGenericFeed } from '../feed-builder/feed-builder.component';
import { Subscription } from 'rxjs';
import { getGenericFeedParams } from '../../utils';
import { ScrapeController } from '../interactive-website/scrape-controller';
import { CodeEditorModalComponentProps } from '../../modals/code-editor-modal/code-editor-modal.component';
import { InteractiveWebsiteComponent } from '../interactive-website/interactive-website.component';

export type TypedFormControls<TControl> = {
  [K in keyof TControl]: FormControl<TControl[K]>;
};

export type ComponentStatus = 'valid' | 'invalid';

@Component({
  selector: 'app-transform-website-to-feed',
  templateUrl: './transform-website-to-feed.component.html',
  styleUrls: ['./transform-website-to-feed.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TransformWebsiteToFeedComponent
  implements OnInit, OnChanges, OnDestroy
{
  @Input({ required: true })
  scrapeRequest: GqlSourceInput;

  @Input({ required: true })
  scrapeResponse: ScrapeResponse;

  @ViewChild('interactiveWebsite')
  interactiveWebsiteComponent: InteractiveWebsiteComponent;

  @Input()
  feed: NativeOrGenericFeed;

  @Output()
  statusChange: EventEmitter<ComponentStatus> =
    new EventEmitter<ComponentStatus>();

  @Output()
  selectedFeedChange: EventEmitter<NativeOrGenericFeed> =
    new EventEmitter<NativeOrGenericFeed>();

  formGroup: FormGroup<TypedFormControls<Selectors>> = new FormGroup<
    TypedFormControls<Selectors>
  >(
    {
      contextXPath: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(1)],
      }),
      dateXPath: new FormControl('', []),
      paginationXPath: new FormControl('', []),
      linkXPath: new FormControl('', {
        nonNullable: true,
      }),
      dateIsStartOfEvent: new FormControl(false, {
        nonNullable: true,
        validators: [Validators.required],
      }),
      extendContext: new FormControl(GqlExtendContentOptions.None, []),
    },
    { updateOn: 'change' },
  );

  genericFeeds: GqlTransientGenericFeed[] = [];
  nativeFeeds: GqlRemoteNativeFeed[] = [];
  currentNativeFeed: GqlRemoteNativeFeed;
  currentGenericFeed: GqlTransientGenericFeed;
  isNonSelected = true;
  busy = false;
  showSelectors = false;
  protected selectedFeed: NativeOrGenericFeed;
  private scaleScore: ScaleLinear<number, number, never>;
  private subscriptions: Subscription[] = [];
  protected scrapeController: ScrapeController;
  remoteFeed: RemoteFeed;

  constructor(
    private readonly changeRef: ChangeDetectorRef,
    private readonly feedService: FeedService,
    private readonly modalService: ModalService,
  ) {}

  async ngOnInit() {
    try {
      this.scrapeController = new ScrapeController(this.scrapeRequest);
      this.scrapeController.response = this.scrapeResponse;

      const genericFeedParams = getGenericFeedParams(
        this.scrapeRequest.flow?.sequence,
      );
      if (genericFeedParams) {
        await this.pickGenericFeed({
          selectors: genericFeedParams,
          count: 0,
          score: 0,
          hash: '',
        });
      }

      this.subscriptions.push(
        this.formGroup.valueChanges.subscribe(() => {
          this.emitSelectedFeed();
        }),
        this.formGroup.controls.contextXPath.valueChanges.subscribe((xpath) => {
          this.scrapeController.showElements.next(xpath);
        }),
      );
      const elementWithFeeds = this.scrapeResponse.outputs.find(
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
      if (this.feed) {
        if (this.feed.nativeFeed) {
          await this.pickNativeFeed(this.feed.nativeFeed);
        } else if (this.feed.genericFeed) {
          await this.pickGenericFeed(this.feed.genericFeed);
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
    await this.resetSelection();
    if (this.currentNativeFeed !== feed) {
      this.currentNativeFeed = feed;
      // await assignNativeFeedToContext(feed, this.handler);
      this.selectedFeed = {
        nativeFeed: this.currentNativeFeed,
      };
      this.emitSelectedFeed();
    }
    this.isNonSelected = !this.currentGenericFeed && !this.currentNativeFeed;
    this.changeRef.detectChanges();
  }

  async pickGenericFeed(genericFeed: GqlTransientGenericFeed) {
    await this.resetSelection();
    this.showSelectors = true;
    if (this.currentGenericFeed?.hash !== genericFeed.hash) {
      this.currentGenericFeed = cloneDeep(genericFeed);
      this.selectedFeed = {
        genericFeed: omit(this.currentGenericFeed, 'samples') as any,
      };
      this.scrapeController.showElements.next(
        this.selectedFeed.genericFeed.selectors.contextXPath,
      );
    }
    this.isNonSelected = !this.currentGenericFeed && !this.currentNativeFeed;

    const selectors = genericFeed.selectors;
    this.formGroup.setValue({
      contextXPath: selectors.contextXPath,
      dateIsStartOfEvent: selectors.dateIsStartOfEvent,
      dateXPath: selectors.dateXPath,
      paginationXPath: selectors.paginationXPath || '',
      linkXPath: selectors.linkXPath,
      extendContext: selectors.extendContext,
    });
    this.emitSelectedFeed();

    this.changeRef.detectChanges();
  }

  getRelativeScore(genericFeed: GqlTransientGenericFeed): number {
    return this.scaleScore ? this.scaleScore(genericFeed.score) : 0;
  }

  private async resetSelection() {
    this.showSelectors = false;
    this.currentGenericFeed = null;
    this.currentNativeFeed = null;
  }

  private isValid(): boolean {
    if (this.selectedFeed) {
      if (this.selectedFeed.genericFeed) {
        return this.formGroup.valid;
      }
      return true;
    }
    return false;
  }

  private async emitSelectedFeed() {
    this.statusChange.emit(this.isValid() ? 'valid' : 'invalid');
    this.selectedFeedChange.emit(this.getSelectedFeed());
    this.remoteFeed = await this.feedService.previewFeed({
      sources: [
        getScrapeRequest(
          this.getSelectedFeed(),
          this.scrapeRequest as GqlScrapeRequest,
        ),
      ],
      filters: [],
      tags: [],
    })
  }

  // async previewGenericFeed() {
  //   await this.modalService.openRemoteFeedModal({
  //     feedProvider: () =>
  //       this.feedService.previewFeed({
  //         sources: [
  //           getScrapeRequest(
  //             this.getSelectedFeed(),
  //             this.scrapeRequest as GqlScrapeRequest,
  //           ),
  //         ],
  //         filters: [],
  //         tags: [],
  //       }),
  //   });
  // }
  activeSegment: string;

  async pickGenericFeedBySelectors(
    selectors: Partial<GqlTransientGenericFeed['selectors']>,
  ) {
    console.log('pickGenericFeedBySelectors', selectors, this.genericFeeds);
    const contextXPath = selectors.contextXPath;
    const linkXPath = selectors.linkXPath;
    if (contextXPath && linkXPath) {
      const matchingGenericFeed = this.genericFeeds.find(
        (genericFeed) =>
          genericFeed.selectors.contextXPath === contextXPath &&
          genericFeed.selectors.linkXPath === linkXPath,
      );
      if (matchingGenericFeed) {
        await this.pickGenericFeed(matchingGenericFeed);
      } else {
        console.warn('no matching feed found');
      }
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.scrapeResponse?.currentValue) {
      console.log('changed scrapeResponse');
    }
  }

  private getSelectedFeed(): NativeOrGenericFeed {
    if (this.selectedFeed.nativeFeed) {
      return this.selectedFeed;
    } else {
      return {
        genericFeed: {
          selectors: {
            linkXPath: this.formGroup.value.linkXPath,
            contextXPath: this.formGroup.value.contextXPath,
            dateIsStartOfEvent: this.formGroup.value.dateIsStartOfEvent,
            extendContext: this.formGroup.value.extendContext,
            dateXPath: this.formGroup.value.dateXPath,
            paginationXPath: this.formGroup.value.paginationXPath,
          },
          hash: '',
          score: 0,
          count: 0,
        },
      };
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async pickElementWithin(xpath: string, fc: FormControl<string>) {
    this.scrapeController.extractElements.emit({
      xpath,
      callback: async (elements: HTMLElement[]) => {
        const componentProps: CodeEditorModalComponentProps = {
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

  selectTab(tab: string) {
    this.interactiveWebsiteComponent.selectTab(tab)
  }
}
