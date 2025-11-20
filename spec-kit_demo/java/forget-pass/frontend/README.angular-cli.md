Angular dev setup & run (Windows cmd.exe)

1) From `frontend` install dependencies:

```cmd
cd /d C:\Users\Dmytro_Panov\workspace\Projects\AIinSDLC\CopilotTest\spec-kit_demo\java\forget-pass\frontend
npm install
```

2) Initialize Tailwind (if not already present):

```cmd
npx tailwindcss init
```

3) Add Tailwind directives to `src/styles.css` if missing:

```css
@tailwind base;
@tailwind components;
@tailwind utilities;
```

4) Run dev server with proxy to backend:

```cmd
npx ng serve --proxy-config proxy.conf.json
```

5) Environment variables (do NOT commit secrets):

Set the reCAPTCHA site key in the browser at runtime before bootstrapping the app (example for local testing):

- Add to `index.html` before main script:

```html
<script>window.__RECAPTCHA_SITE_KEY__ = 'your-site-key-here';</script>
```

Or run a small local script to inject it into `window` during development.

Notes:
- Follow constitution & `.specify` to keep reactive forms, accessibility, and security requirements.
- After `npm install`, run `npm start` or `npx ng serve --proxy-config proxy.conf.json`.
