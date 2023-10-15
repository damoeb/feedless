import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { GqlScrapeAction, GqlScrapeActionInput, GqlXyPosition } from '../../../generated/graphql';
import { without } from 'lodash-es';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { isDefined } from '../wizard/wizard-handler';
import { KeyLabelOption } from '../select/select.component';
import { BoundingBox } from '../embedded-website/embedded-website.component';

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


@Component({
  selector: 'app-scrape-actions',
  templateUrl: './scrape-actions.component.html',
  styleUrls: ['./scrape-actions.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ScrapeActionsComponent implements OnInit {
  @Input({required: true})
  actions: GqlScrapeActionInput[] = [];

  @Input()
  pickFragment: boolean = false;

  @Output()
  actionsChanged: EventEmitter<GqlScrapeActionInput[]> = new EventEmitter<GqlScrapeActionInput[]>();

  @Output()
  pickElement: EventEmitter<(xpath: string) => void> = new EventEmitter<(xpath: string) => void>();

  @Output()
  pickPosition: EventEmitter<(position: GqlXyPosition) => void> =
    new EventEmitter<(position: GqlXyPosition) => void>();

  @Output()
  pickBoundingBox: EventEmitter<(boundingBox: BoundingBox) => void> =
    new EventEmitter<(boundingBox: BoundingBox) => void>();

  actionsFg = new FormGroup({
    actions: new FormArray<ActionFormGroup>([]),
  });

  fragmentFg = new FormGroup({
    fragmentType: new FormControl<FragmentType>('element'),
    boundingBox: new FormGroup({
      x: new FormControl<number>(0),
      y: new FormControl<number>(0),
      h: new FormControl<number>(0),
      w: new FormControl<number>(0)
    }),
    xpath: new FormControl<string>('')
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
      label: 'Element',
      default: true
    },
    {
      key: 'boundingBox',
      label: 'Area'
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

  constructor(private readonly changeRef: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.actions.map((action) => this.addAction(action));
    this.actionsFg.valueChanges.subscribe(() => {
      this.changeRef.detectChanges();
    });
    this.fragmentFg.valueChanges.subscribe(() => {
      this.changeRef.detectChanges();
    });

    this.changeRef.detectChanges();
  }

  private emitActions(): void {
    console.log('valid', this.actionsFg.valid);
    this.actionsChanged.emit(this.toScrapeActions());
    // console.log('emit', JSON.stringify(this.toScrapeActions(), null, 2));
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
  }

  triggerPickElement(sink: FormControl<string>) {
    this.pickElement.emit((xpath) => sink.setValue(xpath));
  }

  triggerPickPosition(
    sink: FormGroup<{ x: FormControl<number>; y: FormControl<number> }>,
  ) {
    this.pickPosition.emit((position) => {
      sink.controls.x.setValue(position.x);
      sink.controls.y.setValue(position.y);
    });
  }

  triggerPickBoundingBox(
    sink: FormGroup<{ x: FormControl<number>; y: FormControl<number>, w: FormControl<number>; h: FormControl<number> }>,
  ) {
    this.pickPosition.emit((position) => {
      sink.controls.x.setValue(position.x);
      sink.controls.y.setValue(position.y);
    });
  }

  formatPosition(
    position: FormGroup<{ x: FormControl<number>; y: FormControl<number> }>,
  ) {
    if (position.value.x || position.value.y) {
      return `(${position.value.x}, ${position.value.y})`;
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
      return 'danger';
    }
  }

  formatBoundingBox(value: FormGroup<{
    w: FormControl<number>;
    x: FormControl<number>;
    h: FormControl<number>;
    y: FormControl<number>
  }>): string {
    return `[1,2,3,4]`
  }
}
