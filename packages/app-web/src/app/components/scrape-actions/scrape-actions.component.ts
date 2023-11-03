import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component, ElementRef,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';
import { GqlScrapeAction, GqlScrapeActionInput } from '../../../generated/graphql';
import { isNull, without } from 'lodash-es';
import { FormArray, FormControl, FormGroup, ValidationErrors, Validators, ɵValue } from '@angular/forms';
import { isDefined } from '../wizard/wizard-handler';
import { KeyLabelOption } from '../select/select.component';
import { BoundingBox, XyPosition } from '../embedded-image/embedded-image.component';
import { Subscription } from 'rxjs';

const xpathSelector = (initialValue: string = '', validatorsCondition: () => boolean) => {
  return new FormControl(initialValue, {
    nonNullable: true,
    asyncValidators: async (control): Promise<ValidationErrors | null> => {
      if (validatorsCondition()) {
        return {
          ...Validators.required(control),
          ...Validators.minLength(1)(control)
        }
      }
    }
  });
}

const stringField = <V, T extends FormControl<V>>(initialValue: string = '', validatorsCondition: () => boolean) => {
  return new FormControl(initialValue, {
    nonNullable: true,
    asyncValidators: async (control): Promise<ValidationErrors | null> => {
      if (validatorsCondition()) {
        return {
          ...Validators.required(control),
          ...Validators.minLength(1)(control)
        }
      }
    }
  });
}

type ScrapeAction = keyof GqlScrapeAction

