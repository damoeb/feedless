import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { ProfileService } from '../../services/profile.service';
import { debounce, interval, merge, Subscription } from 'rxjs';
import { Embeddable } from '../embedded-website/embedded-website.component';
import {
  GqlCookieValueInput,
  GqlDomActionSelectInput,
  GqlDomActionTypeInput,
  GqlDomElementByNameInput,
  GqlDomElementByNameOrXPathInput,
  GqlDomElementByXPathInput,
  GqlDomElementInput,
  GqlMarkupTransformer,
  GqlPuppeteerWaitUntil,
  GqlRequestHeaderInput,
  GqlScrapeAction,
  GqlScrapeActionInput,
  GqlScrapeEmitInput,
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
import { ScrapedElement, ScrapeResponse } from '../../graphql/types';
import { KeyLabelOption } from '../select/select.component';
import { BoundingBox, XyPosition } from '../embedded-image/embedded-image.component';
import { isDefined, ResponseMapper } from '../../modals/feed-builder-modal/scrape-builder';
import {
  NativeOrGenericFeed,
  TransformWebsiteToFeedComponent,
  TransformWebsiteToFeedComponentProps
} from '../transform-website-to-feed/transform-website-to-feed.component';
import { ModalController } from '@ionic/angular';
import { ModalService } from '../../services/modal.service';
import { Source } from '../../modals/feed-builder-modal/feed-builder-modal.component';
import { isNull, isUndefined, startCase } from 'lodash-es';

type View = 'screenshot' | 'markup';


type ActionType = keyof GqlScrapeAction

type AnyPrimitive = string | number | boolean
export type TypedFormGroup<TYPE> = {
  [K in keyof TYPE]: TYPE[K] extends AnyPrimitive
    ? FormControl<TYPE[K] | null>
    : (
      TYPE[K] extends Array<infer U>
        ? (
          U extends AnyPrimitive ? FormArray<FormControl<U | null>>
            : FormArray<FormGroup<TypedFormGroup<U>>>
          )
        : FormGroup<TypedFormGroup<TYPE[K]>>
      );
};

type ClickType = 'element' | 'position'

type FragmentType = 'boundingBox' | 'element'

interface ScrapeEmits {
  emit: GqlScrapeEmitInput[],
  debug?: {
    screenshot?: boolean,
    console?: boolean,
    cookies?: boolean,
    html?: boolean
  }
}

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
  actions: FormArray<FormGroup<TypedFormGroup<ScrapeAction>>>
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

export interface ScrapeSourceComponentProps {
  source: Source;
}

export interface ScrapeSourceDismissalData {
  request: GqlScrapeRequestInput;
  response: ScrapeResponse;
}

type MapperForm = {
  type: FormControl<ResponseMapper>
  oneOf: FormGroup<{
    feed: FormControl<NativeOrGenericFeed>
    fragment: FormGroup<{
      fragmentType: FormControl<FragmentType>,
      xpath: FormControl<string>
      boundingBox: FormGroup<{
        x: FormControl<number>,
        y: FormControl<number>,
        h: FormControl<number>,
        w: FormControl<number>
      }>
    }>
  }>
}

@Component({
  selector: 'app-scrape-source',
  templateUrl: './scrape-source.component.html',
  styleUrls: ['./scrape-source.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ScrapeSourceComponent implements OnInit, OnDestroy, ScrapeSourceComponentProps {

  @Output()
  requestChanged: EventEmitter<GqlScrapeRequestInput> = new EventEmitter<GqlScrapeRequestInput>();

  @Output()
  responseChanged: EventEmitter<ScrapeResponse> = new EventEmitter<ScrapeResponse>();

  @Input()
  source: Source;

  private scrapeResponse: ScrapeResponse;

  scrapeRequestFG: FormGroup<SourceForm> = new FormGroup<SourceForm>({
    url: new FormControl<RenderEngine>(null, { nonNullable: true, validators: [Validators.required, Validators.minLength(4)] }),
    renderEngine: new FormControl<RenderEngine>('static', { nonNullable: true, validators: [Validators.required] }),
    screenResolution: new FormControl<ScreenResolution>(null, { nonNullable: true, validators: [Validators.required] }),
    actions: new FormArray<FormGroup<TypedFormGroup<ScrapeAction>>>([])
  }, { updateOn: 'change', validators: [Validators.required] });

  private subscriptions: Subscription[] = [];

  isDarkMode: boolean;
  embedMarkup: Embeddable;
  embedScreenshot: Embeddable;

  screenResolutions: KeyLabelOption<ScreenResolution>[] = [
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
  ].map(sr => ({
      key: sr,
      label: `${sr.name} ${sr.width}x${sr.height}`
    }
  ));


  isLoading = false;
  view: View = 'screenshot';
  pickElementDelegate: (xpath: string | null) => void;
  pickPositionDelegate: (position: GqlXyPosition | null) => void;
  pickBoundingBoxDelegate: (boundingBox: BoundingBox | null) => void;
  scrapedElements: { viewId: string, label: string, data: any }[];

  protected isDefined(v: any | undefined): boolean {
    return !isNull(v) && !isUndefined(v);
  }

  totalTime: string;
  renderOptions: KeyLabelOption<RenderEngine>[] = [
    { key: 'static', label: 'Static Response' },
    { key: 'chrome', label: 'Headless Browser' }
  ];
  errorMessage: string;
  highlightXpath: string;

  mapperFg = new FormGroup<MapperForm>({
    type: new FormControl<ResponseMapper>(null, { nonNullable: true, validators: [Validators.required] }),
    oneOf: new FormGroup({
      feed: new FormControl<NativeOrGenericFeed>(null, { nonNullable: true, validators: [Validators.required] }),
      fragment: new FormGroup({
        fragmentType: new FormControl<FragmentType>('element', { nonNullable: true, validators: [Validators.required] }),
        boundingBox: new FormGroup({
          x: new FormControl<number>(0, { nonNullable: false, validators: [Validators.required] }),
          y: new FormControl<number>(0, { nonNullable: false, validators: [Validators.required] }),
          h: new FormControl<number>(0, { nonNullable: false, validators: [Validators.required, Validators.min(10)] }),
          w: new FormControl<number>(0, { nonNullable: false, validators: [Validators.required, Validators.min(10)] })
        }, { validators: [Validators.required] }),
        xpath: new FormControl<string>('', { nonNullable: false, validators: [Validators.required, Validators.minLength(2)] })
      }, { validators: [Validators.required] })
    }, { validators: [Validators.required] })
  });

  clickTypes: KeyLabelOption<ClickType>[] = [
    {
      key: 'element',
      label: 'Element'
    },
    {
      key: 'position',
      label: 'Position'
    }
  ];
  fragmentTypes: KeyLabelOption<FragmentType>[] = [
    {
      key: 'element',
      label: 'Element'
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

  constructor(
    readonly profile: ProfileService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly modalCtrl: ModalController,
    private readonly modalService: ModalService,
    private readonly scrapeService: ScrapeService
  ) {
    this.mapperFg.controls.type.valueChanges
      .subscribe((mapperType) => {
        if (!mapperType) {
          return;
        }
        switch (mapperType) {
          case 'fragment':
          case 'pageScreenshot':
            this.ensureRenderEngineIsChrome();
            break;
        }

        console.log('map to', mapperType);
        switch (mapperType) {
          case 'pageScreenshot':
          case 'pageMarkup':
          case 'readability':
            this.mapperFg.controls.oneOf.disable();
            break;
          default:
            this.mapperFg.controls.oneOf.enable();
            break;
        }

        Object.keys(this.mapperFg.controls.oneOf.controls)
          .forEach(otherKey => {
            if (otherKey === mapperType) {
              this.mapperFg.controls.oneOf.controls[otherKey].enable({ onlySelf: true });
            } else {
              this.mapperFg.controls.oneOf.controls[otherKey].disable({ onlySelf: true });
            }
          });

        merge(
          this.mapperFg.controls.type.valueChanges,
          this.mapperFg.controls.oneOf.controls.feed.valueChanges,
          this.mapperFg.controls.oneOf.controls.fragment.valueChanges
        )
          .pipe(debounce(() => interval(800)))
          .subscribe(async () => {
            if (this.mapperFg.valid) {
              return this.scrapeUrl();
            }
          });

        // console.log(JSON.stringify(this.getFormControlStatus(this.mapperFg), null, 2));
      });
  }

  async ngOnInit() {
    this.mapperFg.disable();
    this.subscriptions.push(
      this.profile.watchColorScheme().subscribe((isDarkMode) => {
        this.isDarkMode = isDarkMode;
        this.changeRef.detectChanges();
      }),
      // this.formGroup.valueChanges
      //   .pipe(debounce(() => interval(200)))
      //   .subscribe(values => {
      //     console.log('formChange', values);
      //     // return this.scrapeUrl();
      //   })
      this.scrapeRequestFG.controls.url.statusChanges.subscribe(status => {
        if (status === 'VALID') {
          this.mapperFg.enable({ onlySelf: true });
        } else {
          this.mapperFg.disable({ onlySelf: true });
        }
      }),
      merge(
        this.scrapeRequestFG.controls.renderEngine.valueChanges,
        this.scrapeRequestFG.controls.screenResolution.valueChanges
      ).pipe(debounce(() => interval(800)))
        .subscribe(() => {
          this.scrapeUrl();
        })
    );

    if (this.source?.request) {
      this.scrapeRequestFG.controls.url.setValue(this.source.request.page.url);
      this.scrapeRequestFG.controls.renderEngine.setValue(this.source.request.page.prerender ? 'chrome' : 'static');
      this.scrapeRequestFG.controls.screenResolution.setValue(this.screenResolutions[0].key);

      if (this.source.request.page.actions) {
        this.source.request.page.actions.forEach(action => this.addAction(action));
      }

      // this.mapperFg.patchValue(this.parseMapper(this.source?.request))

      if (!this.source?.response) {
        await this.scrapeUrl();
      }
    }

    if (this.source?.response) {
      this.handleScrapeResponse(this.source?.response);
      this.changeRef.detectChanges();
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  async scrapeUrl() {
    try {
      if (!isValidUrl(this.scrapeRequestFG.value.url)) {
        this.scrapeRequestFG.controls.url.setValue(fixUrl(this.scrapeRequestFG.value.url));
      }
      this.changeRef.detectChanges();

      if (this.isLoading) {
        return;
      }

      this.errorMessage = null;
      this.isLoading = true;
      this.changeRef.detectChanges();

      const scrapeRequest = this.getScrapeRequest();
      this.requestChanged.emit(scrapeRequest);

      const scrapeResponse = await this.scrapeService.scrape(scrapeRequest);
      this.handleScrapeResponse(scrapeResponse);

      this.scrapeRequestFG.markAsUntouched();

    } catch (e) {
      this.errorMessage = e.message;
    }
    this.isLoading = false;
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

  private getScrapeRequest(debug = true): GqlScrapeRequestInput {

    let prerender: InputMaybe<GqlScrapePrerenderInput>;

    if (this.scrapeRequestFG.value.renderEngine === 'chrome') {
      prerender = {
        waitUntil: GqlPuppeteerWaitUntil.Load,
        viewport: {
          isMobile: false,
          height: this.scrapeRequestFG.value.screenResolution.height,
          width: this.scrapeRequestFG.value.screenResolution.width
        }
      };
    }

    const emit = this.getScrapeEmits(debug);
    const actions = this.toScrapeActions();
    return {
      page: {
        url: this.scrapeRequestFG.value.url,
        actions,
        prerender
      },
      ...emit
    };
  }

  private handleScrapeResponse(scrapeResponse: ScrapeResponse) {
    this.scrapeResponse = scrapeResponse;
    if (scrapeResponse.failed) {
      this.errorMessage = scrapeResponse.errorMessage;
    } else {
      this.responseChanged.emit(scrapeResponse);
      this.scrapedElements = scrapeResponse.elements
        ?.map((element: ScrapedElement, index: number) => {
          if (element.image) {
            return {
              viewId: `${index}`,
              label: `Image #${index + 1}`,
              data: element
            };
          } else {
            const excludedTransformers = [GqlMarkupTransformer.Feeds];
            if (this.mapperFg.value.type !== 'readability') {
              excludedTransformers.push(GqlMarkupTransformer.Readability)
            }

            return element.selector.fields
              ?.filter(field => !excludedTransformers.includes(field.transformer.internal))
              ?.map((field, fieldIndex) => {
              return {
                viewId: `${index}/${fieldIndex}`,
                label: startCase(`${field.name}`),
                data: field
              };
            }) ?? [];
          }
        }).flat();

      this.totalTime =
        (
          (scrapeResponse.debug.metrics.queue + scrapeResponse.debug.metrics.render) /
          1000
        ).toFixed(2) + 's';
      this.view = 'markup';
      console.log('response.debug.contentType', scrapeResponse.debug.contentType);
      const url = this.scrapeRequestFG.value.url;
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

  private async ensureRenderEngineIsChrome() {
    if (!this.embedScreenshot && this.scrapeRequestFG.value.renderEngine !== 'chrome') {
      this.scrapeRequestFG.controls.renderEngine.setValue('chrome');
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

  deleteAction(action: FormGroup<TypedFormGroup<ScrapeAction>>) {
    this.scrapeRequestFG.controls.actions.removeAt(this.scrapeRequestFG.controls.actions.controls.indexOf(action));
  }

  async addAction(action?: GqlScrapeActionInput) {
    await this.ensureRenderEngineIsChrome();
    const type =
      Object.keys(action || {}).find((attr) => isDefined(action[attr])) ||
      ('click' as any);

    const strFcOptions = (): FormControlOptions => ({ nonNullable: true, validators: [Validators.required, Validators.minLength(1)] });

    const newFormControl = () => new FormControl<string>(null, strFcOptions());

    const elementByNameOrXpath = (name: string) => new FormGroup<TypedFormGroup<GqlDomElementByNameOrXPathInput>>({
      name: new FormGroup<TypedFormGroup<GqlDomElementByNameInput>>({
        value: new FormControl<string>(name, strFcOptions())
      }),
      xpath: new FormGroup<TypedFormGroup<GqlDomElementByXPathInput>>({
        value: newFormControl()
      })
    });
    const newFormGroupOpts = () => ({ validators: [Validators.required] });
    const actionFg: FormGroup<TypedFormGroup<ScrapeAction>> = new FormGroup<TypedFormGroup<ScrapeAction>>({
      type: new FormControl<ActionType>(null, { nonNullable: true, validators: [Validators.required] }),
      oneOf: new FormGroup<TypedFormGroup<OneOfAction>>({
        header: new FormGroup<TypedFormGroup<GqlRequestHeaderInput>>({
          name: newFormControl(),
          value: newFormControl()
        }, newFormGroupOpts()),
        wait: new FormGroup<TypedFormGroup<GqlWaitActionInput>>({
          element: elementByNameOrXpath(action?.wait?.element?.name?.value)
        }, newFormGroupOpts()),
        cookie: new FormGroup<TypedFormGroup<GqlCookieValueInput>>({
          value: newFormControl()
        }, newFormGroupOpts()),
        select: new FormGroup<TypedFormGroup<GqlDomActionSelectInput>>({
          selectValue: newFormControl(),
          element: new FormGroup<TypedFormGroup<GqlDomElementByXPathInput>>({
            value: newFormControl()
          })
        }, newFormGroupOpts()),
        type: new FormGroup<TypedFormGroup<GqlDomActionTypeInput>>({
          typeValue: newFormControl(),
          element: new FormGroup<TypedFormGroup<GqlDomElementByXPathInput>>({
            value: newFormControl()
          }, newFormGroupOpts())
        }, newFormGroupOpts()),
        click: new FormGroup<TypedFormGroup<OneOfClick>>({
          type: new FormControl<ClickType>(null, { nonNullable: true, validators: [Validators.required] }),
          oneOf: new FormGroup<TypedFormGroup<GqlDomElementInput>>({
            element: elementByNameOrXpath(action?.click?.element?.name?.value),
            position: new FormGroup<TypedFormGroup<GqlXyPositionInput>>({
              x: new FormControl<number>(action?.click?.position?.x, {
                nonNullable: true,
                validators: [Validators.required, Validators.min(1)]
              }),
              y: new FormControl<number>(action?.click?.position?.y, {
                nonNullable: true,
                validators: [Validators.required, Validators.min(1)]
              })
            }, newFormGroupOpts())
          }, newFormGroupOpts())
        })
      }, newFormGroupOpts())
    }, newFormGroupOpts());

    actionFg.controls.type.valueChanges.subscribe((actionType) => {
      Object.keys(actionFg.controls.oneOf.controls)
        .forEach(otherKey => {
          if (otherKey === actionType) {
            actionFg.controls.oneOf.controls[otherKey].enable({ onlySelf: true, emitEvent: false });
          } else {
            actionFg.controls.oneOf.controls[otherKey].disable({ onlySelf: true, emitEvent: false });
          }
        });
    });
    actionFg.controls.oneOf.controls.click.controls.type.valueChanges
      .subscribe((clickType) => {
        Object.keys(actionFg.controls.oneOf.controls.click.controls.oneOf.controls)
          .forEach(otherKey => {
            if (otherKey === clickType) {
              actionFg.controls.oneOf.controls.click.controls.oneOf.controls[otherKey].enable({ onlySelf: true, emitEvent: false });
            } else {
              actionFg.controls.oneOf.controls.click.controls.oneOf.controls[otherKey].disable({ onlySelf: true, emitEvent: false });
            }
          });
      });

    actionFg.patchValue({
      type,
      oneOf: {
        header: {
          name: action?.header?.name,
          value: action?.header?.name
        },
        type: {
          element: {
            value: action?.type?.element?.value
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
          type: action?.click?.element ? 'element' : 'position',
          oneOf: {
            position: {
              x: action?.click?.position?.x,
              y: action?.click?.position?.y
            },
            element: {
              name: action?.click?.element?.name,
              xpath: action?.click?.element?.xpath
            }
          }
        }
      }
    });

    this.scrapeRequestFG.controls.actions.push(actionFg);

    actionFg.statusChanges
      .pipe(debounce(() => interval(800)))
      .subscribe(status => {
        if (status === 'VALID') {
          return this.scrapeUrl();
        }
      });

    this.changeRef.detectChanges();
  }

  private toScrapeActions(): GqlScrapeActionInput[] {
    return this.scrapeRequestFG.controls.actions.controls
      .filter(actionFg => actionFg.valid)
      .map<GqlScrapeActionInput>(
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
                        value: control.oneOf.click.oneOf.element.xpath.value
                      }
                      // name: {
                      //   value: control.value.oneOf.click.oneOf.selector
                      // }
                    }
                  }
                };
              } else if (clickType == 'position') {
                return {
                  click: {
                    position: {
                      x: control.oneOf.click.oneOf.position.x,
                      y: control.oneOf.click.oneOf.position.y
                    }
                  }
                };
              } else {
                throw new Error('unsupported');
              }
            case 'cookie':
              return {
                cookie: {
                  value: control.oneOf.cookie.value
                }
              };
            case 'header':
              return {
                header: {
                  value: control.oneOf.header.value,
                  name: control.oneOf.header.name
                }
              };
            case 'wait':
              return {
                wait: {
                  element: {
                    xpath: {
                      value: control.oneOf.wait.element.xpath.value
                    }
                    // name: {
                    //   value: control.value.oneOf.click.oneOf.selector
                    // }
                  }
                }
              };
            case 'select':
              return {
                select: {
                  element: {
                    value: control.oneOf.select.element.value
                  },
                  selectValue: control.oneOf.select.selectValue
                }
              };

            case 'type':
              return {
                type: {
                  element: {
                    value: control.oneOf.type.element.value
                  },
                  typeValue: control.oneOf.type.typeValue
                }
              };
          }
        }
      );
  }

  labelForXpath(xpath: string): string {
    if (xpath) {
      return xpath.substring(0, 25) + '...';
    } else {
      return 'Choose Element';
    }
  }

  labelForPosition(position: ɵValue<TypedFormGroup<InputMaybe<GqlDomElementInput>>['position']>): string {
    if (position.x && position.y) {
      return `(${position.x}, ${position.y})`;
    } else {
      return 'Click X/Y Coordinates';
    }
  }

  labelForBoundingBox(boundingBoxFG: FormGroup<{
    w: FormControl<number>;
    x: FormControl<number>;
    h: FormControl<number>;
    y: FormControl<number>
  }>): string {
    if (boundingBoxFG.valid) {
      const { x, y, w, h } = boundingBoxFG.value;
      return `[${x},${y},${w},${h}]`;
    } else {
      return 'Choose Bounding Box';
    }
  }

  async triggerPickBoundingBox(boundingBoxSink: FormGroup<{
    w: FormControl<number | null>;
    x: FormControl<number | null>;
    h: FormControl<number | null>;
    y: FormControl<number | null>
  }>) {
    await this.ensureRenderEngineIsChrome();
    this.view = 'screenshot';
    this.pickBoundingBoxDelegate = (boundingBox: BoundingBox | null) => {
      if (boundingBox) {
        boundingBoxSink.setValue({
          y: boundingBox.y,
          w: boundingBox.w,
          h: boundingBox.h,
          x: boundingBox.x
        });
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

  async triggerPickPosition(positionSink: FormGroup<TypedFormGroup<GqlDomElementInput['position']>>) {
    await this.ensureRenderEngineIsChrome();
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
        label: 'Page Fragment'
      },
      {
        key: 'pageScreenshot',
        label: 'Full Page Screenshot'
      },
      {
        key: 'pageMarkup',
        label: 'Full Page Markup'
      },
      {
        key: 'readability',
        label: 'Readability'
      }
    ];
  }

  triggerPickFeed(feed: FormControl<NativeOrGenericFeed | null>) {
    return this.openWebsiteToFeedModal(feed);
  }

  labelForFeedMapper(feed: FormControl<NativeOrGenericFeed | null>) {
    if (feed.value?.genericFeed) {
      return 'Generic Feed';
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
    const componentProps: TransformWebsiteToFeedComponentProps = {
      scrapeRequest: this.getScrapeRequest(),
      scrapeResponse: this.scrapeResponse,
      feed: feed.value
    };
    const modal = await this.modalCtrl.create({
      component: TransformWebsiteToFeedComponent,
      componentProps
    });

    await modal.present();
    const response = await modal.onDidDismiss();
    if (response.data) {
      this.mapperFg.controls.oneOf.controls.feed.setValue(response.data);
      this.changeRef.detectChanges();
    }
  }

  private getScrapeEmits(debug: boolean): ScrapeEmits {
    const emits = this.getScrapeEmitsWithMapper();
    if (debug) {
      return {
        emit: [...emits.emit, {
          selectorBased: {
            xpath: {
              value: '/'
            },
            expose: {
              transformers:
                [
                  {
                    internal: {
                      transformer: GqlMarkupTransformer.Readability
                    }
                  },
                  {
                    internal: {
                      transformer: GqlMarkupTransformer.Feeds
                    }
                  }
                ]
            }
          }
        }
        ],
        debug: {
          screenshot: true,
          console: true,
          cookies: true,
          html: true
        }
      };
    } else {
      return emits;
    }
  }

  private getScrapeEmitsWithMapper(): ScrapeEmits {
    if (this.mapperFg.valid) {
      switch (this.mapperFg.value.type) {
        case 'pageMarkup':
          return {
            emit: [],
            debug: {
              html: true
            }
          };
        case 'pageScreenshot':
          return {
            emit: [],
            debug: {
              screenshot: true
            }
          };
        case 'fragment':
          switch (this.mapperFg.value.oneOf.fragment.fragmentType) {
            case 'element':
              return {
                emit: [{
                  selectorBased: {
                    xpath: {
                      value: this.mapperFg.value.oneOf.fragment.xpath
                    },
                    expose: {
                    }
                  }
                }
                ]
              };
            case 'boundingBox':
              const bb = this.mapperFg.value.oneOf.fragment.boundingBox;
              return {
                emit: [{
                  imageBased: {
                    boundingBox: {
                      w: bb.w,
                      h: bb.h,
                      x: bb.x,
                      y: bb.y
                    }
                  }
                }
                ]
              };
            default:
              throw new Error(`Unsupported fragmentType ${this.mapperFg.value.oneOf.fragment.fragmentType}`);
          }
        case 'readability':
          return {
            emit: [{
              selectorBased: {
                xpath: {
                  value: '/'
                },
                expose: {
                  transformers: [
                    {
                      internal: {
                        transformer: GqlMarkupTransformer.Readability
                      }
                    }
                  ]
                }
              }
            }]
          };
        case 'feed':
          return {
            emit: [{
              selectorBased: {
                xpath: {
                  value: '/'
                },
                expose: {
                  transformers: [
                    {
                      internal: {
                        transformer: GqlMarkupTransformer.Feed,
                        transformerData: {
                          genericFeed: this.mapperFg.value.oneOf.feed.genericFeed.selectors
                        }
                      }
                    }
                  ]
                }
              }
            }]
          };
      }
    } else {
      return { emit: [] };
    }
  }

  dismissModal() {
    return this.modalCtrl.dismiss();
  }

  applyChanges() {
    const data: ScrapeSourceDismissalData = {
      request: this.getScrapeRequest(false),
      // responseMapper: this.getMapper(),
      response: this.scrapeResponse
    };
    return this.modalCtrl.dismiss(data);
  }

  async openCodeEditor() {
    await this.modalService.openCodeEditorModal(JSON.stringify(this.getScrapeRequest(false), null, 2));
  }

  // private getMapper(): ResponseMapperInput {
  //   const mapper = pick(this.mapperFg.value.oneOf, this.mapperFg.value.type);
  //   return { ...mapper, type: this.mapperFg.value.type }
  // }
  // private parseMapper(request: GqlScrapeRequestInput): DeepPartial<MapperFormData> {
  //   if (request.emit && request.emit.length > 0) {
  //     const firstEmit = request.emit[0];
  //     if (firstEmit.selectorBased) {
  //       if (firstEmit.selectorBased.expose.html) {
  //         return {
  //           type: 'fragment'
  //         }
  //       }
  //       if (firstEmit.selectorBased.expose.transformers) {
  //         const firstTransformer = firstEmit.selectorBased.expose.transformers[0];
  //         if (firstTransformer.internal.transformer === GqlMarkupTransformer.Feed) {
  //           return {
  //             type: 'feed',
  //             oneOf: {
  //               feed: null
  //             }
  //           }
  //         }
  //         if (firstTransformer.internal.transformer === GqlMarkupTransformer.Readability) {
  //           return {
  //             type: 'readability'
  //           }
  //         }
  //
  //       }
  //     }
  //
  //   } else {
  //     if (request.debug.screenshot) {
  //       return {
  //         type: 'pageScreenshot',
  //       }
  //     }
  //     if (request.debug.html) {
  //       return {
  //         type: 'pageMarkup',
  //       }
  //     }
  //     if (request.debug.html) {
  //       return {
  //         type: 'pageMarkup',
  //       }
  //     }
  //   }
  //   return undefined;
  // }
  getMarkupForDynamicView(viewId: string) {
    return this.scrapedElements.find(element => element.viewId === viewId).data;
  }
}
