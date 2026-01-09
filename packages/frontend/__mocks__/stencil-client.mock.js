// Mock for @stencil/core/internal/client
// This prevents Jest from loading the actual Stencil runtime

module.exports = {
  Build: { isBrowser: false, isDev: true },
  Host: {},
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
};

