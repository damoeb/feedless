import axios, { AxiosResponse } from 'axios';
import https from 'https';

export type HttpTestOptions = {
  insecure: boolean
  local: boolean
};

export class HttpTest {
  private body: any;

  private constructor(private response: AxiosResponse) {
    this.body = response.data;
  }

  static async get(url: string, options: HttpTestOptions) {
    const httpsAgent = new https.Agent({
      rejectUnauthorized: !options.insecure
    });

    const axiosInstance = axios.create({
      httpsAgent,
      maxRedirects: 0,
      validateStatus: (status: number) =>
        status >= 200 && status < 400
    });
    const patchedUrl = this.patchUrl(url, options.local);
    // console.log(`get ${patchedUrl}`);
    return new HttpTest(await axiosInstance.get(patchedUrl));
  }

  status(status: number) {
    expect(this.response.status).toBe(status);
    return this;
  }

  ok() {
    this.status(200);
    return this;
  }

  contentTypeIsHtml() {
    this.contentTypeIs('text/html');
    return this;
  }

  contentTypeIs(contentType: string) {
    expect(this.response.headers['content-type']).toBe(contentType);
    return this;
  }

  hasHtmlBody() {
    this.containsBodyFragment('<html');
    return this;
  }

  hasXmlBody() {
    this.containsBodyFragment('<?xml ');
    return this;
  }

  containsBodyFragment(fragment: string) {
    expect(JSON.stringify(this.body).indexOf(fragment) > -1).toBeTruthy();
    return this;
  }

  html() {
    this.contentTypeIsHtml();
    this.hasHtmlBody();

    return this;
  }

  xml() {
    this.contentTypeIs('text/xml');
    this.hasXmlBody();
    return this;
  }

  json() {
    this.contentTypeIs('application/json');
    return this;
  }

  private static patchUrl(url: string, local: boolean) {
    if (!local) {
      return url.replace('.local', '');
    }
    return url;
  }

  location(url: string) {
    expect(this.response.headers['location']).toBe(url);
    return this;
  }
}
