import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { GqlScrapeAction, GqlScrapeActionInput, GqlXyPosition } from '../../../generated/graphql';
import { without } from 'lodash-es';
import { FormArray, FormControl, FormGroup, Validators, ɵValue } from '@angular/forms';
import { isDefined } from '../wizard/wizard-handler';
import { KeyLabelOption } from '../select/select.component';
import { BoundingBox, XyPosition } from '../embedded-image/embedded-image.component';
import { Subscription } from 'rxjs';

const xpathSelector = (value: string = '') =>
  new FormControl(value, {
    nonNullable: true,
    validators: [Validators.required, Validators.minLength(1)],
  });
const stringField = (value: string = '') =>
  new FormControl(value, {
    nonNullable: true,
    validators: [Validators.required],
  });

type ScrapeAction = keyof GqlScrapeAction

type ActionFormGroup = FormGroup<{
  oneOf: FormGroup<{
    wait: FormGroup<{ selector: FormControl<string> }>;
    select: FormGroup<{
      selector: FormControl<string>;
      text: FormControl<string>;
    }>;
    cookie: FormGroup<{ text: FormControl<string> }>;
    header: FormGroup<{ name: FormControl<string>; text: FormControl<string> }>;
    type: FormGroup<{
      selector: FormControl<string>;
      text: FormControl<string>;
    }>;
    click: FormGroup<{
      oneOf: FormGroup<{
        selector: FormControl<string>;
        position: FormGroup<{ x: FormControl<number>; y: FormControl<number> }>;
      }>;
      type: FormControl<ClickType>;
    }>;
  }>;
  type: FormControl<ScrapeAction>;
}>;

type ClickType = 'element' | 'position'

type FragmentType = 'boundingBox' | 'element'


type BoundingBoxFG = ɵValue<FormGroup<{
  w: FormControl<number>;
  x: FormControl<number>;
  h: FormControl<number>;
  y: FormControl<number>
}>>;

