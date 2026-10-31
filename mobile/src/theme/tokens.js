// Design tokens: the single source of truth for colors, spacing, radii, and type.
// Plain CommonJS so tailwind.config.js can require it. TS code imports the typed
// re-exports from '@/theme'. Dark first: the app has no light theme yet.

const colors = /** @type {const} */ ({
  // Dashboard (everyday app chrome)
  bg: '#0B0B0F',
  surface: '#16161D',
  'surface-raised': '#1F1F29',
  border: '#2A2A36',
  'text-primary': '#F5F5F7',
  'text-secondary': '#A1A1AA',
  'text-muted': '#6B6B76',
  accent: '#FF5A36',
  'accent-muted': '#3A1A12',
  success: '#34D399',
  warning: '#FBBF24',
  danger: '#F87171',

  // Wrapped experience (bolder palette, used only inside the story player)
  'wrapped-ink': '#0A0A0A',
  'wrapped-paper': '#FFF8F0',
  'wrapped-volt': '#C6F432',
  'wrapped-magenta': '#FF2E93',
  'wrapped-violet': '#5B21B6',
  'wrapped-sky': '#0EA5E9',
});

// Gradients are color stop lists for a gradient view (added with the Wrapped cards, Phase 4).
const gradients = /** @type {const} */ ({
  sunrise: ['#FF5A36', '#FF2E93'],
  night: ['#5B21B6', '#0EA5E9'],
  pulse: ['#C6F432', '#10B981'],
});

// Semantic spacing on top of Tailwind's default 4pt scale.
const spacing = /** @type {const} */ ({
  gutter: 20, // horizontal screen padding
  card: 16, // padding inside cards
});

const radii = /** @type {const} */ ({
  tile: 12,
  card: 16,
  pill: 9999,
});

// [fontSize, lineHeight] in points. Dashboard scale, then the Wrapped display scale.
const typography = /** @type {const} */ ({
  caption: [12, 16],
  body: [15, 22],
  label: [13, 18],
  title: [20, 26],
  headline: [28, 34],
  stat: [32, 38],
  display: [56, 60],
  'display-xl': [80, 84],
});

module.exports = { colors, gradients, spacing, radii, typography };
