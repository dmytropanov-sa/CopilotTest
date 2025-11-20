/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/**/*.{html,ts}',
    './index.html'
  ],
  theme: {
    extend: {
      colors: {
        primary: '#0B5ED7',
        'primary-dark': '#084298',
        accent: '#36C2CE',
        success: '#198754',
        warning: '#FFC107',
        danger: '#DC3545',
        info: '#0DCAF0',
        background: '#F8FAFC',
        surface: '#FFFFFF',
        'focus-outline': '#6BA8FF',
        'password-weak': '#DC3545',
        'password-medium': '#FFC107',
        'password-strong': '#198754'
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'SFMono-Regular', 'monospace']
      }
    }
  },
  plugins: []
};
