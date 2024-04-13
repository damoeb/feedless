module.exports = function (config) {
  config.set({
    basePath: process.cwd(),
    frameworks: ["jasmine", "@angular-devkit/build-angular"],
    plugins: [
      require("karma-jasmine"),
      require("karma-chrome-launcher"),
      require("karma-jasmine-html-reporter"),
      require("karma-coverage"),
      require("@angular-devkit/build-angular/plugins/karma"),
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
    jasmineHtmlReporter: {
      suppressAll: true, // removes the duplicated traces
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
    coverageReporter: {
      dir: require("path").join(__dirname, "./coverage/ngv"),
      subdir: ".",
      reporters: [{ type: "html" }, { type: "text-summary" }],
    },
    reporters: ["progress"],
    port: 9876, // karma web server port
    colors: true,
    logLevel: config.LOG_INFO,
    browsers: ["ChromiumHeadlessCI"],
    autoWatch: false,
    singleRun: true,
    watch: false,
    concurrency: Infinity,
  });
};
