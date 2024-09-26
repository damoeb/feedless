import { Directive, ElementRef, Renderer2, OnInit } from '@angular/core';
import { environment } from '../../../environments/environment';

@Directive({
  selector: '[appDev]',
})
export class RemoveIfProdDirective implements OnInit {
  constructor(
    private el: ElementRef,
    private renderer: Renderer2,
  ) {}

  ngOnInit(): void {
    if (environment.production) {
      // If it's production, remove the element from the DOM
      this.renderer.removeChild(
        this.el.nativeElement.parentNode,
        this.el.nativeElement,
      );
    } else {
      // If it's not production, add a red border to the element
      this.renderer.setStyle(
        this.el.nativeElement,
        'border',
        '2px solid magenta',
      );
    }
  }
}
