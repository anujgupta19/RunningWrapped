const { colors, spacing, radii, typography } = require('./src/theme/tokens');

const px = (n) => `${n}px`;

/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./app/**/*.{ts,tsx}', './src/**/*.{ts,tsx}'],
  presets: [require('nativewind/preset')],
  theme: {
    extend: {
      colors,
      spacing: Object.fromEntries(Object.entries(spacing).map(([k, v]) => [k, px(v)])),
      borderRadius: Object.fromEntries(Object.entries(radii).map(([k, v]) => [k, px(v)])),
      fontSize: Object.fromEntries(
        Object.entries(typography).map(([k, [size, lineHeight]]) => [
          k,
          [px(size), { lineHeight: px(lineHeight) }],
        ]),
      ),
    },
  },
  plugins: [],
};
