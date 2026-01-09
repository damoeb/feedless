const nxPreset = require('@nx/jest/preset').default;

module.exports = {
  ...nxPreset,
  moduleNameMapper: {
    ...nxPreset.moduleNameMapper,
    '@stencil/core/internal/client': '<rootDir>/../../__mocks__/stencil-client.mock.js',
  },
};
