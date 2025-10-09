module.exports = {
  preset: "jest-preset-angular",
  setupFilesAfterEnv: ["<rootDir>/src/setup-jest.ts"],
  testEnvironment: "jsdom",
  setupFiles: ["<rootDir>/src/setup-jest-env.ts"],
  collectCoverage: true,
  coverageDirectory: "coverage",
  coverageReporters: ["html", "text-summary"],
  testPathIgnorePatterns: [
    "<rootDir>/node_modules/",
    "<rootDir>/dist/",
    "<rootDir>/www/",
    "<rootDir>/e2e/",
  ],
  transformIgnorePatterns: [
    "node_modules/(?!(@angular|@ionic|@stencil|ionicons|@justinribeiro|rxjs|lodash-es|d3-.*|uuid|flexsearch|@codemirror|dayjs|dexie|graphql|jwt-decode|leaflet|marked|pixelmatch|platform|schema-dts|tslib|typesafe-routes|zone.js|@apollo|internmap))",
  ],
  moduleNameMapper: {
    "^src/(.*)$": "<rootDir>/src/$1",
    "^@app/(.*)$": "<rootDir>/src/app/$1",
    "ionicons/components/ion-icon.js":
      "<rootDir>/src/test/mocks/ion-icon.mock.js",
  },
  maxWorkers: 1,
  workerIdleMemoryLimit: "1GB",
  testTimeout: 60000,
  detectOpenHandles: false,
  forceExit: true,
  silent: false,
  verbose: false,
  errorOnDeprecated: false,
};
