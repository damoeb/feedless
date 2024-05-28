import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { dateFormat } from '../../services/session.service';
import { debounce, interval, Subscription } from 'rxjs';
import { FeatureService } from '../../services/feature.service';
import { Feature } from '../../graphql/types';
import { FormControl } from '@angular/forms';
import { ToastController } from '@ionic/angular';
import { sortBy } from 'lodash-es';

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
    private readonly toastCtrl: ToastController,
    private readonly changeRef: ChangeDetectorRef,
  ) {
  }

  async ngOnInit() {
    this.subscriptions.push(
      this.featureService.findAll().subscribe((features) => {
        this.features = sortBy(features.map((feature) => {
          let fc: FormControl;
          if (feature.value.boolVal) {
            fc = new FormControl<boolean>(feature.value.boolVal.value);
            fc.valueChanges
              .pipe(debounce(() => interval(800)))
              .subscribe(async (value) => {
                await this.featureService.updateFeature({
                  name: feature.name,
                  value: {
                    boolVal: {
                      value,
                    },
                  },
                });
                this.showSavedToast();
              });
          } else {
            if (feature.value.numVal) {
              fc = new FormControl<number>(feature.value.numVal.value);
              fc.valueChanges
                .pipe(debounce(() => interval(800)))
                .subscribe(async (value) => {
                  await this.featureService.updateFeature({
                    name: feature.name,
                    value: {
                      numVal: {
                        value,
                      },
                    },
                  });
                });
              this.showSavedToast();
            } else {
              throw Error(`Unsupported feature ${feature}`);
            }
          }

          return { ...feature, fc };
        }), 'name');

        this.loading = false;
        this.changeRef.detectChanges();
      }),
    );
    this.changeRef.detectChanges();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  private async showSavedToast() {
    const toast = await this.toastCtrl.create({
      message: 'Saved',
      duration: 3000,
      color: 'success',
    });

    await toast.present();
  }
}
