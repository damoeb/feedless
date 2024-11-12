import { Injectable } from '@angular/core';
import {
  GqlFeatureName,
  GqlVertical,
  GqlProfileName,
  GqlServerSettingsQuery,
  GqlServerSettingsQueryVariables,
  ServerSettings,
} from '../../generated/graphql';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { ApolloClient, HttpLink, InMemoryCache } from '@apollo/client/core';
import { AlertController } from '@ionic/angular/standalone';
import { Feature, LocalizedLicense } from '../graphql/types';
import { environment } from '../../environments/environment';
import { AlertButton } from '@ionic/core/dist/types/components/alert/alert-interface';
import { VerticalAppConfig } from '../types';

type ToastOptions = {
  header: string;
  message: string;
  subHeader?: string | undefined;
  cssClass?: string;
  buttons?: AlertButton[];
};

@Injectable({
  providedIn: 'root',
})
export class ServerConfigService {
  apiUrl!: string;
  private features!: Feature[];
  private profiles!: GqlProfileName[];
  private buildFrom!: number;
  private version!: string;
  private license: LocalizedLicense;

  constructor(
    private readonly httpClient: HttpClient,
    private readonly alertCtrl: AlertController,
  ) {}

  async fetchConfig(): Promise<VerticalAppConfig> {
    const config = await firstValueFrom(
      this.httpClient.get<VerticalAppConfig>('/config.json'),
    );

    this.apiUrl = config.apiUrl;

    const product = config.product;

    if (product) {
      console.log(`enabling product ${product}`);
      const products: GqlVertical[] = Object.keys(GqlVertical).map(
        // @ts-ignore
        (p) => GqlVertical[p],
      );
      console.log(`Know products ${products.join(', ')}`);
      if (!products.some((otherProduct) => otherProduct == product)) {
        const message = `Product '${product}' does not exist. Know products are ${products.join(
          ', ',
        )}`;
        await this.showToast({
          header: 'Invalid Config',
          cssClass: 'fatal-alert medium-alert',
          message,
        });
      }
    } else {
      await this.showToast({
        header: 'Invalid Config',
        cssClass: 'fatal-alert medium-alert',
        message: `Cannot map hostname ${location.hostname} to product`,
      });
    }

    return config;
  }

  async fetchServerSettings(): Promise<void> {
    try {
      const response = await this.createApolloClient()
        .query<GqlServerSettingsQuery, GqlServerSettingsQueryVariables>({
          query: ServerSettings,
          variables: {
            data: {
              host: location.host,
              product: environment.product,
            },
          },
        })
        .then((response) => response.data.serverSettings);
      this.features = response.features;
      this.profiles = response.profiles;
      this.version = response.version;
      this.buildFrom = response.build.date;
      this.license = response.license;
    } catch (e) {
      if (!environment.offlineSupport) {
        await this.showToast({
          header: 'Temporary Connection Issue',
          message: `Our servers are currently unavailable, and we're working to restore access as quickly as possible. Please try again in a few minutes. Thank you for your patience!`,
        });
        throw e;
      }
    }
  }

  createApolloClient(): ApolloClient<any> {
    return new ApolloClient<any>({
      link: new HttpLink({ uri: `${this.apiUrl}/graphql` }),
      cache: new InMemoryCache(),
    });
  }

  private async showToast({
    header,
    message,
    subHeader,
    cssClass,
    buttons,
  }: ToastOptions) {
    const alert = await this.alertCtrl.create({
      header,
      backdropDismiss: false,
      message,
      subHeader,
      cssClass,
      buttons,
    });

    await alert.present();
  }

  hasProfile(profile: GqlProfileName) {
    return this.profiles?.indexOf(profile) > -1;
  }

  isSelfHosted() {
    return this.hasProfile(GqlProfileName.SelfHosted);
  }

  isSaas() {
    return !this.isSelfHosted();
  }

  getBuildFrom() {
    return this.buildFrom;
  }

  getVersion() {
    return this.version;
  }

  getFeatureValueInt(featureName: GqlFeatureName): number | undefined {
    const feature = this.getFeature(featureName);
    if (feature) {
      return feature.value.numVal!.value;
    }
  }

  getFeatureValueBool(featureName: GqlFeatureName): boolean {
    const feature = this.getFeature(featureName);
    if (feature) {
      return feature.value.boolVal!.value;
    }
    console.warn(`Feature ${featureName} not listed`);
    return false;
  }

  private getFeature(featureName: GqlFeatureName): Feature {
    return this.features.find((ft) => ft.name === featureName)!;
  }

  getLicense(): LocalizedLicense {
    return this.license;
  }

  setLicense(license: LocalizedLicense) {
    this.license = license;
  }
}
