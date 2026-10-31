import { Text as RNText, type TextProps as RNTextProps } from 'react-native';

import { cn } from '@/lib/cn';

export type TextVariant = 'caption' | 'label' | 'body' | 'title' | 'headline' | 'stat';
export type TextTone = 'primary' | 'secondary' | 'muted' | 'accent' | 'danger';

const variantClass: Record<TextVariant, string> = {
  caption: 'text-caption',
  label: 'text-label font-medium',
  body: 'text-body',
  title: 'text-title font-semibold',
  headline: 'text-headline font-bold',
  stat: 'text-stat font-bold',
};

const toneClass: Record<TextTone, string> = {
  primary: 'text-text-primary',
  secondary: 'text-text-secondary',
  muted: 'text-text-muted',
  accent: 'text-accent',
  danger: 'text-danger',
};

// Large type still scales with the OS setting, but capped so stat rows don't explode the layout.
const maxScale: Partial<Record<TextVariant, number>> = { headline: 1.4, stat: 1.3 };

export type TextProps = RNTextProps & {
  variant?: TextVariant;
  tone?: TextTone;
  className?: string;
};

export function Text({ variant = 'body', tone = 'primary', className, style, ...rest }: TextProps) {
  const isHeading = variant === 'headline' || variant === 'title';
  return (
    <RNText
      accessibilityRole={isHeading ? 'header' : undefined}
      maxFontSizeMultiplier={maxScale[variant]}
      className={cn(variantClass[variant], toneClass[tone], className)}
      style={[variant === 'stat' && { fontVariant: ['tabular-nums'] }, style]}
      {...rest}
    />
  );
}
