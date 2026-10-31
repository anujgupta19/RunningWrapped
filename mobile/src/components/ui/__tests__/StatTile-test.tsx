import { render, screen } from '@testing-library/react-native';

import { StatTile } from '../StatTile';

describe('StatTile', () => {
  it('shows label, value, and unit, and reads them as one accessible element', async () => {
    await render(<StatTile label="Distance" value="12.4" unit="mi" caption="+8% vs last week" />);

    expect(screen.getByText('Distance')).toBeOnTheScreen();
    expect(screen.getByText('12.4')).toBeOnTheScreen();
    expect(screen.getByText('mi')).toBeOnTheScreen();
    expect(screen.getByLabelText('Distance, 12.4 mi, +8% vs last week')).toBeOnTheScreen();
  });

  it('renders an empty placeholder without a unit when there is no value', async () => {
    await render(<StatTile label="Avg pace" value={null} unit="/mi" />);

    expect(screen.getByText('—')).toBeOnTheScreen();
    expect(screen.queryByText('/mi')).toBeNull();
    expect(screen.getByLabelText('Avg pace, no data yet')).toBeOnTheScreen();
  });
});
