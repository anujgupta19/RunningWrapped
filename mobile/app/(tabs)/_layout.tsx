import Ionicons from '@expo/vector-icons/Ionicons';
import { Tabs } from 'expo-router';
import type { ComponentProps } from 'react';

import { colors } from '@/theme';

type IconName = ComponentProps<typeof Ionicons>['name'];

const ACTIVE_TINT = colors.accent;
const INACTIVE_TINT = colors['text-muted'];

function tabIcon(name: IconName, focusedName: IconName) {
  return function TabIcon({ focused, size }: { focused: boolean; size: number }) {
    return (
      <Ionicons
        name={focused ? focusedName : name}
        color={focused ? ACTIVE_TINT : INACTIVE_TINT}
        size={size}
      />
    );
  };
}

export default function TabsLayout() {
  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: ACTIVE_TINT,
        tabBarInactiveTintColor: INACTIVE_TINT,
        tabBarStyle: { backgroundColor: colors.surface, borderTopColor: colors.border },
      }}
    >
      <Tabs.Screen
        name="index"
        options={{ title: 'Home', tabBarIcon: tabIcon('home-outline', 'home') }}
      />
      <Tabs.Screen
        name="progress"
        options={{ title: 'Progress', tabBarIcon: tabIcon('trending-up-outline', 'trending-up') }}
      />
      <Tabs.Screen
        name="wrapped"
        options={{ title: 'Wrapped', tabBarIcon: tabIcon('sparkles-outline', 'sparkles') }}
      />
      <Tabs.Screen
        name="activities"
        options={{ title: 'Activities', tabBarIcon: tabIcon('list-outline', 'list') }}
      />
      <Tabs.Screen
        name="profile"
        options={{
          title: 'Profile',
          tabBarIcon: tabIcon('person-circle-outline', 'person-circle'),
        }}
      />
    </Tabs>
  );
}
