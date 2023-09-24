import { Directive, ElementRef, Input, OnInit } from '@angular/core';
import { assign, isArray } from 'lodash-es';
import { ServerSettingsService } from '../../services/server-settings.service';
import { GqlFeatureName } from '../../../generated/graphql';
import { Router } from '@angular/router';

export interface FeatureToggleOptions {
  whenInactive?: 'hide' | 'disable';
  tooltip?: boolean;
}

const defaultFeatureToggleOptions: FeatureToggleOptions = {
  whenInactive: 'disable',
  tooltip: true,
};

@Directive({
  selector: '[appFeatureToggle]',
})
export class FeatureToggleDirective implements OnInit {
  @Input() appFeatureToggle: GqlFeatureName | GqlFeatureName[];
  @Input() appFeatureToggleOptions: FeatureToggleOptions;

  constructor(
    private readonly serverSettings: ServerSettingsService,
    private readonly router: Router,
    private readonly el: ElementRef,
  ) {}

  ngOnInit(): void {
    if (!this.appFeatureToggle) {
      throw new Error('FeatureToggleDirective requires a name');
    }
    if (
      !this.serverSettings.canUseFeatures(
        isArray(this.appFeatureToggle)
          ? this.appFeatureToggle
          : [this.appFeatureToggle],
      )
    ) {
      const effectiveOptions = assign(
        defaultFeatureToggleOptions,
        this.appFeatureToggleOptions,
      );
      if (effectiveOptions.whenInactive === 'hide') {
        this.el.nativeElement.style.display = 'none';
        // } else if (effectiveOptions.whenInactive === 'authenticate') {
        //   this.el.nativeElement.addEventListener('click', (e) => {
        //     e.stopPropagation();
        //     e.preventDefault();
        //     this.router.navigateByUrl('/login');
        //   });
      } else {
        this.el.nativeElement.setAttribute('disabled', true);
      }

      if (effectiveOptions.tooltip) {
        this.el.nativeElement.setAttribute(
          'title',
          `Feature '${this.appFeatureToggle}' is not available in server`,
        );
      }
    }
  }
}
