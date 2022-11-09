import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { catchError, Observable } from 'rxjs';
import { ToastController } from '@ionic/angular';
import { GraphQLError } from 'graphql/error';

@Injectable({
  providedIn: 'root'
})
export class HttpErrorInterceptorService {
  constructor(private readonly toastCtrl: ToastController) {
  }

  interceptNetworkError(networkError: Error | (Error & { response: Response; statusCode: number; bodyText: string }) | (Error & { response: Response; result: Record<string, any>; statusCode: number })): void {
    console.error(`[Network error]: ${networkError}`);
   }

  interceptGraphQLErrors(graphQLErrors: ReadonlyArray<GraphQLError>) {
    graphQLErrors.forEach(({ message, locations, path }) =>
      console.error(
        `[GraphQL error]: Message: ${message}, Location: ${locations}, Path: ${path}`
      )
    );
  }
}
