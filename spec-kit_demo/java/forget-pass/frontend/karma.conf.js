// Try to set CHROME_BIN to Puppeteer's Chromium so Karma can run headless in CI
try {
  const puppeteer = require('puppeteer');
  process.env.CHROME_BIN = puppeteer.executablePath();
} catch (e) {
  // ignore — fallback to system Chrome if Puppeteer not available
}

module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('@angular-devkit/build-angular/plugins/karma'),
      require('karma-coverage')
    ],
    client: {
      clearContext: false
    },
    reporters: ['progress', 'coverage'],
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: false,
    // Use Puppeteer's Chromium for headless runs in CI / container environments
    browsers: ['ChromeHeadlessNoSandbox'],
    customLaunchers: {
      ChromeHeadlessNoSandbox: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox', '--disable-setuid-sandbox']
      }
    },
    beforeMiddleware: [],
    // set CHROME_BIN to Puppeteer's executable at runtime
    beforeLaunch: function () {
      try {
        process.env.CHROME_BIN = require('puppeteer').executablePath();
      } catch (e) {
        // ignore if puppeteer not installed — Karma will fallback to system Chrome
      }
    },
    singleRun: true,
    restartOnFileChange: false
    ,
    coverageReporter: {
      dir: require('path').join(__dirname, 'coverage'),
      reporters: [
        { type: 'lcov', subdir: '.' },
        { type: 'cobertura', subdir: '.', file: 'cobertura-coverage.xml' }
      ]
    }
  });
};
