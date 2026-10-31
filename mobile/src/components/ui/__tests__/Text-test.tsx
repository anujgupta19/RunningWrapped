import { render, screen } from '@testing-library/react-native';

import { Text } from '../Text';

describe('Text', () => {
  it('marks headline and title text as headers for screen readers', async () => {
    await render(
      <>
        <Text variant="headline">Progress</Text>
        <Text variant="title">This month</Text>
        <Text>Body copy</Text>
      </>,
    );

    expect(screen.getByRole('header', { name: 'Progress' })).toBeOnTheScreen();
    expect(screen.getByRole('header', { name: 'This month' })).toBeOnTheScreen();
    expect(screen.queryByRole('header', { name: 'Body copy' })).toBeNull();
  });
});
