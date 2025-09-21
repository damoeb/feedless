import { Test, TestingModule } from '@nestjs/testing';
import { PuppeteerModule } from './puppeteer.module';
import { PuppeteerService } from './puppeteer.service';
import { ScrapeEmit } from '../../generated/graphql';

describe('PuppeteerService', () => {
  let service: PuppeteerService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      imports: [PuppeteerModule],
    }).compile();

    service = module.get<PuppeteerService>(PuppeteerService);
  });

  function mockNewPage(goto: () => string) {
    return jest.fn().mockImplementation(() => {
      return {
        on: () => {},
        setExtraHTTPHeaders: () => {},
        goto,
        evaluate: () => {},
        viewport: () => {},
        setRequestInterception: () => {},
        cookies: () => Promise.resolve([]),
        screenshot: () =>
          Promise.resolve(Buffer.from('screenshot').toString('base64')),
      };
    });
  }

  it('error will be returned if browser raises an error', async () => {
    // 'ERR_NAME_NOT_RESOLVED',
    //   'ERR_TIMED_OUT',
    //   'ERR_ABORTED',
    //   'ERR_CONNECTION_RESET',
    //   'ERR_TOO_MANY_REDIRECTS',
    //   'ERR_CONNECTION_REFUSED',
    //   'ERR_EMPTY_RESPONSE',
    //   'ERR_TOO_MANY_REDIRECTS',
    //   'ERR_CERT_AUTHORITY_INVALID',
    //   'ERR_CERT_COMMON_NAME_INVALID',
    //   'ERR_CERT_AUTHORITY_INVALID',
    //   'ERR_CERT_DATE_INVALID',
    //   'ERR_CONNECTION_CLOSED',
    //   'ERR_INVALID_RESPONSE',
    //   'ERR_CERT_DATE_INVALID',
    //   'ERR_SSL_PROTOCOL_ERROR',
    //   'ERR_SSL_VERSION_OR_CIPHER_MISMATCH',
    service.newBrowser = jest.fn().mockImplementation(() => {
      return {
        close: () => {},
      };
    });
    service.newPage = mockNewPage(() => {
      throw new Error('ERR_CONNECTION_RESET');
    });

    const response = await service.submit({
      id: '',
      title: '',
      flow: {
        sequence: [
          {
            fetch: {
              get: {
                url: {
                  literal: 'https://foo.bar',
                },
              },
            },
          },
        ],
      },
    });
    expect(response.ok).toBeFalsy();
    expect(response.errorMessage).toEqual('ERR_CONNECTION_RESET');
  });

  xit('extract', async () => {
    service.newBrowser = jest.fn().mockImplementation(() => {
      return {
        close: () => {},
      };
    });
    service.newPage = mockNewPage(() => {
      throw new Error('ERR_CONNECTION_RESET');
    });

    const response = await service.submit({
      id: '',
      title: '',
      flow: {
        sequence: [
          {
            fetch: {
              get: {
                url: {
                  literal: 'https://foo.bar',
                },
              },
            },
          },
          {
            extract: {
              fragmentName: '',
              selectorBased: {
                uniqueBy: ScrapeEmit.Html,
                emit: [],
                fragmentName: '',
                max: 1,
                xpath: {
                  value: '//',
                },
              },
            },
          },
        ],
      },
    });
    expect(response.ok).toBeTruthy();
  });
});
