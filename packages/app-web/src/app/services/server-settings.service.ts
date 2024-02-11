import { Injectable } from '@angular/core';
import {
  GqlFeatureName,
  GqlProductName,
  GqlServerSettingsQuery,
  GqlServerSettingsQueryVariables,
  ServerSettings,
} from '../../generated/graphql';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { ApolloClient, HttpLink, InMemoryCache } from '@apollo/client/core';
import { AlertController } from '@ionic/angular';
import { Feature } from '../graphql/types';
import { environment } from '../../environments/environment';
import { AlertButton } from '@ionic/core/dist/types/components/alert/alert-interface';
import { ActivatedRoute } from '@angular/router';

export type FeedlessAppConfig = {
  apiUrl: string;
  forceProduct?: GqlProductName;
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
    private readonly activatedRoute: ActivatedRoute,
    private readonly alertCtrl: AlertController,
  ) {}

  async fetchServerSettings(): Promise<void> {
    try {
      const config = await firstValueFrom(
        this.httpClient.get<FeedlessAppConfig>('/config.json'), // todo here?
      );
      this.apiUrl = config.apiUrl;

      if (!environment.production) {
      }
      const devForceProduct = environment.production
        ? null
        : this.activatedRoute.snapshot.queryParams.forceProduct;
      const forceProduct = config.forceProduct || devForceProduct;
      if (forceProduct) {
        const product = forceProduct;
        console.log(`forcing product ${product}`);
        const products = Object.keys(GqlProductName).map(
          (p) => GqlProductName[p],
        );
        console.log(`Know products ${products.join(', ')}`);
        if (!products.some((otherProduct) => otherProduct == product)) {
          const message = `Product '${product}' does not exist. Know products are ${products.join(
            ', ',
          )}`;
          console.error(message);
          await this.showToast({
            header: 'Invalid Config',
            message,
            cssClass: 'fatal-alert',
          });
        }
        environment.product = () => product;
      }
      const response = await this.createApolloClient()
        .query<GqlServerSettingsQuery, GqlServerSettingsQueryVariables>({
          query: ServerSettings,
          variables: {
            data: {
              host: location.host,
              product: environment.product(),
            },
          },
        })
        .then((response) => response.data.serverSettings);
      this.features = response.features;
      this.gatewayUrl = response.gatewayUrl;
      this.appUrl = response.appUrl;
    } catch (e) {
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
