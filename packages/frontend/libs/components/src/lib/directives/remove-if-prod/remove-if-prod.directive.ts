import {
  Directive,
  ElementRef,
  inject,
  isDevMode,
  OnInit,
  PLATFORM_ID,
  Renderer2,
} from '@angular/core';
import { isPlatformServer } from '@angular/common';

@Directive({ selector: '[appDev]', standalone: true })
export class RemoveIfProdDirective implements OnInit {
  private el = inject(ElementRef);
  private renderer = inject(Renderer2);
  private platformId = inject(PLATFORM_ID);

  ngOnInit(): void {
    const isServer = isPlatformServer(this.platformId);
    const isProduction = !isDevMode();

    if (isServer || isProduction) {
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
