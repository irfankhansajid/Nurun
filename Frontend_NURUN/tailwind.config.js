/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'brand-orange': '#F55F1D', // Gorama Logo Color
        'brand-yellow': '#FDBF50', // Gorama Filter Color
        'brand-dark': '#0A0A0A',
        'brand-gray': '#F2F2F2',
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
      },
    },
  },
  plugins: [],
}

