import { View } from 'react-native';

import { cn } from '@/lib/cn';

import { Text } from './Text';

const EMPTY = '—';

export type StatTileProps = {
  label: string;
  /** Already formatted for display (via src/lib/format). `null` renders an empty placeholder. */
  value: string | null;
  unit?: string;
  caption?: string;
  className?: string;
};

export function StatTile({ label, value, unit, caption, className }: StatTileProps) {
  const spoken =
    value === null
      ? `${label}, no data yet`
      : [label, unit ? `${value} ${unit}` : value, caption].filter(Boolean).join(', ');

  return (
    <View
      accessible
      accessibilityLabel={spoken}
      className={cn('flex-1 rounded-tile bg-surface-raised p-4', className)}
    >
      <Text variant="label" tone="secondary">
        {label}
      </Text>
      <View className="mt-1 flex-row items-baseline">
        <Text variant="stat" tone={value === null ? 'muted' : 'primary'}>
          {value ?? EMPTY}
        </Text>
        {unit && value !== null ? (
          <Text tone="secondary" className="ml-1">
            {unit}
          </Text>
        ) : null}
      </View>
      {caption ? (
        <Text variant="caption" tone="muted" className="mt-1">
          {caption}
        </Text>
      ) : null}
    </View>
  );
}
