/** @type {import('tailwindcss').Config} */
export default {
  content: ['./src/**/*.{ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        primary: {
          50: 'var(--c-primary-50)',
          100: 'var(--c-primary-100)',
          200: 'var(--c-primary-200, #BFDBFE)',
          500: 'var(--c-primary)',
          600: 'var(--c-primary-600)',
          700: 'var(--c-primary-700)',
        },
        success: {
          50: 'var(--c-success-50, #F0FDF4)',
          500: 'var(--c-success)',
        },
        warning: {
          50: 'var(--c-warning-50, #FFFBEB)',
          500: 'var(--c-warning)',
        },
        danger: {
          50: 'var(--c-danger-50, #FEF2F2)',
          500: 'var(--c-danger)',
        },
        info: {
          50: 'var(--c-info-50, #ECFEFF)',
          500: 'var(--c-info)',
        },
        // 中性色映射到 CSS 变量，让 bg-white / text-gray-900 等随主题切换
        white: 'var(--c-card)',
        gray: {
          50: 'var(--c-bg)',
          100: 'var(--c-code-bg)',
          200: 'var(--c-border)',
          300: 'var(--c-border)',
          400: 'var(--c-text-muted)',
          500: 'var(--c-text-sec)',
          600: 'var(--c-text-sec)',
          700: 'var(--c-text)',
          800: 'var(--c-text)',
          900: 'var(--c-text)',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      boxShadow: {
        card: '0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04)',
        hover: '0 4px 12px rgba(0,0,0,0.08)',
      },
      transitionDuration: { DEFAULT: '150ms' },
    },
  },
  plugins: [],
};
