import { Directive, ElementRef, Input, OnChanges, OnDestroy } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { TranslationKeys } from '../../types';
import { Subscription } from 'rxjs';

@Directive({
  selector: '[appLocalizeAttr]',
})
export class LocalizeAttrDirective implements OnChanges, OnDestroy {

  @Input('appLocalizeAttr') attr!: string;
  @Input({ alias: 'appLocalizeKey', required: true }) key!: TranslationKeys;

  private subscriptions: Subscription;


  constructor(private readonly element: ElementRef,
              private readonly translateService: TranslateService) {
    this.subscriptions = translateService.onLangChange.subscribe(() => {
      this.updateText();
    })
  }

  ngOnChanges() {
    this.updateText();
  }

  private updateText() {
    this.translateService.get(this.key).subscribe(value => {
      this.element.nativeElement.setAttribute(this.attr, value);
    })
  }

  ngOnDestroy(): void {
    this.subscriptions?.unsubscribe()
  }
}
