export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      colors: {
        primary: {
          DEFAULT: '#3B82F6',
          hover: '#2563EB',
          light: '#1e3a5f',
        },
        success: {
          DEFAULT: '#10B981',
          hover: '#059669',
          light: '#0f2e23',
        },
        surface: '#0d1b2e',
        border: '#1e2d44',
        muted: '#64748B',
        navy: {
          900: '#060e1a',
          800: '#0d1b2e',
          700: '#0f2544',
        },
      },
    },
  },
  plugins: [],
};
