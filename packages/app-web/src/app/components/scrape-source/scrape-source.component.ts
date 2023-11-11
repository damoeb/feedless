import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';
import { ProfileService } from '../../services/profile.service';
import { debounce, interval, Subscription } from 'rxjs';
import { Embeddable } from '../embedded-website/embedded-website.component';
import {
  GqlCookieValueInput,
  GqlDomActionSelectInput,
  GqlDomActionTypeInput,
  GqlDomElementByNameInput,
  GqlDomElementByNameOrXPathInput,
  GqlDomElementByXPathInput,
  GqlDomElementInput,
  GqlPuppeteerWaitUntil,
  GqlRequestHeaderInput,
  GqlScrapeAction,
  GqlScrapeActionInput,
  GqlScrapeEmitType,
  GqlScrapePrerenderInput,
  GqlScrapeRequestInput,
  GqlWaitActionInput,
  GqlXyPosition,
  GqlXyPositionInput,
  InputMaybe
} from '../../../generated/graphql';
import { ScrapeService } from '../../services/scrape.service';
import { FormArray, FormControl, FormControlOptions, FormGroup, Validators, ɵValue } from '@angular/forms';
import { fixUrl, isValidUrl } from '../../pages/getting-started/getting-started.page';
import { ScrapeResponse } from '../../graphql/types';
import { KeyLabelOption } from '../select/select.component';
import { BoundingBox, XyPosition } from '../embedded-image/embedded-image.component';
import { isDefined, ResponseMapper, SourceBuilder } from '../../modals/feed-builder-modal/scrape-builder';
import { without } from 'lodash-es';
import { NativeOrGenericFeed } from '../transform-website-to-feed/transform-website-to-feed.component';

type View = 'screenshot' | 'markup';


type ActionType = keyof GqlScrapeAction

export type TypedFormControls<TControl> = {
  [K in keyof TControl]?: TControl[K] extends string|number|boolean ? FormControl<TControl[K]> : FormGroup<TypedFormControls<TControl[K]>>;
};

type ClickType = 'element' | 'position'

type FragmentType = 'boundingBox' | 'element'


// type BoundingBoxFG = ɵValue<FormGroup<{
//   w: FormControl<number>;
//   x: FormControl<number>;
//   h: FormControl<number>;
//   y: FormControl<number>
// }>>;

interface ScreenResolution {
  name: string;
  width: number;
  height: number;
}

type RenderEngine = 'static' | 'chrome';

type SourceForm = {
  url: FormControl<string>,
  renderEngine: FormControl<RenderEngine>,
  screenResolution: FormControl<ScreenResolution>,
  actions: FormArray<FormGroup<TypedFormControls<ScrapeAction>>>
};

type OneOfClick = {
  type: ClickType;
  oneOf: GqlDomElementInput;
};

type OneOfAction = {
  cookie?: InputMaybe<GqlCookieValueInput>;
  header?: InputMaybe<GqlRequestHeaderInput>;
  select?: InputMaybe<GqlDomActionSelectInput>;
  type?: InputMaybe<GqlDomActionTypeInput>;
  wait?: InputMaybe<GqlWaitActionInput>;
  click?: OneOfClick
};

type ScrapeAction = {
  type: ActionType;
  oneOf?: OneOfAction
};

interface WebsiteToFeedModalContext {
  feed?: NativeOrGenericFeed;
}


