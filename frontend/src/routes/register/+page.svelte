<script lang="ts">
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { auth as apiAuth, ApiError } from '$lib/api';
  import { authStore } from '$lib/stores/auth';
  import confetti from 'canvas-confetti';

  type Mode = 'create' | 'join';

  let mode = $state<Mode>('create');
  let email = $state('');
  let password = $state('');
  let inviteCode = $state('');
  let error = $state('');
  let loading = $state(false);
  let createdInviteCode = $state<string | null>(null);
  let joined = $state(false);

  onMount(() => {
    const invite = $page.url.searchParams.get('invite');
    if (invite) {
      inviteCode = invite;
      mode = 'join';
    }
  });

  function fireConfetti() {
    const duration = 3000;
    const end = Date.now() + duration;
    const colors = ['#8BAF8B', '#B8A9C9', '#F5C518', '#E8927C', '#ffffff'];

    (function frame() {
      confetti({
        particleCount: 6,
        angle: 60,
        spread: 55,
        origin: { x: 0 },
        colors,
      });
      confetti({
        particleCount: 6,
        angle: 120,
        spread: 55,
        origin: { x: 1 },
        colors,
      });
      if (Date.now() < end) requestAnimationFrame(frame);
    })();
  }

  async function handleSubmit() {
    if (loading) return;
    error = '';
    loading = true;
    try {
      if (mode === 'create') {
        const res = await apiAuth.register(email, password);
        authStore.login(res.token, res.memberId, email);
        createdInviteCode = res.inviteCode;
      } else {
        const res = await apiAuth.join(email, password, inviteCode);
        authStore.login(res.token, res.memberId, email);
        joined = true;
        fireConfetti();
      }
    } catch (e) {
      error = e instanceof ApiError ? e.message : 'Registrierung fehlgeschlagen';
    } finally {
      loading = false;
    }
  }
</script>

<div class="page">
  <div class="card auth-card">
    {#if joined}
      <div class="celebration">
        <div class="celebration-icon">🎉</div>
        <h1>Willkommen!</h1>
        <p class="celebration-text">Du bist jetzt dabei.<br/>Alles bereit – los geht's!</p>
        <button class="btn-primary" onclick={() => goto('/today')}>Los geht's →</button>
      </div>
    {:else if createdInviteCode}
      <h1>Haushalt erstellt!</h1>
      <p class="invite-info">Teile diesen Code mit deinem Partner, damit er beitreten kann:</p>
      <div class="invite-code">{createdInviteCode}</div>
      <button class="btn-primary" onclick={() => goto('/today')}>Los geht's</button>
    {:else}
      <h1>Registrieren</h1>

      <div class="mode-toggle">
        <button class:active={mode === 'create'} onclick={() => { mode = 'create'; error = ''; }}>
          Neuer Haushalt
        </button>
        <button class:active={mode === 'join'} onclick={() => { mode = 'join'; error = ''; }}>
          Beitreten
        </button>
      </div>

      <form onsubmit={(e) => { e.preventDefault(); handleSubmit(); }}>
        <div class="field">
          <label for="email">E-Mail</label>
          <input id="email" type="email" bind:value={email} required autocomplete="email" />
        </div>
        <div class="field">
          <label for="password">Passwort</label>
          <input id="password" type="password" bind:value={password} required autocomplete="new-password" />
        </div>
        {#if mode === 'join'}
          <div class="field">
            <label for="invite">Einladungscode</label>
            <input id="invite" type="text" bind:value={inviteCode} required placeholder="z.B. ABC123" />
          </div>
        {/if}
        {#if error}
          <p class="error-msg">{error}</p>
        {/if}
        <button type="submit" class="btn-primary" disabled={loading}>
          {loading ? '…' : mode === 'create' ? 'Haushalt erstellen' : 'Beitreten'}
        </button>
      </form>

      <p class="muted center-text">
        Schon ein Konto? <a href="/login">Anmelden</a>
      </p>
    {/if}
  </div>
</div>

<style>
  .page {
    min-height: 100dvh;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 1rem;
  }

  .auth-card {
    width: 100%;
    max-width: 400px;
    padding: 2rem;
  }

  h1 { margin-bottom: 1.25rem; }

  .mode-toggle {
    display: flex;
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    overflow: hidden;
    margin-bottom: 1.25rem;
  }

  .mode-toggle button {
    flex: 1;
    padding: 0.5rem;
    font-size: 0.875rem;
    font-weight: 500;
    color: var(--color-muted);
    border-right: var(--border-width) solid var(--color-border);
  }

  .mode-toggle button:last-child {
    border-right: none;
  }

  .mode-toggle button.active {
    background: var(--color-text);
    color: var(--color-surface);
    font-weight: 700;
  }

  .field {
    display: flex;
    flex-direction: column;
    gap: 0.375rem;
    margin-bottom: 1rem;
  }

  label {
    font-size: 0.875rem;
    font-weight: 600;
  }

  input {
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    padding: 0.625rem 0.75rem;
    background: var(--color-surface);
    outline: none;
    width: 100%;
  }

  input:focus {
    box-shadow: var(--shadow-card);
  }

  .error-msg {
    color: var(--accent-rose);
    font-size: 0.875rem;
    margin-bottom: 0.75rem;
  }

  .btn-primary {
    width: 100%;
    padding: 0.75rem;
    background: var(--color-text);
    color: var(--color-surface);
    border-radius: var(--border-radius-sm);
    font-weight: 700;
    font-size: 1rem;
    border: var(--border-width) solid var(--color-border);
    box-shadow: var(--shadow-card);
    margin-top: 0.5rem;
  }

  .btn-primary:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  .center-text {
    text-align: center;
    margin-top: 1.25rem;
  }

  .center-text a {
    font-weight: 600;
    text-decoration: underline;
  }

  .celebration {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 0.75rem;
    padding: 1rem 0;
  }

  .celebration-icon {
    font-size: 3.5rem;
    line-height: 1;
    margin-bottom: 0.25rem;
  }

  .celebration h1 {
    margin-bottom: 0;
  }

  .celebration-text {
    color: var(--color-muted);
    font-size: 0.9375rem;
    line-height: 1.6;
    margin-bottom: 0.5rem;
  }

  .invite-info {
    margin-bottom: 1rem;
    color: var(--color-muted);
    font-size: 0.9375rem;
  }

  .invite-code {
    font-size: 1.5rem;
    font-weight: 700;
    letter-spacing: 0.1em;
    text-align: center;
    padding: 1rem;
    background: var(--accent-sage-bg);
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius);
    box-shadow: var(--shadow-card);
    margin-bottom: 1.5rem;
  }
</style>
