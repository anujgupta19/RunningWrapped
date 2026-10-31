import { cn } from '../cn';

describe('cn', () => {
  it('joins truthy class names with spaces', () => {
    expect(cn('a', 'b', 'c')).toBe('a b c');
  });

  it('skips false, null, undefined, and empty strings', () => {
    expect(cn('a', false, null, undefined, '', 'b')).toBe('a b');
  });
});
