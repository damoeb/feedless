import { Directive, ElementRef, inject } from '@angular/core';

@Directive({
  // eslint-disable-next-line @angular-eslint/directive-selector
  selector: '[localize-*], [localize]',
})
export class LocalizeDirective {
  constructor() {
    const element = inject(ElementRef);

    if (element.nativeElement) {
      console.log(element.nativeElement.attributes);
      element.nativeElement.replace('hase');
    }
  }
}
