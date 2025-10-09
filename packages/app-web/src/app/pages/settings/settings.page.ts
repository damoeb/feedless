import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnInit,
} from '@angular/core';
import { dateFormat } from '../../services/session.service';
import { debounce, interval } from 'rxjs';
import { FeatureService } from '../../services/feature.service';
import { Feature, FeatureGroup } from '../../graphql/types';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  IonCheckbox,
  IonContent,
  IonInput,
  IonItem,
  IonLabel,
  IonList,
  IonSelect,
  IonSelectOption,
  IonSpinner,
  ToastController,
} from '@ionic/angular/standalone';
import { sortBy } from 'lodash-es';
import { AppConfigService } from '../../services/app-config.service';

type FeatureWithFormControl = Feature & { fc: FormControl };

@Component({
  selector: 'app-settings-page',
  templateUrl: './settings.page.html',
  styleUrls: ['./settings.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonContent,
    IonList,
    IonItem,
    IonSpinner,
    IonSelect,
    FormsModule,
    ReactiveFormsModule,
    IonSelectOption,
    IonLabel,
    IonInput,
    IonCheckbox,
  ],
  standalone: true,
})
export class SettingsPage implements OnInit {
  private readonly featureService = inject(FeatureService);
  private readonly toastCtrl = inject(ToastController);
  private readonly appConfig = inject(AppConfigService);
  private readonly changeRef = inject(ChangeDetectorRef);

  loading = true;
  featureGroupsFc = new FormControl<FeatureGroup>(null);
  protected readonly dateFormat = dateFormat;
  protected features: FeatureWithFormControl[] = [];
  protected featureGroups: FeatureGroup[];

  async ngOnInit() {
    this.appConfig.setPageTitle('Settings');
    this.featureService.findAll({}, false).then((featureGroups) => {
      this.featureGroups = featureGroups;
      this.featureGroupsFc.valueChanges.subscribe((featureGroup) => {
        this.features = sortBy(
          featureGroup.features.map((feature) => {
            let fc: FormControl;
            if (feature.value.boolVal) {
              fc = new FormControl<boolean>(feature.value.boolVal.value);
              fc.valueChanges.pipe(debounce(() => interval(800))).subscribe(async (value) => {
                await this.featureService.updateFeatureValue({
                  id: feature.value.id,
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
                fc.valueChanges.pipe(debounce(() => interval(800))).subscribe(async (value) => {
                  await this.featureService.updateFeatureValue({
                    id: feature.value.id,
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
          }),
          'name'
        );
        this.changeRef.detectChanges();
      });
      this.featureGroupsFc.setValue(featureGroups[0]);

      this.loading = false;
      this.changeRef.detectChanges();
    });
    this.changeRef.detectChanges();
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
