const { defineConfig } = require("cypress");

module.exports = defineConfig({
  e2e: {
    // baseUrl: 'localhost:4200',
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
  },
});
