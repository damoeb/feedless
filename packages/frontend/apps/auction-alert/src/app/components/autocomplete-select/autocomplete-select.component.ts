import { isPlatformBrowser } from '@angular/common';
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  computed,
  effect,
  inject,
  input,
  signal,
  PLATFORM_ID,
} from '@angular/core';
import { FormControl } from '@angular/forms';
import {
  IonContent,
  IonHeader,
  IonInput,
  IonItem,
  IonLabel,
  IonList,
  IonPopover,
  IonSearchbar,
  PopoverController,
} from '@ionic/angular/standalone';

@Component({
  selector: 'app-autocomplete-select',
  templateUrl: './autocomplete-select.component.html',
  styleUrls: ['./autocomplete-select.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonContent,
    IonHeader,
    IonInput,
    IonItem,
    IonLabel,
    IonList,
    IonPopover,
    IonSearchbar,
  ],
  standalone: true,
})
export class AutocompleteSelectComponent {
  private readonly popoverCtrl = inject(PopoverController);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly host = inject(ElementRef<HTMLElement>);
  private readonly platformId = inject(PLATFORM_ID);

  readonly options = input.required<readonly string[]>();
  readonly control = input.required<FormControl<string | string[]>>();
  /** When true, value is toggled per option; otherwise a single string is set. */
  readonly multiple = input(false);
  readonly inputId = input.required<string>();
  readonly placeholder = input('');
  readonly ariaLabel = input('');
  readonly searchPlaceholder = input('Suchen…');

  readonly filterQuery = signal('');
  /** Matches host width for the popover overlay. */
  readonly popoverWidthPx = signal<number | null>(null);

  readonly filteredOptions = computed(() => {
    const q = this.filterQuery().toLowerCase().trim();
    const opts = this.options();
    if (!q) {
      return [...opts];
    }
    return opts.filter((o) => o.toLowerCase().includes(q));
  });

  constructor() {
    effect((onCleanup) => {
      const c = this.control();
      const sub = c.valueChanges.subscribe(() => this.cdr.markForCheck());
      onCleanup(() => sub.unsubscribe());
    });
  }

  displayString(): string {
    const v = this.control().value;
    if (this.multiple()) {
      const arr = Array.isArray(v) ? v : [];
      return arr.length ? arr.join(', ') : '';
    }
    return typeof v === 'string' ? v : '';
  }

  onPopoverWillPresent(): void {
    this.filterQuery.set('');
  }

  onSearchInput(ev: CustomEvent<{ value?: string | null }>): void {
    this.filterQuery.set(String(ev.detail?.value ?? ''));
  }

  isSelected(option: string): boolean {
    const c = this.control();
    const v = c.value;
    if (this.multiple()) {
      const arr = Array.isArray(v) ? v : [];
      return arr.includes(option);
    }
    return v === option;
  }

  onPick(option: string): void {
    if (this.multiple()) {
      const ctrl = this.control() as FormControl<string[]>;
      const cur = [...(ctrl.value ?? [])];
      const idx = cur.indexOf(option);
      if (idx >= 0) {
        cur.splice(idx, 1);
      } else {
        cur.push(option);
      }
      ctrl.setValue(cur);
      ctrl.markAsDirty();
      ctrl.markAsTouched();
      return;
    }
    const ctrl = this.control() as FormControl<string>;
    ctrl.setValue(option);
    ctrl.markAsDirty();
    ctrl.markAsTouched();
    void this.popoverCtrl.dismiss();
  }

  openPopover(ev: Event, popover: IonPopover): void {
    if (isPlatformBrowser(this.platformId)) {
      const w = this.host.nativeElement.getBoundingClientRect().width;
      this.popoverWidthPx.set(Number.isFinite(w) && w > 0 ? w : null);
    }
    void popover.present(ev as MouseEvent);
  }

  onPopoverDidDismiss(): void {
    this.popoverWidthPx.set(null);
  }
}
