import { inject, Injectable, PLATFORM_ID } from '@angular/core';
import { ToastController } from '@ionic/angular/standalone';
import { GraphQLFormattedError } from 'graphql/error';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root',
})
export class HttpErrorInterceptorService {
  private readonly toastCtrl = inject(ToastController);
  private readonly platformId = inject(PLATFORM_ID);

  interceptNetworkError(
    networkError:
      | Error
      | (Error & { response: Response; statusCode: number; bodyText: string })
      | (Error & {
          response: Response;
          result: Record<string, any>;
          statusCode: number;
        }),
  ): void {
    const message = `[Network error]: ${networkError}`;
    console.error(message);
    this.promptToast(message);
  }

  interceptGraphQLErrors(graphQLErrors: ReadonlyArray<GraphQLFormattedError>) {
    const messages = graphQLErrors.map(({ message, locations, path }) => {
      const msg = `[GraphQL error]: Message: ${message}, Location: ${locations}, Path: ${path}`;
      console.error(msg);
      return msg;
    });
    this.promptToast(messages.join('\n'));
  }

  private promptToast(message: string) {
    if (isPlatformBrowser(this.platformId)) {
      this.toastCtrl
        .create({
          message,
          color: 'danger',
          duration: 5000,
          buttons: [
            {
              icon: 'close-outline',
              side: 'end',
              role: 'cancel',
            },
          ],
        })
        .then((toast) => toast.present());
    } else {
      console.error(message);
    }
  }
}
