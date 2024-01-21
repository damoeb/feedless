import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { IonPopover, IonSearchbar, PopoverController } from '@ionic/angular';
import { isFunction, isObject, isString } from 'lodash-es';

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
})
export class MenuComponent<T> implements OnInit {
  @Input()
  hideFilter: boolean = false;

  @Input()
  placeholder: string;

  @Input()
  error: boolean = false;

  @Input()
  disabled: boolean = false;

  @Input()
  labelFn: keyof T | ((value: T) => string);

  @Input()
  color: string = 'light';

  @Input({ required: true })
  items: T[];

  @Input()
  value: T;

  @Output()
  valueChanged: EventEmitter<T> = new EventEmitter<T>();

  @ViewChild('searchbar')
  searchbarElement: IonSearchbar;

  @ViewChild('popover')
  popoverElement: IonPopover;

  currentValue: T;

  constructor(private readonly popoverController: PopoverController) {}

  ngOnInit(): void {
    this.currentValue = this.value;
  }

  query = '';
  indexInFocus = -1;

  filteredOptions(): T[] {
    if (this.query) {
      return this.items.filter((option) => {
        return JSON.stringify(option).indexOf(this.query) > -1;
      });
    } else {
      return this.items;
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
    if (this.indexInFocus === this.items.length - 1) {
      this.indexInFocus = 0;
    } else {
      this.indexInFocus = this.indexInFocus + 1;
    }
  }

  focusPrevious() {
    if (this.indexInFocus <= 0) {
      this.indexInFocus = this.items.length - 1;
    } else {
      this.indexInFocus = this.indexInFocus - 1;
    }
  }

  pickInFocus($event: T) {
    if (this.indexInFocus > -1) {
      return this.pick(this.items[this.indexInFocus]);
    }
  }

  focusSearchbar() {
    if (!this.hideFilter) {
      setTimeout(() => {
        this.searchbarElement.setFocus();
      }, 1);
    }
  }

  togglePopover(popover: IonPopover, event: MouseEvent) {
    return popover.present(event);
  }

  private dismiss() {
    return this.popoverController.dismiss();
  }

  label(option: T) {
    return labelProvider<T>(option, this.labelFn);
  }
}
