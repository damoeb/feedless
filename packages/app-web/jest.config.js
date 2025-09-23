module.exports = {
  preset: "jest-preset-angular",
  setupFilesAfterEnv: ["<rootDir>/src/setup-jest.ts"],
  testEnvironment: "jsdom",
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
  // Prevent worker crashes
  maxWorkers: 2,
  workerIdleMemoryLimit: "512MB",
  // Handle unhandled promise rejections
  testTimeout: 30000,
  // Detect open handles
  detectOpenHandles: true,
  forceExit: true,
};