@Component({
  selector: 'app-scrape-source',
  templateUrl: './scrape-source.component.html',
  styleUrls: ['./scrape-source.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ScrapeSourceComponent implements OnInit, OnDestroy {

  @Output()
  requestChanged: EventEmitter<GqlScrapeRequestInput> = new EventEmitter<GqlScrapeRequestInput>();

  @Output()
  responseChanged: EventEmitter<ScrapeResponse> = new EventEmitter<ScrapeResponse>();

  @Input()
  scrapeRequest: GqlScrapeRequestInput;

  @Input()
  scrapeResponse: ScrapeResponse;

  formGroup: FormGroup<SourceForm> = new FormGroup<SourceForm>({
    url: new FormControl<RenderEngine>(null, {nonNullable: true, validators: [Validators.required, Validators.minLength(4)]}),
    renderEngine: new FormControl<RenderEngine>('static', {nonNullable: true, validators: [Validators.required]}),
    screenResolution: new FormControl<ScreenResolution>(null, {nonNullable: true, validators: [Validators.required]}),
    actions: new FormArray<FormGroup<TypedFormControls<ScrapeAction>>>([])
  }, {updateOn: 'change'});

  private subscriptions: Subscription[] = [];

  isDarkMode: boolean;
  embedMarkup: Embeddable;
  embedScreenshot: Embeddable;

  screenResolutions: ScreenResolution[] = [
    {
      name: 'XGA',
      width: 1024,
      height: 768
    },
    {
      name: 'HD720',
      width: 1280,
      height: 720
    },
    {
      name: 'WXGA',
      width: 1280,
      height: 800
    },
    {
      name: 'SXGA',
      width: 1280,
      height: 1024
    }
  ];

  loading = false;
  view: View = 'screenshot';
  pickElementDelegate: (xpath: string | null) => void;
  pickPositionDelegate: (position: GqlXyPosition | null) => void;
  pickBoundingBoxDelegate: (boundingBox: BoundingBox | null) => void;

  protected readonly isDefined = isDefined;

  totalTime: string;
  renderOptions: KeyLabelOption<RenderEngine>[] = [
    { key: 'static', label: 'Static Response' },
    { key: 'chrome', label: 'Headless Browser' }
  ];
  errorMessage: string;
  highlightXpath: string;

  websiteToFeedModalContext: WebsiteToFeedModalContext;

  mapperFg = new FormGroup({
    type: new FormControl<ResponseMapper>(null, {nonNullable: true, validators: [Validators.required]}),
    oneOf: new FormGroup({
      feed: new FormControl<NativeOrGenericFeed>(null, {nonNullable: true, validators: [Validators.required]}),
      fragment: new FormGroup({
        fragmentType: new FormControl<FragmentType>('element', { nonNullable: true, validators: [Validators.required] }),
        boundingBox: new FormGroup({
          x: new FormControl<number>(0, { nonNullable: false, validators: [Validators.required] }),
          y: new FormControl<number>(0, { nonNullable: false, validators: [Validators.required] }),
          h: new FormControl<number>(0, { nonNullable: false, validators: [Validators.required, Validators.min(10)] }),
          w: new FormControl<number>(0, { nonNullable: false, validators: [Validators.required, Validators.min(10)] })
        }),
        xpath: new FormControl<string>('', { nonNullable: false, validators: [Validators.required, Validators.minLength(2)] })
      })
    })
  });

  clickTypes: KeyLabelOption<ClickType>[] = [
    {
      key: 'element',
      label: 'Element',
      default: true
    },
    {
      key: 'position',
      label: 'Position'
    }
  ];
  fragmentTypes: KeyLabelOption<FragmentType>[] = [
    {
      key: 'element',
      label: 'Element',
      default: true
    },
    {
      key: 'boundingBox',
      label: 'Bounding Box'
    }
  ];

  scrapeActionOptions: KeyLabelOption<ActionType>[] = [
    {
      key: 'click',
      label: 'Click',
      default: true
    },
    {
      key: 'cookie',
      label: 'Cookie'
    },
    {
      key: 'header',
      label: 'Header'
    },
    {
      key: 'select',
      label: 'Select'
    },
    {
      key: 'type',
      label: 'Type'
    },
    {
      key: 'wait',
      label: 'Wait'
    }
  ];

  @ViewChild('websiteToFeedModal')
  websiteToFeedModalElement: HTMLIonModalElement

  constructor(
    readonly profile: ProfileService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly scrapeService: ScrapeService
  ) {
  }

  ngOnInit() {
    if (this.scrapeRequest) {
      this.formGroup.controls.url.setValue(this.scrapeRequest.page.url);
      this.formGroup.controls.renderEngine.setValue(this.scrapeRequest.page.prerender ? 'chrome' : 'static');
      this.formGroup.controls.screenResolution.setValue(this.screenResolutions[0]);

      if (this.scrapeRequest.page.actions) {
        this.scrapeRequest.page.actions.forEach(action => this.addAction(action))
      }
      // this.changeRef.detectChanges();
    }
    if (this.scrapeResponse) {
      this.handleScrapeResponse(this.scrapeResponse);
      this.changeRef.detectChanges();
    }
    this.subscriptions.push(
      this.profile.watchColorScheme().subscribe((isDarkMode) => {
        this.isDarkMode = isDarkMode;
        this.changeRef.detectChanges();
      }),
      this.formGroup.valueChanges
        .pipe(debounce(() => interval(200)))
        .subscribe(values => {
          console.log('formChange', values);
          // return this.scrapeUrl();
        })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async scrapeUrl() {
    try {
      if (!isValidUrl(this.formGroup.value.url)) {
        this.formGroup.controls.url.setValue(fixUrl(this.formGroup.value.url));
      }
      this.changeRef.detectChanges();

      if (this.loading) {
        return;
      }

      this.errorMessage = null;
      this.loading = true;
      this.changeRef.detectChanges();

      const scrapeRequest = this.getScrapeRequest();
      this.requestChanged.emit(scrapeRequest);

      const scrapeResponse = await this.scrapeService.scrape(scrapeRequest);
      this.handleScrapeResponse(scrapeResponse);

      this.formGroup.markAsUntouched();

    } catch (e) {
      this.errorMessage = e.message;
    }
    this.loading = false;
    this.changeRef.detectChanges();
  }

  handlePickedXpath(xpath: string) {
    if (this.pickElementDelegate) {
      this.highlightXpath = xpath;
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

  handlePickedBoundingBox(boundingBox: BoundingBox | null) {
    if (this.pickBoundingBoxDelegate) {
      this.pickBoundingBoxDelegate(boundingBox);
      this.pickBoundingBoxDelegate = null;
      this.changeRef.detectChanges();
    }
  }

  // async handleActionChanged(actions: GqlScrapeActionInput[]) {
  //   console.log('actions changed');
  //   this.actions = actions;
  //   // await this.scrapeUrl();
  // }

  private getScrapeRequest(): GqlScrapeRequestInput {

    let prerender: InputMaybe<GqlScrapePrerenderInput>;

    if (this.formGroup.value.renderEngine === 'chrome') {
      prerender = {
        waitUntil: GqlPuppeteerWaitUntil.Load,
        viewport: {
          isMobile: false,
          height: this.formGroup.value.screenResolution.height,
          width: this.formGroup.value.screenResolution.width
        }
      };
    }

    return {
      page: {
        url: this.formGroup.value.url,
        actions: this.toScrapeActions(),
        prerender
      },
      debug: {
        screenshot: true,
        console: true,
        cookies: true,
        html: true
      },
      emit: this.getScrapeEmitTypes(),
      elements: ['/']
    };
  }

  private handleScrapeResponse(scrapeResponse: ScrapeResponse) {
    if (scrapeResponse.failed) {
      this.errorMessage = scrapeResponse.errorMessage;
    } else {
      this.responseChanged.emit(scrapeResponse);
      this.totalTime =
        (
          (scrapeResponse.debug.metrics.queue + scrapeResponse.debug.metrics.render) /
          1000
        ).toFixed(2) + 's';
      this.view = 'markup';
      console.log('response.debug.contentType', scrapeResponse.debug.contentType);
      const url = this.formGroup.value.url;
      this.embedMarkup = {
        mimeType: scrapeResponse.debug.contentType,
        data: scrapeResponse.debug.html,
        url,
        viewport: scrapeResponse.debug.viewport
      };
      if (scrapeResponse.debug.screenshot) {
        this.view = 'screenshot';
        this.embedScreenshot = {
          mimeType: 'image/png',
          data: scrapeResponse.debug.screenshot,
          url,
          viewport: scrapeResponse.debug.viewport
        };
      }
    }
  }

  screenResolutionLabelProvider(sr: ScreenResolution): string {
    return `${sr.name} ${sr.width}x${sr.height}`;
  }

  private async ensureScreenshotExists() {
    console.log('ensureScreenshotExists');
    if (!this.embedScreenshot && this.formGroup.value.renderEngine !== 'chrome') {
      this.formGroup.controls.renderEngine.setValue('chrome');
    }
  }

  isPickAnyModeActive(): boolean {
    return isDefined(this.pickPositionDelegate) || isDefined(this.pickElementDelegate) || isDefined(this.pickBoundingBoxDelegate);
  }

  getPickModeLabel(): string {
    if (isDefined(this.pickPositionDelegate)) {
      return 'Pick a position';
    }
    if (isDefined(this.pickElementDelegate)) {
      return 'Pick an element';
    }
    if (isDefined(this.pickBoundingBoxDelegate)) {
      return 'Draw a bounding box';
    }
    throw new Error('not supported');
  }

  resetPickMode() {
    this.pickPositionDelegate = null;
    this.pickElementDelegate = null;
    this.pickBoundingBoxDelegate = null;
    this.changeRef.detectChanges();
  }

  deleteAction(action: FormGroup<TypedFormControls<ScrapeAction>>) {
    this.formGroup.controls.actions.controls = without(this.formGroup.controls.actions.controls, action);
    // this.emitActions();
  }

  addAction(action?: GqlScrapeActionInput) {
    const type =
      Object.keys(action || {}).find((attr) => isDefined(action[attr])) ||
      ('click' as any);

    const strFcOptions = (): FormControlOptions => ({nonNullable: true, validators: [Validators.required, Validators.minLength(1)]});

    const elementByNameOrXpath = (name: string) => new FormGroup<TypedFormControls<GqlDomElementByNameOrXPathInput>>({
      name: new FormGroup<TypedFormControls<GqlDomElementByNameInput>>({
        value: new FormControl<string>(name, strFcOptions())
      }),
      xpath: new FormGroup<TypedFormControls<GqlDomElementByXPathInput>>({
        value: new FormControl<string>(null, strFcOptions())
      })
    });
    const actionFc: FormGroup<TypedFormControls<ScrapeAction>> = new FormGroup<TypedFormControls<ScrapeAction>>({
      type: new FormControl<ActionType>(null),
      oneOf: new FormGroup<TypedFormControls<OneOfAction>>({
        header: new FormGroup<TypedFormControls<GqlRequestHeaderInput>>({
          name: new FormControl<string>(null, strFcOptions()),
          value: new FormControl<string>(null, strFcOptions()),
        }),
        wait: new FormGroup<TypedFormControls<GqlWaitActionInput>>({
          element: elementByNameOrXpath(action?.wait?.element?.name?.value)
        }),
        cookie: new FormGroup<TypedFormControls<GqlCookieValueInput>>({
          value: new FormControl<string>(null, strFcOptions()),
        }),
        select: new FormGroup<TypedFormControls<GqlDomActionSelectInput>>({
          selectValue: new FormControl<string>(null, strFcOptions()),
          element: new FormGroup<TypedFormControls<GqlDomElementByXPathInput>>({
            value: new FormControl<string>(null, strFcOptions()),
          })
        }),
        type: new FormGroup<TypedFormControls<GqlDomActionTypeInput>>({
          typeValue: new FormControl<string>(null, strFcOptions()),
          element: new FormGroup<TypedFormControls<GqlDomElementByXPathInput>>({
            value: new FormControl<string>(null, strFcOptions()),
          })
        }),
        click: new FormGroup<TypedFormControls<OneOfClick>>({
          type: new FormControl<ClickType>(null, {nonNullable: true}),
          oneOf: new FormGroup<TypedFormControls<GqlDomElementInput>>({
            element: elementByNameOrXpath(action?.click?.element?.name?.value),
            position: new FormGroup<TypedFormControls<GqlXyPositionInput>>({
              x: new FormControl<number>(action?.click?.position?.x, {nonNullable: true, validators: [Validators.required, Validators.min(1)]}),
              y: new FormControl<number>(action?.click?.position?.y, {nonNullable: true, validators: [Validators.required, Validators.min(1)]})
            }),
          })
        })
      })
    });

    actionFc.controls.type.valueChanges.subscribe(type => {
      Object.keys(actionFc.controls.oneOf.controls)
        .forEach(otherKey => {
          if (otherKey === type) {
            actionFc.controls.oneOf.controls[otherKey].enable();
          } else {
            actionFc.controls.oneOf.controls[otherKey].disable();
          }
        })
    });
    actionFc.controls.oneOf.controls.click.controls.type.valueChanges.subscribe(type => {
      Object.keys(actionFc.controls.oneOf.controls.click.controls.oneOf.controls)
        .forEach(otherKey => {
          if (otherKey === type) {
            actionFc.controls.oneOf.controls.click.controls.oneOf.controls[otherKey].enable();
          } else {
            actionFc.controls.oneOf.controls.click.controls.oneOf.controls[otherKey].disable();
          }
        })
    });

    actionFc.patchValue({
      type,
      oneOf: {
        header: {
          name: action?.header?.name,
          value: action?.header?.name,
        },
        type: {
          element: {
            value: action?.type?.element?.value,
          },
          typeValue: action?.type?.typeValue
        },
        cookie: {
          value: action?.cookie?.value
        },
        select: {
          selectValue: action?.select?.selectValue,
          element: {
            value: action?.select?.element?.value
          }
        },
        click: {
          type: 'element',
          oneOf: {
            position: {
              x: action?.click?.position?.x,
              y: action?.click?.position?.y,
            },
            element: {
              name: action?.click?.element?.name,
              xpath: action?.click?.element?.xpath
            }
          }
        }
      }
    })

    this.formGroup.controls.actions.controls.push(actionFc);

    actionFc.statusChanges
      .pipe(debounce(() => interval(200)))
      .subscribe(values => {
        console.log('dirty');
        // return this.scrapeUrl();
      })

    // this.emitActions();
    this.changeRef.detectChanges();
  }

  private toScrapeActions(): GqlScrapeActionInput[] {
    return this.formGroup.controls.actions.controls.map<GqlScrapeActionInput>(
      (actionFromGroup) => {
        const control = actionFromGroup.value;
        const type = control.type;
        switch (type) {
          case 'click':
            const clickType = control.oneOf.click.type;
            if (clickType == 'element') {
              return {
                click: {
                  element: {
                    xpath: {
                      value: control.oneOf.click.oneOf.element.xpath.value,
                    },
                    // name: {
                    //   value: control.value.oneOf.click.oneOf.selector
                    // }
                  },
                },
              };
            } else if (clickType == 'position') {
              return {
                click: {
                  position: {
                    x: control.oneOf.click.oneOf.position.x,
                    y: control.oneOf.click.oneOf.position.y,
                  },
                },
              };
            } else {
              throw new Error('unsupported');
            }
          case 'cookie':
            return {
              cookie: {
                value: control.oneOf.cookie.value,
              },
            };
          case 'header':
            return {
              header: {
                value: control.oneOf.header.value,
                name: control.oneOf.header.name,
              },
            };
          case 'wait':
            return {
              wait: {
                element: {
                  xpath: {
                    value: control.oneOf.wait.element.xpath.value,
                  },
                  // name: {
                  //   value: control.value.oneOf.click.oneOf.selector
                  // }
                },
              },
            };
          case 'select':
            return {
              select: {
                element: {
                  value: control.oneOf.select.element.value,
                },
                selectValue: control.oneOf.select.selectValue,
              },
            };

          case 'type':
            return {
              type: {
                element: {
                  value: control.oneOf.type.element.value,
                },
                typeValue: control.oneOf.type.typeValue,
              },
            };
        }
      },
    );
  }

  labelForXpath(xpath: string): string {
    if (xpath) {
      return xpath.substring(0, 25)+ '...';
    } else {
      return 'Choose Element'
    }
  }

  labelForPosition(position: ɵValue<TypedFormControls<InputMaybe<GqlDomElementInput>>['position']>): string {
    if (position.x && position.y) {
      return `(${position.x}, ${position.y})`;
    } else {
      return 'Click X/Y Coordinates'
    }
  }

  labelForBoundingBox(boundingBoxFG: FormGroup<{
    w: FormControl<number>;
    x: FormControl<number>;
    h: FormControl<number>;
    y: FormControl<number>
  }>): string {
    if (boundingBoxFG.valid) {
      const {x,y,w,h} = boundingBoxFG.value
      return `[${x},${y},${w},${h}]`
    } else {
      return 'Choose Bounding Box'
    }
  }

  async triggerPickBoundingBox(boundingBoxSink: FormGroup<{
    w: FormControl<number | null>;
    x: FormControl<number | null>;
    h: FormControl<number | null>;
    y: FormControl<number | null>
  }>) {
    await this.ensureScreenshotExists();
    this.view = 'screenshot';
    this.pickBoundingBoxDelegate = (boundingBox: BoundingBox | null) => {
      if (boundingBox) {
        boundingBoxSink.setValue({
          y: boundingBox.y,
          w: boundingBox.w,
          h: boundingBox.h,
          x: boundingBox.x
        })
      }
    };
  }

  triggerPickElement(xpathSink: FormControl<string>) {
    this.view = 'markup';
    this.pickElementDelegate = (xpath: string | null) => {
      if (xpath) {
        xpathSink.setValue(xpath);
      }
    };
  }

  async triggerPickPosition(positionSink: FormGroup<TypedFormControls<GqlDomElementInput['position']>>) {
    await this.ensureScreenshotExists();
    this.view = 'screenshot';
    this.pickPositionDelegate = (position: XyPosition | null) => {
      if (position) {
        positionSink.setValue({
          x: position.x,
          y: position.y
        });
      }
    };
  }

  // async registerPickPositionDelegate(callback: (position: XyPosition | null) => void) {
  //   await this.ensureScreenshotExists();
  //   this.view = 'screenshot';
  //   this.isFullscreenMode = true;
  //   this.pickPositionDelegate = (position: XyPosition | null) => {
  //     this.isFullscreenMode = false;
  //     callback(position);
  //   };
  // }
  //
  // async registerPickBoundingBoxDelegate(callback: (boundingBox: BoundingBox | null) => void) {
  //   await this.ensureScreenshotExists();
  //   this.view = 'screenshot';
  //   this.isFullscreenMode = true;
  //   this.pickBoundingBoxDelegate = (boundingBox: BoundingBox | null) => {
  //     this.isFullscreenMode = false;
  //     callback(boundingBox);
  //   };
  // }

  setValue<T>(ctrl: FormControl<T>, value: T) {
    ctrl.setValue(value);
    ctrl.markAsTouched();
  }

  getMapperOptions(): KeyLabelOption<ResponseMapper>[] {
    return [
      {
        key: 'feed',
        label: 'Feed'
      },
      {
        key: 'fragment',
        label: 'Fragment'
      },
      {
        key: 'readability',
        label: 'Readability'
      },
    ];
  }

  protected readonly GqlScrapeEmitType = GqlScrapeEmitType;

  triggerPickFeed(feed: FormControl<NativeOrGenericFeed | null>) {
    return this.openWebsiteToFeedModal(feed);
  }

  labelForFeedMapper(feed: FormControl<NativeOrGenericFeed | null>) {
    if (feed.value?.genericFeed) {
      return 'Generic Feed'
    } else {
      if (feed.value?.nativeFeed) {
        return 'Native Feed';
      } else {
        return 'Choose a Feed';
      }
    }
  }

  // -- w2f modal --------------------------------------------------------------

  private async openWebsiteToFeedModal(feed: FormControl<NativeOrGenericFeed | null>) {
    this.websiteToFeedModalContext = {
      feed: feed.value,
    };
    await this.websiteToFeedModalElement.present()
  }
  async dismissWebsiteToFeedModal() {
    this.websiteToFeedModalContext = null;
    await this.websiteToFeedModalElement.dismiss()
  }

  async applyChangesFromWebsiteToFeedModal() {
    const {  feed } = this.websiteToFeedModalContext;
    console.log('applyChangesFromWebsiteToFeedModal', feed);
    // this.websiteToFeedModalContext.sourceBuilder.withMapper({
    //   feed
    // });
    await this.dismissWebsiteToFeedModal();
  }

  private getScrapeEmitTypes(): GqlScrapeEmitType[] {
    switch (this.mapperFg.value.type) {
      case 'fragment':
        break;
      case 'readability':
        return [GqlScrapeEmitType.Readability];
      case 'feed':
      default:
        return [GqlScrapeEmitType.Feeds];
    }
  }
}
