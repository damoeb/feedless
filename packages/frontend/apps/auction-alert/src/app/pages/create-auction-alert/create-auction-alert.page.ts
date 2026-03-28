import { isPlatformBrowser } from '@angular/common';
import { Component, inject, OnInit, PLATFORM_ID, signal } from '@angular/core';
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
import { ApolloClient } from '@apollo/client/core';

import { AppConfigService, PageService, PageTags } from '@feedless/components';

import { AutocompleteSelectComponent } from '../../components/autocomplete-select/autocomplete-select.component';
import dayjs from 'dayjs';
import {
  CreateAuctionAlertCheckout,
  GqlCreateAuctionAlertCheckoutMutation,
  GqlCreateAuctionAlertCheckoutMutationVariables,
  GqlFeedlessPlugins,
  GqlIntervalUnit,
  GqlSegmentInput,
} from '@feedless/graphql-api';

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

@Component({
  selector: 'app-create-auction-alert-page',
  templateUrl: './create-auction-alert.page.html',
  styleUrls: ['./create-auction-alert.page.scss'],
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
  private readonly apollo = inject<ApolloClient<any>>(ApolloClient);
  private readonly pageService = inject(PageService);
  private readonly toastCtrl = inject(ToastController);
  private readonly alertCtrl = inject(AlertController);
  private readonly appConfigService = inject(AppConfigService);
  private readonly platformId = inject(PLATFORM_ID);

  readonly wizardStep = signal<1 | 2>(1);
  readonly checkoutLoading = signal(false);

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
      acceptTerms: new FormControl<boolean>(false, { nonNullable: true }),
    },
    { validators: [this.auctionCategoryValidator()] },
  );

  ngOnInit() {
    this.pageService.setMetaTags(this.getPageTags());
  }

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

  private auctionCategoryValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const fg = control as FormGroup;
      if (fg.untouched) {
        return null;
      }
      const type = fg.get('objectType')?.value as ObjectType;
      if (type === this.objectTypeRealEstate) {
        const v = fg.get('liegenschaftKategorie')?.value;
        if (!v || !String(v).trim()) {
          return { liegenschaftKategorieRequired: true };
        }
      }
      if (type === this.objectTypeOther) {
        const arr = fg.get('beweglichKategorien')?.value as string[];
        if (!arr?.length) {
          return { beweglichKategorienRequired: true };
        }
      }
      return null;
    };
  }

  goToStep2(): void {
    this.form.markAllAsTouched();
    this.form.updateValueAndValidity();
    if (!this.form.valid) {
      console.log(this.form.errors);
      void this.toastCtrl
        .create({
          message:
            'Bitte E-Mail, Bundesland und passende Kategorie(n) ausfüllen.',
          color: 'danger',
          duration: 3000,
        })
        .then((t) => t.present());
      return;
    }
    this.wizardStep.set(2);
  }

  goToStep1(): void {
    this.wizardStep.set(1);
  }

  async startCheckout(): Promise<void> {
    if (!this.form.value.acceptTerms) {
      const toast = await this.toastCtrl.create({
        message: 'Bitte AGB und Datenschutz bestätigen.',
        color: 'danger',
        duration: 3000,
      });
      await toast.present();
      return;
    }

    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    this.checkoutLoading.set(true);
    try {
      const res = await this.apollo.mutate<
        GqlCreateAuctionAlertCheckoutMutation,
        GqlCreateAuctionAlertCheckoutMutationVariables
      >({
        mutation: CreateAuctionAlertCheckout,
        variables: {
          repositoryId: this.getRepositoryId(),
          segmentation: this.buildSegmentation(),
        },
      });

      const data = res.data?.createAuctionAlertCheckout;
      if (!data) {
        throw new Error('Keine Antwort vom Server.');
      }
      if (data.loginRequired) {
        const toast = await this.toastCtrl.create({
          message:
            data.errorMessage ??
            'Für diese E-Mail existiert bereits ein Konto. Bitte melden Sie sich an.',
          color: 'warning',
          duration: 6000,
        });
        await toast.present();
        return;
      }
      if (data.errorMessage) {
        const toast = await this.toastCtrl.create({
          message: data.errorMessage,
          color: 'danger',
          duration: 5000,
        });
        await toast.present();
        return;
      }
      if (data.checkoutUrl) {
        window.location.href = data.checkoutUrl;
        return;
      }
      const toast = await this.toastCtrl.create({
        message: 'Checkout konnte nicht gestartet werden.',
        color: 'danger',
        duration: 4000,
      });
      await toast.present();
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : 'Checkout fehlgeschlagen.';
      const toast = await this.toastCtrl.create({
        message: msg,
        color: 'danger',
        duration: 5000,
      });
      await toast.present();
    } finally {
      this.checkoutLoading.set(false);
    }
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

  private buildSegmentation(): GqlSegmentInput {
    return {
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
    };
  }
}
