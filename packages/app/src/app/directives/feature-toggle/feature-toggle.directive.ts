import { Directive, ElementRef, Input, OnInit } from '@angular/core';
import { assign } from 'lodash';
import { ServerSettingsService } from '../../services/server-settings.service';
import { GqlFeatureName } from '../../../generated/graphql';

export enum WhenInactiveOption {
  hide,
  disable
}

export interface FeatureToggleOptions {
  whenInactive?: WhenInactiveOption;
  tooltip?: boolean;
}

const defaultFeatureToggleOptions: FeatureToggleOptions = {
  whenInactive: WhenInactiveOption.disable,
  tooltip: true
};

@Directive({
  selector: '[appFeatureToggle]'
})
export class FeatureToggleDirective implements OnInit {

  @Input() appFeatureToggle: GqlFeatureName;
  @Input() appFeatureToggleOptions: FeatureToggleOptions;

  constructor(private readonly serverSettings: ServerSettingsService,
              private readonly el: ElementRef) {
  }

  ngOnInit(): void {
    if (!this.appFeatureToggle) {
      throw new Error('FeatureToggleDirective requires a name');
    }
    if (!this.serverSettings.hasFeature(this.appFeatureToggle)) {
      const effectiveOptions = assign(defaultFeatureToggleOptions, this.appFeatureToggleOptions);
      if (effectiveOptions.whenInactive === WhenInactiveOption.hide) {
        this.el.nativeElement.style.display = 'none';
      } else {
        this.el.nativeElement.setAttribute('disabled', true);
      }

      if (effectiveOptions.tooltip) {
        this.el.nativeElement.setAttribute('title', `Feature '${this.appFeatureToggle}' is not available in server`);
      }
    }
  }

}
