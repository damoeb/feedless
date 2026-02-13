// Mock for @stencil/core/internal/client
// This prevents Jest from loading the actual Stencil runtime

module.exports = {
  Build: {
    isBrowser: typeof window !== 'undefined',
    isDev: true,
  },
  createEvent: () => ({ emit: () => {} }),
  Host: {},
  HTMLElement:
    typeof globalThis !== 'undefined' && globalThis.HTMLElement
      ? globalThis.HTMLElement
      : class HTMLElement {},
  proxyCustomElement: (C) => C,
  h: () => null,
  getElement: () => null,
  forceUpdate: () => {},
  getRenderingRef: () => null,
  registerInstance: () => {},
  getConnect: () => null,
  getContext: () => null,
  getHostRef: () => null,
  plt: {},
  supportsShadow: false,
  win: {},
  doc: {},
  EMPTY_OBJ: {},
  consoleDevError: () => {},
  consoleDevWarn: () => {},
  consoleDevInfo: () => {},
  consoleError: () => {},
  getAssetPath: () => '',
  render: () => null,
  setAssetPath: () => {},
  setNonce: () => {},
  setPlatformOptions: () => {},
};