type Action = {
  oneOf: {
    wait: { selector: { value: string } };
    select: {
      selector: { value: string };
      text: string;
    };
    cookie: { text: string };
    header: { name: string; text: string };
    type: {
      selector: { value: string };
      text: string;
    };
    click: {
      oneOf: {
        selector: { value: string };
        position: { x: number; y: number };
      };
      type: ClickType;
    };
  };
  type: ScrapeAction;
};

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

  @ViewChild('formElement')
  formRef: ElementRef;

  @Input()
  pickFragment: boolean = false;

  @Output()
  actionsChanged: EventEmitter<GqlScrapeActionInput[]> = new EventEmitter<GqlScrapeActionInput[]>();

  @Output()
  pickElement: EventEmitter<(xpath: string | null) => void> = new EventEmitter<(xpath: string | null) => void>();

  @Output()
  highlightXpath: EventEmitter<string> = new EventEmitter<string>();

  @Output()
  highlightBoundingBox: EventEmitter<BoundingBox> = new EventEmitter<BoundingBox>();

  @Output()
  pickPosition: EventEmitter<(position: XyPosition | null) => void> =
    new EventEmitter<(position: XyPosition | null) => void>();

  @Output()
  pickBoundingBox: EventEmitter<(boundingBox: BoundingBox | null) => void> =
    new EventEmitter<(boundingBox: BoundingBox | null) => void>();

  allActions: Action[] = [];

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
      this.fragmentFg.valueChanges.subscribe(() => {
        this.changeRef.detectChanges();
      }),
      this.fragmentFg.controls.fragmentType.valueChanges.subscribe(value => {

      })
    );

    this.changeRef.detectChanges();
  }

  emitActions(): void {
    const isFormValid = (this.formRef.nativeElement as HTMLFormElement).checkValidity();
    if (isFormValid) {
      this.actionsChanged.emit(this.toScrapeActions());
    }
  }

  deleteAction(action: Action) {
    this.allActions = without(
      this.allActions,
      action,
    );
    this.emitActions();
  }

  addAction(action?: GqlScrapeActionInput) {
    const type =
      Object.keys(action || {}).find((attr) => isDefined(action[attr])) ||
      ('click' as any);
    const fg: Action = {
      type,
      oneOf: {
        header: {
          text: action?.header?.value,
          name: action?.header?.name,
        },
        select: {
          text: action?.select?.selectValue,
          selector: { value: action?.select?.element?.value },
        },
        click: {
          type: isDefined(action?.click?.position) ? 'position' : 'element',
          oneOf: {
            position: {
              x: action?.click?.position?.x || 0,
              y: action?.click?.position?.y || 0,
            },
            selector: {
              value: action?.click?.element?.xpath?.value ||
                action?.click?.element?.name?.value
            },
          },
        },
        wait: {
          selector: { value: action?.wait?.element?.xpath?.value ||
              action?.wait?.element?.name?.value},
        },
        cookie: {
          text: action?.cookie?.value,
        },
        type: {
          text: action?.type?.typeValue,
          selector: {
            value: action?.type?.element?.value
          },
        },
      },
    };

    this.allActions.push(fg);

    this.emitActions();
    this.changeRef.detectChanges();
  }

  triggerPickElement(sink: { value: string }) {
    if (sink.value) {
      this.highlightXpath.emit(sink.value)
    }
    this.pickElement.emit((xpath) => {
      console.log('-> pickElement', xpath);
      if (!isNull(xpath)) {
        sink.value = xpath;
        this.changeRef.detectChanges();
      }
    });
  }

  triggerPickPosition(
    sink: { x: number; y: number }
  ) {
    this.pickPosition.emit((position) => {
      console.log('-> pickPosition', position);
      if (!isNull(position)) {
        sink.x = position.x;
        sink.y = position.y;
        this.changeRef.detectChanges();
      }
    });
  }

  triggerPickBoundingBox(
    sink: FormGroup<{ x: FormControl<number>; y: FormControl<number>, w: FormControl<number>; h: FormControl<number> }>,
  ) {
    this.pickBoundingBox.emit((boundingBox) => {
      console.log('-> pickBoundingBox', boundingBox);
      if (!isNull(boundingBox)) {
        sink.controls.x.setValue(boundingBox.x);
        sink.controls.y.setValue(boundingBox.y);
        sink.controls.w.setValue(boundingBox.w);
        sink.controls.h.setValue(boundingBox.h);
        this.changeRef.detectChanges();
      }
    });
  }

  formatPosition(
    position: { x: number; y: number }
  ) {
    if (position.x || position.y) {
      return `(${position.x}, ${position.y})`;
    } else {
      return '';
    }
  }

  private toScrapeActions(): GqlScrapeActionInput[] {
    return this.allActions.map<GqlScrapeActionInput>(
      (control) => {
        const type = control.type;
        switch (type) {
          case 'click':
            const clickType = control.oneOf.click.type;
            if (clickType == 'element') {
              return {
                click: {
                  element: {
                    xpath: {
                      value: control.oneOf.click.oneOf.selector.value,
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
                value: control.oneOf.cookie.text,
              },
            };
          case 'header':
            return {
              header: {
                value: control.oneOf.header.text,
                name: control.oneOf.header.name,
              },
            };
          case 'wait':
            return {
              wait: {
                element: {
                  xpath: {
                    value: control.oneOf.wait.selector.value,
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
                  value: control.oneOf.select.selector.value,
                },
                selectValue: control.oneOf.select.text,
              },
            };

          case 'type':
            return {
              type: {
                element: {
                  value: control.oneOf.type.selector.value,
                },
                typeValue: control.oneOf.type.text,
              },
            };
        }
      },
    );
  }

  getColorForControl(valid: boolean) {
    if (valid) {
      return 'light';
    } else {
      return 'success';
    }
  }

  labelForXpath(xpath: { value: string }): string {
    if (xpath.value) {
      return xpath.value.substring(0, 25)+ '...';
    } else {
      return 'Choose Element'
    }
  }

  labelForPosition(position: XyPosition): string {
    if (position.x && position.y) {
      return this.formatPosition(position);
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

  triggerHighlightBoundingBox(boundingBox: BoundingBoxFG) {
    this.highlightBoundingBox.emit({
      x: boundingBox.x,
      y: boundingBox.y,
      w: boundingBox.w,
      h: boundingBox.h
    })
  }
}
