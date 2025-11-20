try {
  const puppeteer = require('puppeteer');
  console.log(puppeteer.executablePath());
} catch (e) {
  console.error('Puppeteer not available');
  process.exit(1);
}
