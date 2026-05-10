export function isoDate(d: Date): string {
  return d.toISOString().slice(0, 10);
}

export function getWeekMonday(d: Date = new Date()): Date {
  const result = new Date(d);
  const day = result.getDay();
  const diff = day === 0 ? -6 : 1 - day;
  result.setDate(result.getDate() + diff);
  return result;
}

export function addDays(d: Date, n: number): Date {
  const result = new Date(d);
  result.setDate(result.getDate() + n);
  return result;
}

export function formatDate(d: Date): string {
  return d.toLocaleDateString('de-DE', { weekday: 'long', day: 'numeric', month: 'long' });
}

export function formatDay(d: Date): string {
  return d.toLocaleDateString('de-DE', { weekday: 'short' });
}

/**
 * Format an ISO date string ("2026-05-12") as "Di, 12. Mai".
 * Parses as local date to avoid UTC midnight shifting the day.
 */
export function formatShortDate(iso: string): string {
  const [y, m, d] = iso.split('-').map(Number);
  const date = new Date(y, m - 1, d);
  return date.toLocaleDateString('de-DE', { weekday: 'short', day: 'numeric', month: 'short' });
}
