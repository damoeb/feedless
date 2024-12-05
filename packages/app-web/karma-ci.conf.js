module.exports = function (config) {
  config.set({
    basePath: process.cwd(),
    frameworks: ["jasmine", "@angular-devkit/build-angular"],
    plugins: [
      require("karma-jasmine"),
      require("karma-chrome-launcher"),
      require("@angular-devkit/build-angular/plugins/karma"),
      require("karma-mocha-reporter"),
    ],
    client: {
      jasmine: {
        // you can add configuration options for Jasmine here
        // the possible options are listed at https://jasmine.github.io/api/edge/Configuration.html
        // for example, you can disable the random execution with `random: false`
        // or set a specific seed with `seed: 4321`
      },
      clearContext: false, // leave Jasmine Spec Runner output visible in browser
    },
    customLaunchers: {
      ChromiumHeadlessCI: {
        base: "ChromiumHeadless",
        flags: [
          "--disable-translate",
          "--disable-extensions",
          "--remote-debugging-port=9223",
          "--no-sandbox",
          "--headless",
          "--disable-gpu",
        ],
      },
    },
    reporters: ["mocha"],
    port: 9876, // karma web server port
    colors: true,
    logLevel: config.LOG_INFO,
    browsers: ["ChromiumHeadlessCI"],
    captureTimeout: 60000,
    browserNoActivityTimeout: 60000,
    autoWatch: false,
    singleRun: true,
    watch: false,
    concurrency: Infinity,
  });
};
