import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { dateFormat } from '../../services/session.service';
import { Subscription } from 'rxjs';
import { FeatureService } from '../../services/feature.service';
import { Feature } from '../../graphql/types';
import { FormControl } from '@angular/forms';

type FeatureWithFormControl = Feature & { fc: FormControl };

@Component({
  selector: 'app-settings-page',
  templateUrl: './settings.page.html',
  styleUrls: ['./settings.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SettingsPage implements OnInit, OnDestroy {
  loading = true;
  private subscriptions: Subscription[] = [];
  protected readonly dateFormat = dateFormat;
  protected features: FeatureWithFormControl[];

  constructor(
    private readonly featureService: FeatureService,
    private readonly changeRef: ChangeDetectorRef,
  ) {
  }

  async ngOnInit() {
    this.subscriptions.push(
      this.featureService.findAll().subscribe((features) => {
        this.features = features.map((feature) => {
          let fc: FormControl;
          if (feature.value.boolVal) {
            fc = new FormControl<boolean>(feature.value.boolVal.value);
            fc.valueChanges.subscribe((value) => {
              this.featureService.updateFeature({
                name: feature.name,
                value: {
                  boolVal: {
                    value,
                  },
                },
              });
            });
          } else {
            if (feature.value.numVal) {
              fc = new FormControl<number>(feature.value.numVal.value);
              fc.valueChanges.subscribe((value) => {
                this.featureService.updateFeature({
                  name: feature.name,
                  value: {
                    numVal: {
                      value,
                    },
                  },
                });
              });
            } else {
              throw Error(`Unsupported feature ${feature}`);
            }
          }

          return { ...feature, fc };
        });

        this.loading = false;
        this.changeRef.detectChanges();
      }),
    );
    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
