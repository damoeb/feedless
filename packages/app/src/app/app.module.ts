import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouteReuseStrategy } from '@angular/router';
import { APOLLO_OPTIONS } from 'apollo-angular';
import { HttpLink } from 'apollo-angular/http';
import { ApolloClientOptions, DefaultOptions, InMemoryCache } from '@apollo/client/core';
import { TextToSpeech } from '@ionic-native/text-to-speech/ngx';
import { HTTP } from '@ionic-native/http/ngx';
import { IonicModule, IonicRouteStrategy } from '@ionic/angular';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { CommonModule } from '@angular/common';
import { HttpClientModule, HttpHeaders } from '@angular/common/http';
import { AuthModule } from './auth/auth.module';
import { BubbleModule } from './components/bubble/bubble.module';
import { BucketCreateModule } from './components/bucket-create/bucket-create.module';

@NgModule({
  declarations: [AppComponent],
  entryComponents: [],
  imports: [
    BrowserModule,
    CommonModule,
    IonicModule.forRoot(),
    AppRoutingModule,
    HttpClientModule,
    AuthModule,
    BubbleModule,
    BucketCreateModule
  ],
  providers: [
    { provide: RouteReuseStrategy, useClass: IonicRouteStrategy },
    HTTP,
    TextToSpeech,
    {
      provide: APOLLO_OPTIONS,
      useFactory: (httpLink: HttpLink) => {
        const defaultOptions: DefaultOptions = {
          watchQuery: {
            fetchPolicy: 'no-cache',
            errorPolicy: 'ignore',
          },
          query: {
            fetchPolicy: 'no-cache',
            errorPolicy: 'all',
          },
        };
        const headers = new HttpHeaders();
        headers.set('Authorization', 'Bearer bar');
        const options: ApolloClientOptions<any> = {
          cache: new InMemoryCache(),
          connectToDevTools: true,
          credentials: 'foo-bar',
          link: httpLink
            .create({
              withCredentials: true,
              uri: '/graphql',
            })
            .setOnError((error) => {
              console.error('gql', error);
            }),
          defaultOptions,
        };
        return options;
      },
      deps: [HttpLink],
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
