import { writable, derived } from 'svelte/store';
import { browser } from '$app/environment';

interface AuthState {
  token: string | null;
  memberId: string | null;
  email: string | null;
}

function createAuthStore() {
  const store = writable<AuthState>({
    token: browser ? localStorage.getItem('token') : null,
    memberId: browser ? localStorage.getItem('memberId') : null,
    email: browser ? localStorage.getItem('email') : null,
  });

  return {
    subscribe: store.subscribe,
    login(token: string, memberId: string, email: string) {
      if (browser) {
        localStorage.setItem('token', token);
        localStorage.setItem('memberId', memberId);
        localStorage.setItem('email', email);
      }
      store.set({ token, memberId, email });
    },
    logout() {
      if (browser) {
        localStorage.removeItem('token');
        localStorage.removeItem('memberId');
        localStorage.removeItem('email');
      }
      store.set({ token: null, memberId: null, email: null });
    },
  };
}

export const authStore = createAuthStore();
export const isAuthenticated = derived(authStore, ($a) => !!$a.token);
