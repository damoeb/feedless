import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
} from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import {
  IonButton,
  IonCheckbox,
  IonContent,
  IonInput,
  IonItem,
  IonLabel,
  IonRadio,
  IonRadioGroup,
  ToastController,
} from '@ionic/angular/standalone';

import { PageService, PageTags } from '@feedless/components';

import { AutocompleteSelectComponent } from '../../app/components/autocomplete-select/autocomplete-select.component';
import dayjs from 'dayjs';

export type ObjectType = 'liegenschaften' | 'beweglich';

function nonEmptyArrayValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const v = control.value;
    if (Array.isArray(v) && v.length > 0) {
      return null;
    }
    return { required: true };
  };
}

function auctionCategoryValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const fg = control as FormGroup;
    const type = fg.get('auctionType')?.value as ObjectType;
    if (type === 'liegenschaften') {
      const v = fg.get('liegenschaftKategorie')?.value;
      if (!v || !String(v).trim()) {
        return { liegenschaftKategorieRequired: true };
      }
    }
    if (type === 'beweglich') {
      const arr = fg.get('beweglichKategorien')?.value as string[];
      if (!arr?.length) {
        return { beweglichKategorienRequired: true };
      }
    }
    return null;
  };
}

@Component({
  selector: 'app-create-auction-alert-page',
  templateUrl: './create-auction-alert.page.html',
  styleUrls: ['./create-auction-alert.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonButton,
    IonCheckbox,
    IonContent,
    IonInput,
    IonItem,
    IonLabel,
    IonRadio,
    IonRadioGroup,
    ReactiveFormsModule,
    AutocompleteSelectComponent,
  ],
  standalone: true,
})
export class CreateAuctionAlertPage implements OnInit {
  private readonly pageService = inject(PageService);
  private readonly toastCtrl = inject(ToastController);

  /** Bundesländer (same labels as Ediktsdatei filters). */
  readonly countries = [
    'Burgenland',
    'Wien',
    'Oberösterreich',
    'Niederösterreich',
    'Kärnten',
    'Salzburg',
    'Tirol',
    'Steiermark',
    'Vorarlberg',
  ] as const;

  /** Kategorien — [Einfache Suche Liegenschaften](https://edikte.justiz.gv.at/edikte/ex/exedi3.nsf/suche!OpenForm&subf=) */
  readonly liegenschaftKategorien = [
    'Einfamilienhaus',
    'Zweifamilienhaus',
    'Mehrfamilienhaus',
    'Mietwohnhaus',
    'Mietshaus',
    'gemischt genutztes Haus',
    'Reihenhaus',
    'Hausanteil',
    'Wohnungseigentumsobjekt',
    'Eigentumswohnung',
    'Maisonette',
    'Dachterrassenwohnung',
    'Dachgeschoßwohnung',
    'Garconniere',
    'Gartenwohnung',
    'unbebaute Liegenschaft',
    'bebaubare Liegenschaft',
    'land- und forstwirtschaftlich genutzte Liegenschaft',
    'gewerbliche Liegenschaft',
    'Superädifikat',
    'Baurecht',
    'Sonstiges',
  ] as const;

  /** Kategorie (ohne Unterkategorie) — [Bewegliche Sachen](https://edikte.justiz.gv.at/edikte/fe/feedi6.nsf/suche!OpenForm&subf=) */
  readonly beweglichKategorien = [
    'Antiquität, Kunst',
    'Audio, HiFi',
    'Auto HiFi',
    'Auto, Motorrad, KFZ, LKW',
    'Bekleidung',
    'Buch, Zeitschrift',
    'Büro',
    'Computer, Zubehör',
    'Computersoftware',
    'Elektrogerät',
    'Fahrrad',
    'Haus, Garten',
    'Lagerbestände',
    'Landmaschine',
    'Maschinen und technische Anlagen',
    'Musikinstrument',
    'Sammlerobjekt',
    'Sonstige Vermögensrechte',
    'Spielzeug, Spiel',
    'Sportartikel',
    'Telekommunikation',
    'Tier',
    'Tonträger',
    'TV, Video, Foto',
    'Uhren, Schmuck',
    'Videospiel',
    'Sonstiges',
  ] as const;

  readonly form = new FormGroup(
    {
      email: new FormControl<string>('', [
        Validators.required,
        Validators.email,
      ]),
      zip: new FormControl<number | undefined>(undefined),
      country: new FormControl<string[]>([], {
        nonNullable: true,
        validators: [nonEmptyArrayValidator()],
      }),
      objectType: new FormControl<ObjectType>('liegenschaften', {
        nonNullable: true,
        validators: [Validators.required],
      }),
      liegenschaftKategorie: new FormControl<string>('', { nonNullable: true }),
      beweglichKategorien: new FormControl<string[]>([], { nonNullable: true }),
      nurInternetVersteigerungen: new FormControl<boolean>(false, {
        nonNullable: true,
      }),
    },
    { validators: [auctionCategoryValidator()] },
  );

  readonly objectTypeRealEstate: ObjectType = 'liegenschaften';
  readonly objectTypeOther: ObjectType = 'beweglich';

  ngOnInit() {
    this.pageService.setMetaTags(this.getPageTags());
  }

  async onSubmit() {
    this.form.markAllAsTouched();
    this.form.updateValueAndValidity();
    if (!this.form.valid) {
      const toast = await this.toastCtrl.create({
        message:
          'Bitte E-Mail, Bundesland und passende Kategorie(n) ausfüllen.',
        color: 'danger',
        duration: 3000,
      });
      await toast.present();
      return;
    }
    const toast = await this.toastCtrl.create({
      message: "You're subscribed! We'll notify you of matching auctions.",
      color: 'success',
      duration: 3000,
    });
    await toast.present();
    this.form.reset({
      email: '',
      country: [],
      objectType: 'liegenschaften',
      liegenschaftKategorie: '',
      beweglichKategorien: [],
      nurInternetVersteigerungen: false,
    });
  }

  private getPageTags(): PageTags {
    return {
      title: 'Edikte Alerts',
      description:
        'Erfahre mehr über lokale.events - die Plattform für lokale Veranstaltungen und Events. Wir bringen Menschen zusammen und machen regionale Schätze sichtbar.',
      publisher: 'feedless',
      category: 'Alerts',
      url: 'https://edikte.feedless.org',
      lang: 'de',
      publishedAt: dayjs(),
      keywords: ['Versteigerungen', 'Email', 'Alerts', 'Notifications'],
      author: 'feedless Team',
      robots: 'index, follow',
      canonicalUrl: 'https://edikte.feedless.org',
    };
  }
}
