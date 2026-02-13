// Stencil/Ionic custom elements call __registerHost and __attachShadow in constructor; JSDOM does not provide these
if (typeof HTMLElement !== 'undefined') {
  (
    HTMLElement.prototype as unknown as { __registerHost?: () => void }
  ).__registerHost = function (): void {
    /* JSDOM stub */
  };
  if (
    !Object.prototype.hasOwnProperty.call(
      HTMLElement.prototype,
      '__attachShadow',
    )
  ) {
    (
      HTMLElement.prototype as unknown as { __attachShadow?: () => ShadowRoot }
    ).__attachShadow = function (): ShadowRoot {
      return {
        appendChild: (): void => {
          /* JSDOM stub */
        },
        addEventListener: (): void => {
          /* JSDOM stub */
        },
        removeEventListener: (): void => {
          /* JSDOM stub */
        },
      } as unknown as ShadowRoot;
    };
  }
}

import { setupZoneTestEnv } from 'jest-preset-angular/setup-env/zone';

// Ionic web components use __registerHost (Stencil) which is not available in JSDOM
setupZoneTestEnv({
  errorOnUnknownElements: false,
  errorOnUnknownProperties: false,
});

// matchMedia is not available in JSDOM
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation((query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(),
    removeListener: jest.fn(),
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
});

// Stencil createEvent and Build are used by Ionic components; not available in JSDOM
jest.mock('@stencil/core/internal/client', () => ({
  createEvent: () => ({ emit: jest.fn() }),
  HTMLElement: globalThis.HTMLElement ?? class {},
  h: () => null,
  Host: () => null,
  proxyCustomElement: (Cls: unknown) => Cls,
  Build: { isBrowser: typeof window !== 'undefined' },
  getAssetPath: (path: string) => path,
  forceUpdate: () => {
    /* empty */
  },
  readTask: (fn: () => void) => fn(),
  writeTask: (fn: () => void) => fn(),
  setMode: () => {
    /* empty */
  },
  getMode: () => 'ios',
}));
