import { AfterViewInit, Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { IonPopover, IonSearchbar, PopoverController } from '@ionic/angular';
import { isArray, isString } from 'lodash-es';
import { AppMenuOption, MenuComponent } from '../menu/menu.component';

export type AppSelectOption = AppMenuOption

@Component({
  selector: 'app-select',
  templateUrl: './select.component.html',
  styleUrls: ['./select.component.scss'],
})
export class SelectComponent {

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

  @Input()
  value: any = 'title';

  @ViewChild('menu')
  menuElement: MenuComponent

  constructor() {
  }


  getLabel() {
    if (this.displayLabel) {
      return this.label
    } else {
      if (this.value && this.menuElement) {
        return this.menuElement.options.find(o => o.value == this.value)?.label || this.placeholder
      } else {
        return this.placeholder
      }
    }
  }
}
