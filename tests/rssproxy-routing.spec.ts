import { HttpTest, HttpTestOptions } from './test-utils';

const options: HttpTestOptions = {
  insecure: true,
  local: true
};

// kubectl logs -n traefik -l app.kubernetes.io/name=traefik

function servesUI(baseUrl: string) {
  describe(`ui`, () => {
    describe('serves', () => {
      it('/', async () => {
        const response = await HttpTest.get(baseUrl, options);

        response
          .ok()
          .html();
        // containsBodyFragment(response, 'rss-proxy');
      });
      it('/index.html', async () => {
        const response = await HttpTest.get(`${baseUrl}/index.html`, options);
        response
          .ok()
          .html()
          .containsBodyFragment('RSS-proxy');
      });
      it('/sitemap.xml', async () => {
        const response = await HttpTest.get(`${baseUrl}/sitemap.xml`, options);
        response
          .ok()
          .xml();
      });
      it('/config.json', async () => {
        const response = await HttpTest.get(`${baseUrl}/config.json`, options);
        response
          .ok()
          .json()
          .containsBodyFragment('rssProxy');
      });
      it('/robots.txt', async () => {
        const response = await HttpTest.get(`${baseUrl}/robots.txt`, options);
        response
          .ok()
          .contentTypeIs('text/plain');
      });
    });
  });
}

function redirectsUI(baseUrl: string, redirectUrl: string) {
  describe(`ui`, () => {
    describe('redirects', () => {
      it(`${baseUrl} -> ${redirectUrl}`, async () => {
        const response = await HttpTest.get(baseUrl, options);

        response
          .status(301)
          .location(redirectUrl);
      });
    });
  });
}

function servesAPI(baseUrl: string) {
  describe('api', () => {
    it('serves xsl', async () => {
      const response = await HttpTest.get(`${baseUrl}/feed/static/feed.xsl`, options);
      response
        .ok()
        .contentTypeIs('text/xsl;charset=UTF-8');

    });
    const urls = [`${baseUrl}/api/w2f?v=0.1&url=http%3A%2F%2Flocalhost&link=.%2Fa%5B1%5D&context=%2F%2Fdiv%5B3%5D%2Fdiv%2Fdiv%5B1%5D%2Fsection%5B1%5D%2Farticle&re=none&q=contains(%23any%2C%20%22EM%22)&out=atom`,
      `${baseUrl}/api/tf?url=https%3A%2F%2Flocalhost%2Fnews-atom.xml&re=none&q=not(contains(%23any%2C%20%22Politik%22))&out=atom`,
      `${baseUrl}/api/feed?url=https%3A%2F%2Flocalhost&pContext=%2F%2Fbody%2Fdiv%5B3%5D%2Fdiv%2Fdiv%5B1%5D%2Fsection%5B1%5D%2Farticle&pLink=.%2Fa%5B1%5D`,
      `${baseUrl}/api/feed?url=http%3A%2F%2Flocalhost&pContext=%2F%2Fbody%2Fdiv%5B3%5D%2Fdiv%2Fdiv%5B1%5D%2Fsection%5B1%5D%2Farticle&pLink=.%2Fa%5B1%5D&x=s`];
    for (let index in urls) {
      const apiUrl = urls[index];
      it(apiUrl, async () => {
        const response = await HttpTest.get(apiUrl, options);
        response
          .ok()
          .contentTypeIs('application/xml;charset=utf-8');

      });
    }
  });
}

describe('rssproxy', () => {

  servesAPI('https://rssproxy.local.feedless.org');
  servesUI('https://rssproxy.local.feedless.org');

  servesAPI('https://rssproxy.local.migor.org');
  redirectsUI('https://rssproxy.local.migor.org', 'https://rssproxy.local.feedless.org');

  servesAPI('https://rssproxy-v1.local.migor.org');
  redirectsUI('https://rssproxy-v1.local.migor.org', 'https://rssproxy.local.feedless.org');

});
