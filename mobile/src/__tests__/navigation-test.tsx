import { fireEvent, renderRouter, screen } from 'expo-router/testing-library';

describe('tab navigation', () => {
  it('opens on Home with the zero-activity state', async () => {
    await renderRouter('./app');

    expect(screen).toHavePathname('/');
    expect(screen.getByText('No runs yet')).toBeOnTheScreen();
  });

  it.each([
    ['Progress', '/progress'],
    ['Wrapped', '/wrapped'],
    ['Activities', '/activities'],
    ['Profile', '/profile'],
  ])('switches to the %s tab', async (title, pathname) => {
    await renderRouter('./app');

    await fireEvent.press(screen.getByRole('button', { name: new RegExp(title) }));

    expect(screen).toHavePathname(pathname);
    expect(screen.getByRole('header', { name: title })).toBeOnTheScreen();
  });
});
