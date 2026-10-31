import type { ReactNode } from 'react';
import { ScrollView, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { cn } from '@/lib/cn';

type ScreenProps = {
  children: ReactNode;
  /** Wrap content in a ScrollView. Use a FlatList screen instead for long/paginated lists. */
  scroll?: boolean;
  className?: string;
};

/** Root container for every screen: dark background, top safe area, gutter padding. */
export function Screen({ children, scroll = false, className }: ScreenProps) {
  const insets = useSafeAreaInsets();
  const paddingTop = insets.top + 16;

  if (scroll) {
    return (
      <ScrollView
        className="flex-1 bg-bg"
        contentContainerClassName={cn('px-gutter pb-8', className)}
        contentContainerStyle={{ paddingTop }}
      >
        {children}
      </ScrollView>
    );
  }

  return (
    <View className={cn('flex-1 bg-bg px-gutter', className)} style={{ paddingTop }}>
      {children}
    </View>
  );
}
