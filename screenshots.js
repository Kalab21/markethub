const { execSync, spawn } = require('child_process');
const http = require('http');
const https = require('https');
const path = require('path');
const fs = require('fs');

const CHROME = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const BASE = 'http://localhost:8081';
const OUT = path.join(__dirname, 'screenshots');
const W = 1280, H = 900;

if (!fs.existsSync(OUT)) fs.mkdirSync(OUT);

function screenshot(url, file) {
  const outFile = path.join(OUT, file);
  console.log(`Capturing ${file}...`);
  execSync(`"${CHROME}" --headless=new --disable-gpu --window-size=${W},${H} --screenshot="${outFile}" "${url}"`, { stdio: 'pipe' });
  console.log(`  Saved: ${outFile}`);
}

function post(options, data) {
  return new Promise((resolve, reject) => {
    const req = http.request(options, res => {
      let body = '';
      res.on('data', d => body += d);
      res.on('end', () => resolve({ status: res.statusCode, headers: res.headers, body }));
    });
    req.on('error', reject);
    if (data) req.write(data);
    req.end();
  });
}

function get(url, cookies) {
  return new Promise((resolve, reject) => {
    const opts = new URL(url);
    const req = http.request({ hostname: opts.hostname, port: opts.port, path: opts.pathname + opts.search, headers: cookies ? { Cookie: cookies } : {} }, res => {
      let body = '';
      res.on('data', d => body += d);
      res.on('end', () => resolve({ status: res.statusCode, headers: res.headers, body }));
    });
    req.on('error', reject);
    req.end();
  });
}

function extractCsrf(html) {
  const m = html.match(/name="_csrf"[^>]*value="([^"]+)"/);
  return m ? m[1] : null;
}

function cookieHeader(setCookieArr) {
  if (!setCookieArr) return '';
  return setCookieArr.map(c => c.split(';')[0]).join('; ');
}

async function run() {
  console.log('\n=== Taking screenshots ===\n');

  // Public pages — no login needed
  screenshot(`${BASE}/onlinemarket/public/login`, 'login.png');
  screenshot(`${BASE}/onlinemarket/public/signup`, 'signup.png');

  // Register a test user
  console.log('\nRegistering test user...');
  const loginPage = await get(`${BASE}/onlinemarket/public/login`);
  let cookies = cookieHeader(loginPage.headers['set-cookie']);
  let csrf = extractCsrf(loginPage.body);

  const signupPage = await get(`${BASE}/onlinemarket/public/signup`, cookies);
  const signupCookies = cookieHeader(signupPage.headers['set-cookie']) || cookies;
  const signupCsrf = extractCsrf(signupPage.body) || csrf;

  const signupData = `firstName=Test&lastName=Buyer&userName=testbuyer&email=testbuyer@test.com&password=password123&role=BUYER&_csrf=${encodeURIComponent(signupCsrf)}`;
  const signupRes = await post({
    hostname: 'localhost', port: 8081,
    path: '/onlinemarket/public/signup', method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Content-Length': Buffer.byteLength(signupData), 'Cookie': signupCookies }
  }, signupData);

  if (signupRes.status === 302 || signupRes.status === 200) {
    console.log('  User registered.');
  } else {
    console.log(`  Signup response: ${signupRes.status}`);
  }

  // Login
  console.log('Logging in...');
  const freshLogin = await get(`${BASE}/onlinemarket/public/login`);
  const loginCookies = cookieHeader(freshLogin.headers['set-cookie']);
  const loginCsrf = extractCsrf(freshLogin.body);

  const loginData = `username=testbuyer&password=password123&_csrf=${encodeURIComponent(loginCsrf)}`;
  const loginRes = await post({
    hostname: 'localhost', port: 8081,
    path: '/onlinemarket/public/login', method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Content-Length': Buffer.byteLength(loginData), 'Cookie': loginCookies }
  }, loginData);

  const sessionCookie = cookieHeader(loginRes.headers['set-cookie']) || loginCookies;
  console.log(`  Login status: ${loginRes.status}`);

  // Write a temp HTML file with meta-refresh + cookie injection for Chrome
  const pages = [
    { file: 'products.png',         path: '/onlinemarket/secured/services/products/list' },
    { file: 'cart.png',              path: '/onlinemarket/secured/services/buyer/shoppingCart' },
    { file: 'seller-dashboard.png',  path: '/onlinemarket/secured/services/products/my-products/1' },
    { file: 'admin.png',             path: '/onlinemarket/secured/services/users/sellers' },
  ];

  // Save session cookie to a temp file Chrome can load
  const cookieSplit = sessionCookie.split('; ');
  const jsessionid = cookieSplit.find(c => c.startsWith('JSESSIONID'));

  for (const p of pages) {
    const tmpHtml = path.join(OUT, '_tmp.html');
    fs.writeFileSync(tmpHtml, `<!DOCTYPE html><html><head>
<script>
document.cookie = "${sessionCookie}; path=/";
window.location = "${BASE}${p.path}";
</script></head><body></body></html>`);
    screenshot(`file:///${tmpHtml}`, p.file);
  }

  fs.unlinkSync(path.join(OUT, '_tmp.html'));
  console.log('\nDone! All screenshots saved to /screenshots/');
  console.log('Commit with: git add screenshots/ && git commit -m "Add UI screenshots" && git push');
}

run().catch(e => { console.error('Error:', e.message); process.exit(1); });
