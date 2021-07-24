import { HttpClient, HttpHeaders } from '@angular/common/http';
import { HTTP } from '@ionic-native/http/ngx';
import { Injectable } from '@angular/core';
import { Platform } from '@ionic/angular';

@Injectable({
  providedIn: 'root',
})
export class HttpProxy {
  constructor(
    private nativeHttp: HTTP,
    private httpClient: HttpClient,
    private platform: Platform
  ) {}

  public get(url: string): Promise<string> {
    console.log('Downloading', url);
    if (this.platform.is('android')) {
      return this.nativeHttp.get(url, {}, {}).then((response) => response.data);
    } else {
      const headers = new HttpHeaders({
        Accept: 'text/html',
      });

      return this.httpClient
        .get(`/api/proxy?url=${url}`, { headers, responseType: 'text' })
        .toPromise() as Promise<string>;
    }
  }
}

export interface HttpProxyResponse {
  status: number;
  headers: {
    [key: string]: string;
  };
  url: string;
  data?: any;
  error?: string;
}
