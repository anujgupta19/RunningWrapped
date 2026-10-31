import { View } from 'react-native';

import { Card, Screen, StatTile, Text } from '@/components/ui';

/**
 * Home dashboard. For now this shows the zero-activity state: no data API exists yet.
 * The real dashboard (week-over-week, weekly distance chart, W/M/Y toggle) is roadmap step 2.11.
 */
export function HomeScreen() {
  return (
    <Screen scroll>
      <Text variant="caption" tone="muted" className="uppercase tracking-widest">
        This week
      </Text>
      <Text variant="headline" className="mt-1">
        Running Wrapped
      </Text>

      <View className="mt-6 flex-row gap-3">
        <StatTile label="Distance" value={null} />
        <StatTile label="Runs" value={null} />
      </View>
      <View className="mt-3 flex-row gap-3">
        <StatTile label="Time" value={null} />
        <StatTile label="Avg pace" value={null} />
      </View>

      <Card className="mt-6">
        <Text variant="title">No runs yet</Text>
        <Text tone="secondary" className="mt-2">
          Import a FIT, GPX, or TCX file from your watch or running app to see your stats here.
        </Text>
      </Card>
    </Screen>
  );
}
