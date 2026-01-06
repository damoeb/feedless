import { Directive, ElementRef, inject, OnInit, Renderer2 } from '@angular/core';
import { environment } from '../../../environments/environment';

@Directive({ selector: '[appDev]', standalone: true })
export class RemoveIfProdDirective implements OnInit {
  private el = inject(ElementRef);
  private renderer = inject(Renderer2);

  ngOnInit(): void {
    if (environment.production) {
      // If it's production, remove the element from the DOM
      this.renderer.removeChild(this.el.nativeElement.parentNode, this.el.nativeElement);
    } else {
      // If it's not production, add a red border to the element
      this.renderer.setStyle(this.el.nativeElement, 'border', '2px solid magenta');
    }
  }
}
