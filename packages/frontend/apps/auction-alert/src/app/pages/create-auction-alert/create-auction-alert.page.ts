import { isPlatformBrowser } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  PLATFORM_ID,
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
  AlertController,
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

import {
  AppConfigService,
  PageService,
  PageTags,
  ReportService,
} from '@feedless/components';

import { AutocompleteSelectComponent } from '../../components/autocomplete-select/autocomplete-select.component';
import dayjs from 'dayjs';
import { GqlFeedlessPlugins, GqlIntervalUnit } from '@feedless/graphql-api';

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
    const type = fg.get('objectType')?.value as ObjectType;
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
  private readonly reportService = inject(ReportService);
  private readonly pageService = inject(PageService);
  private readonly toastCtrl = inject(ToastController);
  private readonly alertCtrl = inject(AlertController);
  private readonly appConfigService = inject(AppConfigService);
  private readonly platformId = inject(PLATFORM_ID);

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

  readonly objectTypeRealEstate: ObjectType = 'liegenschaften';
  readonly objectTypeOther: ObjectType = 'beweglich';

  readonly form = new FormGroup(
    {
      name: new FormControl<string>('', [Validators.required]),
      email: new FormControl<string>('', [
        Validators.required,
        Validators.email,
      ]),
      zip: new FormControl<number | undefined>(undefined),
      country: new FormControl<string[]>([], {
        nonNullable: true,
        validators: [nonEmptyArrayValidator()],
      }),
      objectType: new FormControl<ObjectType>(this.objectTypeRealEstate, {
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

  ngOnInit() {
    this.pageService.setMetaTags(this.getPageTags());
  }

  /** From vertical app config; shown in Kontakt / Impressum sections. */
  get operatorName(): string {
    return this.stringFromCustomProperty('operatorName');
  }

  get operatorAddress(): string {
    return this.stringFromCustomProperty('operatorAddress');
  }

  get operatorEmail(): string {
    return this.stringFromCustomProperty('operatorEmail');
  }

  private stringFromCustomProperty(key: string): string {
    const v = this.appConfigService.customProperties?.[key];
    return typeof v === 'string' ? v.trim() : '';
  }

  async showDatenschutz(): Promise<void> {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    const alert = await this.alertCtrl.create({
      header: 'Datenschutzerklärung',
      cssClass: 'datenschutz-legal-alert',
      message: this.datenschutzAlertMessage(),
      backdropDismiss: true,
      buttons: [
        {
          text: 'Schließen',
          role: 'cancel',
        },
      ],
    });
    await alert.present();
  }

  private datenschutzAlertMessage(): string {
    return [
      'Hinweis: Dieser Text ersetzt keine anwaltliche Beratung. Bitte ergänzen Sie ihn nach Ihrem Anwendungsfall.',
      '',
      'Verantwortliche Stelle',
      'Die im Impressum dieser Seite genannte Stelle ist für die Datenverarbeitung verantwortlich.',
      '',
      'Zwecke und Rechtsgrundlagen',
      'Wir verarbeiten die von Ihnen im Formular angegebenen Daten (z. B. Name, E-Mail, Suchkriterien), um E-Mail-Benachrichtigungen zu gerichtlichen Versteigerungen zu versenden, die Ihren Angaben entsprechen. Rechtsgrundlage ist die Durchführung vorvertraglicher Maßnahmen bzw. die Erfüllung des Nutzungsangebots (Art. 6 Abs. 1 lit. b DSGVO) sowie ggf. Ihre Einwilligung (Art. 6 Abs. 1 lit. a DSGVO), soweit Sie diese abgegeben haben.',
      '',
      'Speicherdauer',
      'Daten werden nur so lange gespeichert, wie es für den jeweiligen Zweck erforderlich ist oder gesetzliche Aufbewahrungsfristen bestehen.',
      '',
      'Ihre Rechte',
      'Sie haben nach Maßgabe der DSGVO Rechte auf Auskunft, Berichtigung, Löschung, Einschränkung der Verarbeitung, Datenübertragbarkeit und Widerspruch. Außerdem haben Sie das Recht, sich bei einer Aufsichtsbehörde zu beschweren.',
      '',
      'Kontakt bei Fragen zum Datenschutz',
      'Wenden Sie sich bitte an die im Impressum angegebene E-Mail-Adresse.',
    ].join('\n');
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

    this.createReporter();
  }

  private getPageTags(): PageTags {
    return {
      title: 'Edikte Alerts',
      description:
        'E-Mail-Benachrichtigungen zu neuen gerichtlichen Versteigerungen in Österreich, passend zu Bundesland, Kategorien und Suchkriterien.',
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

  private getRepositoryId(): string {
    return this.appConfigService.customProperties[
      'auctionsRepositoryId'
    ] as any;
  }

  private async createReporter() {
    await this.reportService.createReport(this.getRepositoryId(), {
      what: {
        tags: {},
      },
      when: {
        scheduled: {
          interval: GqlIntervalUnit.Week,
          startingAt: dayjs().day(0).toDate().getTime(),
        },
      },
      report: {
        plugin: {
          pluginId: GqlFeedlessPlugins.OrgFeedlessEventReport,
          params: {},
        },
      },
      recipient: {
        email: {
          email: this.form.value.email,
          name: this.form.value.name,
        },
      },
    });
  }
}
