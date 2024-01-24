import { Injectable } from '@angular/core';
import { ToastController } from '@ionic/angular';
import { GraphQLError } from 'graphql/error';

@Injectable({
  providedIn: 'root'
})
export class HttpErrorInterceptorService {
  constructor(private readonly toastCtrl: ToastController) {
  }

  interceptNetworkError(
    networkError:
      | Error
      | (Error & { response: Response; statusCode: number; bodyText: string })
      | (Error & {
      response: Response;
      result: Record<string, any>;
      statusCode: number;
    })
  ): void {
    console.error(`[Network error]: ${networkError}`);
  }

  interceptGraphQLErrors(graphQLErrors: ReadonlyArray<GraphQLError>) {
    const messages = graphQLErrors.map(({ message, locations, path }) => {
      const msg = `[GraphQL error]: Message: ${message}, Location: ${locations}, Path: ${path}`;
      console.error(msg);
      return msg;
    });
    this.toastCtrl
      .create({
        message: messages.join('\n'),
        color: 'danger',
        duration: 5000,
        buttons: [
          {
            icon: 'close-outline',
            side: 'end',
            role: 'cancel'
          }
        ]
      })
      .then((toast) => toast.present());
  }
}
