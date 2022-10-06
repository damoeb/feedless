process.env.CHROME_BIN =
  process.env.CHROME_BIN || require("puppeteer").executablePath();
const config = require("./protractor.conf").config;

config.capabilities.chromeOptions = {
  args: [
    "--headless",
    "--window-size=3840,2160",
    "--no-sandbox",
    "--disable-setuid-sandbox",
  ],
  binary: process.env.CHROME_BIN,
};

exports.config = config;
