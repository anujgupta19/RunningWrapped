import { Card, Screen, Text } from '@/components/ui';

type PlaceholderScreenProps = {
  title: string;
  description: string;
};

/** Temporary tab content until the feature's roadmap step lands. */
export function PlaceholderScreen({ title, description }: PlaceholderScreenProps) {
  return (
    <Screen>
      <Text variant="headline">{title}</Text>
      <Card className="mt-6">
        <Text tone="secondary">{description}</Text>
      </Card>
    </Screen>
  );
}
