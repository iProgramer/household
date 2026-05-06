const ACCENTS = [
  { bg: 'var(--accent-sage-bg)',  border: 'var(--accent-sage)' },
  { bg: 'var(--accent-rose-bg)',  border: 'var(--accent-rose)' },
  { bg: 'var(--accent-amber-bg)', border: 'var(--accent-amber)' },
  { bg: 'var(--accent-lilac-bg)', border: 'var(--accent-lilac)' },
];

export function taskAccent(id: string) {
  const hash = id.split('').reduce((acc, c) => acc + c.charCodeAt(0), 0);
  return ACCENTS[hash % ACCENTS.length];
}
