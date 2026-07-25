/** @type {import('tailwindcss').Config} */
export default {
  content: ['./src/**/*.{ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#EFF6FF', 100: '#DBEAFE', 200: '#BFDBFE',
          500: '#2563EB', 600: '#1D4ED8', 700: '#1E40AF',
        },
        success: { 50: '#F0FDF4', 500: '#16A34A' },
        warning: { 50: '#FFFBEB', 500: '#D97706' },
        danger: { 50: '#FEF2F2', 500: '#DC2626' },
        info: { 50: '#ECFEFF', 500: '#0891B2' },
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
