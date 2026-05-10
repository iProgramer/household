import { writable } from 'svelte/store';
import { browser } from '$app/environment';
import { members as membersApi } from '$lib/api';
import type { Member } from '$lib/api';

function createMembersStore() {
  const store = writable<Member[]>([]);

  return {
    subscribe: store.subscribe,
    async load() {
      if (!browser) return;
      try {
        const data = await membersApi.list();
        store.set(data);
      } catch {
        // silently ignore – will retry on next navigation
      }
    },
    clear() {
      store.set([]);
    },
  };
}

export const membersStore = createMembersStore();

// Stable colour pair per member (sorted by id for consistency across sessions)
const MEMBER_PALETTE = [
  { color: 'var(--accent-sage)',  bg: 'var(--accent-sage-bg)' },
  { color: 'var(--accent-lilac)', bg: 'var(--accent-lilac-bg)' },
];

export function memberPalette(allMembers: Member[], memberId: string) {
  const sorted = [...allMembers].sort((a, b) => a.id.localeCompare(b.id));
  const idx = sorted.findIndex((m) => m.id === memberId);
  return MEMBER_PALETTE[idx >= 0 ? idx % MEMBER_PALETTE.length : 0];
}