@Component({
  selector: 'app-scrape-actions',
  templateUrl: './scrape-actions.component.html',
  styleUrls: ['./scrape-actions.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ScrapeActionsComponent implements OnInit, OnDestroy {
  @Input({required: true})
  actions: GqlScrapeActionInput[] = [];

  @Input()
  pickFragment: boolean = false;

  @Output()
  actionsChanged: EventEmitter<GqlScrapeActionInput[]> = new EventEmitter<GqlScrapeActionInput[]>();

  @Output()
  pickElement: EventEmitter<(xpath: string) => void> = new EventEmitter<(xpath: string) => void>();

  @Output()
  highlightXpath: EventEmitter<string> = new EventEmitter<string>();

  @Output()
  highlightBoundingBox: EventEmitter<BoundingBox> = new EventEmitter<BoundingBox>();

  @Output()
  pickPosition: EventEmitter<(position: XyPosition) => void> =
    new EventEmitter<(position: XyPosition) => void>();

  @Output()
  pickBoundingBox: EventEmitter<(boundingBox: BoundingBox) => void> =
    new EventEmitter<(boundingBox: BoundingBox) => void>();

  actionsFg = new FormGroup({
    actions: new FormArray<ActionFormGroup>([]),
  });

  fragmentFg = new FormGroup({
    fragmentType: new FormControl<FragmentType>('element', {nonNullable: true, validators: [Validators.required]}),
    boundingBox: new FormGroup({
      x: new FormControl<number>(0, {nonNullable: false, validators: [Validators.required]}),
      y: new FormControl<number>(0, {nonNullable: false, validators: [Validators.required]}),
      h: new FormControl<number>(0, {nonNullable: false, validators: [Validators.required, Validators.min(10)]}),
      w: new FormControl<number>(0, {nonNullable: false, validators: [Validators.required, Validators.min(10)]})
    }),
    xpath: new FormControl<string>('', {nonNullable: false, validators: [Validators.required, Validators.minLength(2)]})
  })

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
      label: 'XPath',
      default: true
    },
    {
      key: 'boundingBox',
      label: 'Bounding Box'
    }
  ];

  scrapeActionOptions: KeyLabelOption<ScrapeAction>[] = [
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
  private subscriptions: Subscription[] = [];

  constructor(private readonly changeRef: ChangeDetectorRef) {}

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  ngOnInit(): void {
    this.actions.map((action) => this.addAction(action));
    this.subscriptions.push(
      this.actionsFg.valueChanges.subscribe(() => {
        this.changeRef.detectChanges();
      }),
      this.fragmentFg.valueChanges.subscribe(() => {
        this.changeRef.detectChanges();
      }),
      this.fragmentFg.controls.fragmentType.valueChanges.subscribe(value => {

      })
    );

    this.changeRef.detectChanges();
  }

  private emitActions(): void {
    console.log('valid', this.actionsFg.valid);
    if (this.actionsFg.valid) {
      this.actionsChanged.emit(this.toScrapeActions());
    }
  }

  deleteAction(actionFg: ActionFormGroup) {
    this.actionsFg.controls.actions.controls = without(
      this.actionsFg.controls.actions.controls,
      actionFg,
    );
    this.emitActions();
  }

  addAction(action?: GqlScrapeActionInput) {
    const type =
      Object.keys(action || {}).find((attr) => isDefined(action[attr])) ||
      ('click' as any);
    const fg: ActionFormGroup = new FormGroup({
      type: new FormControl<ScrapeAction>(type, {
        nonNullable: true,
        validators: [Validators.required],
      }),
      oneOf: new FormGroup({
        header: new FormGroup({
          text: stringField(action?.header?.value),
          name: stringField(action?.header?.name),
        }),
        select: new FormGroup({
          text: stringField(action?.select?.selectValue),
          selector: xpathSelector(action?.select?.element?.value),
        }),
        click: new FormGroup({
          type: new FormControl<ClickType>(
            isDefined(action?.click?.position) ? 'position' : 'element',
            {
              nonNullable: true,
              validators: [Validators.required],
            },
          ),
          oneOf: new FormGroup({
            position: new FormGroup({
              x: new FormControl<number>(action?.click?.position?.x || 0, {
                nonNullable: true,
                validators: [Validators.required, Validators.min(1)],
              }),
              y: new FormControl<number>(action?.click?.position?.y || 0, {
                nonNullable: true,
                validators: [Validators.required, Validators.min(1)],
              }),
            }),
            selector: xpathSelector(
              action?.click?.element?.xpath?.value ||
                action?.click?.element?.name?.value,
            ),
          }),
        }),
        wait: new FormGroup({
          selector: xpathSelector(
            action?.wait?.element?.xpath?.value ||
              action?.wait?.element?.name?.value,
          ),
        }),
        cookie: new FormGroup({
          text: stringField(action?.cookie?.value),
        }),
        type: new FormGroup({
          text: stringField(action?.type?.typeValue),
          selector: xpathSelector(action?.type?.element?.value),
        }),
      }),
    });

    this.actionsFg.controls.actions.controls.push(fg);

    fg.valueChanges.subscribe(() => {
      this.emitActions();
    });
    this.emitActions();
    this.changeRef.detectChanges();
  }

  triggerPickElement(sink: FormControl<string>) {
    if (sink.value) {
      this.highlightXpath.emit(sink.value)
    }
    this.pickElement.emit((xpath) => sink.setValue(xpath));
  }

  triggerPickPosition(
    sink: FormGroup<{ x: FormControl<number>; y: FormControl<number> }>,
  ) {
    this.pickPosition.emit((position) => {
      sink.controls.x.setValue(position.x);
      sink.controls.y.setValue(position.y);
      this.changeRef.detectChanges();
    });
  }

  triggerPickBoundingBox(
    sink: FormGroup<{ x: FormControl<number>; y: FormControl<number>, w: FormControl<number>; h: FormControl<number> }>,
  ) {
    this.pickBoundingBox.emit((box) => {
      sink.controls.x.setValue(box.x);
      sink.controls.y.setValue(box.y);
      sink.controls.w.setValue(box.w);
      sink.controls.h.setValue(box.h);
      this.changeRef.detectChanges();
    });
  }

  formatPosition(
    position: FormGroup<{ x: FormControl<number>; y: FormControl<number> }>,
  ) {
    if (position.value.x || position.value.y) {
      return `(${position.value.x}, ${position.value.y})`;
    } else {
      return '';
    }
  }

  private toScrapeActions(): GqlScrapeActionInput[] {
    return this.actionsFg.controls.actions.controls.map<GqlScrapeActionInput>(
      (control) => {
        const type = control.value.type;
        switch (type) {
          case 'click':
            const clickType = control.value.oneOf.click.type;
            if (clickType == 'element') {
              return {
                click: {
                  element: {
                    xpath: {
                      value: control.value.oneOf.click.oneOf.selector,
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
                    x: control.value.oneOf.click.oneOf.position.x,
                    y: control.value.oneOf.click.oneOf.position.y,
                  },
                },
              };
            } else {
              throw new Error('unsupported');
            }
          case 'cookie':
            return {
              cookie: {
                value: control.value.oneOf.cookie.text,
              },
            };
          case 'header':
            return {
              header: {
                value: control.value.oneOf.header.text,
                name: control.value.oneOf.header.name,
              },
            };
          case 'wait':
            return {
              wait: {
                element: {
                  xpath: {
                    value: control.value.oneOf.wait.selector,
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
                  value: control.value.oneOf.select.selector,
                },
                selectValue: control.value.oneOf.select.text,
              },
            };

          case 'type':
            return {
              type: {
                element: {
                  value: control.value.oneOf.type.selector,
                },
                typeValue: control.value.oneOf.type.text,
              },
            };
        }
      },
    );
  }

  getColorForControl(control: FormControl | FormGroup) {
    if (control.valid) {
      return 'light';
    } else {
      return 'success';
    }
  }

  labelForXpath(xpathFG: FormControl<string>): string {
    if (xpathFG.valid) {
      return xpathFG.value.substring(0, 25)+ '...';
    } else {
      return 'Choose Element'
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

  triggerHighlightBoundingBox(boundingBox: BoundingBoxFG) {
    this.highlightBoundingBox.emit({
      x: boundingBox.x,
      y: boundingBox.y,
      w: boundingBox.w,
      h: boundingBox.h
    })
  }
}
