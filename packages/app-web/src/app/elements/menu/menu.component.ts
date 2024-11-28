import { Component, OnInit, ViewChild, inject, output, input } from '@angular/core';
import {
  IonPopover,
  IonSearchbar,
  PopoverController,
  IonContent,
  IonHeader,
  IonList,
  IonItem,
  IonLabel,
  IonButton,
} from '@ionic/angular/standalone';
import { isFunction, isObject, isString } from 'lodash-es';
import { NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';

export function labelProvider<T>(
  value: T,
  labelFn: keyof T | ((value: T) => string),
): string {
  if (isFunction(labelFn)) {
    return labelFn(value);
  } else {
    if (isString(labelFn)) {
      if (isObject(value) && Object.keys(value).includes(labelFn)) {
        return `${value[labelFn]}`;
      } else {
        return `${value}`;
      }
    } else {
      return `${value}`;
    }
  }
}

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss'],
  imports: [
    IonPopover,
    IonContent,
    IonHeader,
    IonSearchbar,
    FormsModule,
    IonList,
    IonItem,
    NgClass,
    IonLabel,
    IonButton
],
  standalone: true,
})
export class MenuComponent<T> implements OnInit {
  private readonly popoverController = inject(PopoverController);

  readonly hideFilter = input<boolean>(false);

  readonly placeholder = input<string>();

  readonly error = input<boolean>(false);

  readonly disabled = input<boolean>(false);

  readonly labelFn = input<keyof T | ((value: T) => string)>();

  readonly color = input<string>('light');

  readonly items = input.required<T[]>();

  readonly value = input<T>();

  readonly valueChanged = output<T>();

  @ViewChild('searchbar')
  searchbarElement: IonSearchbar;

  @ViewChild('popover')
  popoverElement: IonPopover;

  currentValue: T;
  query = '';
  indexInFocus = -1;

  ngOnInit(): void {
    this.currentValue = this.value();
  }

  filteredOptions(): T[] {
    if (this.query) {
      return this.items().filter((option) => {
        return JSON.stringify(option).indexOf(this.query) > -1;
      });
    } else {
      return this.items();
    }
  }

  pick(option: T) {
    console.log('pick', option);
    this.currentValue = option;
    this.valueChanged.emit(option);
    return this.dismiss();
  }

  clearOrDismiss(event: any) {
    console.log('clearOrDismiss');
    event.preventDefault();
    event.stopPropagation();
    event.stopImmediatePropagation();

    if (this.query) {
      this.query = '';
    } else {
      return this.dismiss();
    }
  }

  focusNext() {
    if (this.indexInFocus === this.items().length - 1) {
      this.indexInFocus = 0;
    } else {
      this.indexInFocus = this.indexInFocus + 1;
    }
  }

  focusPrevious() {
    if (this.indexInFocus <= 0) {
      this.indexInFocus = this.items().length - 1;
    } else {
      this.indexInFocus = this.indexInFocus - 1;
    }
  }

  pickInFocus() {
    if (this.indexInFocus > -1) {
      return this.pick(this.items()[this.indexInFocus]);
    }
  }

  focusSearchbar() {
    if (!this.hideFilter()) {
      setTimeout(() => {
        this.searchbarElement.setFocus();
      }, 1);
    }
  }

  togglePopover(popover: IonPopover, event: MouseEvent) {
    return popover.present(event);
  }

  label(option: T) {
    return labelProvider<T>(option, this.labelFn());
  }

  private dismiss() {
    return this.popoverController.dismiss();
  }
}
