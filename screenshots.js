const puppeteer = require('puppeteer-core');

const BASE = 'http://localhost:8081';
const ADMIN = { username: 'admin', password: 'password' };
const BUYER = { username: 'buyer', password: 'password' };

async function login(page, user) {
  await page.goto('about:blank');
  await page.goto(`${BASE}/onlinemarket/public/login`, { waitUntil: 'networkidle2' });
  await page.waitForSelector('#username', { visible: true });
  await page.evaluate((u, p) => {
    document.getElementById('username').value = u;
    document.getElementById('password').value = p;
  }, user.username, user.password);
  await Promise.all([
    page.waitForNavigation({ waitUntil: 'load', timeout: 30000 }),
    page.click('#btnSubmit'),
  ]);
  console.log(`  after login URL: ${page.url()}`);
}

async function shot(page, name) {
  await page.screenshot({ path: `screenshots/${name}.png`, fullPage: false });
  console.log(`  saved screenshots/${name}.png`);
}

async function signup(page, first, last, username, email, password, role) {
  await page.goto(`${BASE}/onlinemarket/public/signup`, { waitUntil: 'networkidle2' });
  await page.type('input[name="firstName"]', first);
  await page.type('input[name="lastName"]', last);
  await page.type('input[name="userName"]', username);
  await page.type('input[name="email"]', email);
  await page.type('input[name="password"]', password);
  await page.select('select[name="role"]', role);
  await Promise.all([
    page.waitForNavigation({ waitUntil: 'networkidle2' }),
    page.click('button[type="submit"]'),
  ]);
  console.log(`  signup result URL: ${page.url()}`);
}

(async () => {
  const browser = await puppeteer.launch({
    executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
    args: ['--no-sandbox', '--disable-setuid-sandbox'],
  });
  const page = await browser.newPage();
  await page.setViewport({ width: 1280, height: 800 });

  console.log('Login page...');
  await page.goto(`${BASE}/onlinemarket/public/login`, { waitUntil: 'networkidle2' });
  await shot(page, 'login');

  console.log('Sign up page...');
  await page.goto(`${BASE}/onlinemarket/public/signup`, { waitUntil: 'networkidle2' });
  await shot(page, 'signup');

  console.log('Registering buyer2...');
  await signup(page, 'Jane', 'Buyer', 'buyer2', 'buyer2@market.com', 'password', 'BUYER');
  await shot(page, 'signup-result');

  console.log('Buyer: product list...');
  await login(page, { username: 'buyer2', password: 'password' });
  await page.goto(`${BASE}/onlinemarket/secured/services/products/list`, { waitUntil: 'networkidle2' });
  await shot(page, 'products');

  console.log('Buyer: cart...');
  await page.goto(`${BASE}/onlinemarket/cart/me`, { waitUntil: 'networkidle2' });
  await shot(page, 'cart');

  console.log('Logging out buyer...');
  await page.goto(`${BASE}/onlinemarket/public/logout`, { waitUntil: 'networkidle2' });
  console.log(`  after logout URL: ${page.url()}`);

  console.log('Admin: seller management...');
  await login(page, ADMIN);
  await page.goto(`${BASE}/onlinemarket/secured/services/users/sellers`, { waitUntil: 'networkidle2' });
  await shot(page, 'admin');

  await browser.close();
  console.log('Done.');
})();
