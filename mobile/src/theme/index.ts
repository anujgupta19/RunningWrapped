import tokens from './tokens';

export const { colors, gradients, spacing, radii, typography } = tokens;

export type ColorToken = keyof typeof colors;
export type GradientToken = keyof typeof gradients;
export type TypographyToken = keyof typeof typography;
