import { Injectable } from '@angular/core';
import {
  GqlFeatureName,
  GqlProductName,
  GqlServerSettingsQuery,
  GqlServerSettingsQueryVariables,
  ServerSettings
} from '../../generated/graphql';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { ApolloClient, HttpLink, InMemoryCache } from '@apollo/client/core';
import { AlertController } from '@ionic/angular';
import { Feature } from '../graphql/types';
import { environment } from '../../environments/environment';
import { AlertButton } from '@ionic/core/dist/types/components/alert/alert-interface';

type FeedlessProductConfig = {
  hostname: string;
  product: GqlProductName;
}

export type FeedlessAppConfig = {
  apiUrl: string;
  forceProduct?: GqlProductName;
  products: FeedlessProductConfig[]
};

type ToastOptions = {
  header: string;
  message: string;
  cssClass?: string;
  buttons?: AlertButton[];
};

@Injectable({
  providedIn: 'root',
})
export class ServerSettingsService {
  apiUrl: string; // todo merge api and gateway
  gatewayUrl: string;
  appUrl: string;
  private features: Array<Feature>;

  constructor(
    private readonly httpClient: HttpClient,
    private readonly alertCtrl: AlertController,
  ) {}

  async fetchConfig(): Promise<GqlProductName> {
    const config = await firstValueFrom(
      this.httpClient.get<FeedlessAppConfig>('/config.json'),
    );
    this.apiUrl = config.apiUrl;

    // const product= environment.production ? config.products.find(p => p.hostname === location.hostname)?.product : config.forceProduct
    const product= config.forceProduct ?? config.products.find(p => p.hostname === location.hostname)?.product;

    if (product) {
      console.log(`forcing product ${product}`);
      const products = Object.keys(GqlProductName).map(
        (p) => GqlProductName[p],
      );
      console.log(`Know products ${products.join(', ')}`);
      if (!products.some((otherProduct) => otherProduct == product)) {
        const message = `Product '${product}' does not exist. Know products are ${products.join(
          ', ',
        )}`;
        await this.showToast({
          header: 'Invalid Config',
          message,
          cssClass: 'fatal-alert',
        });
      }
    } else {
      await this.showToast({
        header: 'Invalid Config',
        message: `Cannot map hostname ${location.hostname} to product`,
        cssClass: 'fatal-alert',
      });
    }
    return product;
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
      this.gatewayUrl = response.gatewayUrl;
      this.appUrl = response.appUrl;

    } catch (e) {
      if (!environment.offlineSupport) {
        await this.showToast({
          header: 'Server is not reachable',
          message: 'Either you are offline or the server is down.',
          cssClass: 'fatal-alert',
          buttons: [
            {
              text: 'Ok',
              role: 'confirm',
              handler: () => {
                location.reload();
              },
            },
          ],
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

  isEnabled(featureName: GqlFeatureName): boolean {
    const feature = this.features.find((ft) => ft.name === featureName);
    if (feature) {
      return feature.value.boolVal.value;
    }
    console.warn(`Feature ${featureName} not listed`);
    return false;
  }

  private async showToast({
    header,
    message,
    cssClass,
    buttons,
  }: ToastOptions) {
    const alert = await this.alertCtrl.create({
      header,
      backdropDismiss: false,
      message,
      cssClass,
      buttons,
    });

    await alert.present();
  }
}
