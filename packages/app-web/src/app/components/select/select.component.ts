import { AfterViewInit, Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { IonPopover, IonSearchbar, PopoverController } from '@ionic/angular';
import { isArray, isString } from 'lodash-es';

export interface AppSelectOption {
  value: any,
  label: string,
  hint?: string
}

@Component({
  selector: 'app-select',
  templateUrl: './select.component.html',
  styleUrls: ['./select.component.scss'],
})
export class SelectComponent implements OnInit {

  @Input()
  label: string

  @Input()
  hideFilter: boolean = false

  @Input()
  placeholder: string

  @Input()
  color: string = 'light'

  @Input()
  displayLabel: boolean = false

  @Output()
  valueChanged: EventEmitter<any> = new EventEmitter<any>()

  @Input()
  selectOptions: object | string[] | AppSelectOption[]

  @ViewChild('searchbar')
  searchbarElement: IonSearchbar

  @ViewChild('popover')
  popoverElement: IonPopover

  @Input()
  value: any = 'title';

  options: AppSelectOption[] = [];

  constructor(private readonly popoverController: PopoverController) {
  }

  ngOnInit(): void {
    if (this.selectOptions) {
      if (isArray(this.selectOptions) && this.selectOptions.length > 0) {
        if (isString(this.selectOptions[0])) {
          this.options = this.createOptionsFromStringArray(this.selectOptions as string[])
        } else {
          this.options = this.selectOptions as AppSelectOption[];
        }
      } else {
        this.options = this.createOptionsFromObject(this.selectOptions as object)
      }
    }
  }

  query = '';
  indexInFocus = -1;

  filteredOptions() {
    if (this.query) {
      return this.options.filter(option => {
        return Object.values(option).some(o => o?.indexOf(this.query) > -1)
      })
    } else {
      return this.options;
    }
  }

  pick(option: AppSelectOption) {
    this.value = option.value;
    this.valueChanged.emit(this.value);
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
    if (this.indexInFocus === this.options.length -1) {
      this.indexInFocus = 0;
    } else {
      this.indexInFocus = this.indexInFocus + 1;
    }
  }

  focusPrevious() {
    if (this.indexInFocus <= 0) {
      this.indexInFocus = this.options.length -1;
    } else {
      this.indexInFocus = this.indexInFocus - 1;
    }
  }

  pickInFocus($event: any) {
    if (this.indexInFocus > -1) {
      return this.pick(this.options[this.indexInFocus]);
    }
  }

  focusSearchbar() {
    setTimeout(() => {
      if(this.hideFilter) {
        // this.popoverElement.
      } else {
        this.searchbarElement.setFocus()
      }
    }, 1);
  }

  private createOptionsFromStringArray(options: string[]): AppSelectOption[] {
    return options.map(option => ({
      value: option,
      label: option
    }))
  }

  private createOptionsFromObject(options: object): AppSelectOption[] {
    return Object.keys(options).map(key => ({
      value: key,
      label: options[key]
    }))
  }

  getLabel() {
    if (this.displayLabel) {
      return this.label
    } else {
      if (this.value) {
        return this.options.find(o => o.value === this.value)?.label || this.placeholder
      } else {
        return this.placeholder
      }
    }
  }

  togglePopover(popover: IonPopover, event: MouseEvent) {
    return popover.present(event)
  }

  private dismiss() {
    return this.popoverController.dismiss()
  }

  getColor() {
    if (this.value) {
      return this.color;
    } else {
      return 'danger'
    }
  }
}
