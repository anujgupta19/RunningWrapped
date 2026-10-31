/** Joins class names, skipping falsy values. Doesn't resolve Tailwind conflicts: pass non-overlapping classes. */
export function cn(...classes: (string | false | null | undefined)[]): string {
  return classes.filter(Boolean).join(' ');
}
