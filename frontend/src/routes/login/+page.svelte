<script lang="ts">
  import { goto } from '$app/navigation';
  import { auth as apiAuth, ApiError } from '$lib/api';
  import { authStore } from '$lib/stores/auth';

  let email = $state('');
  let password = $state('');
  let error = $state('');
  let loading = $state(false);

  async function handleLogin() {
    if (loading) return;
    error = '';
    loading = true;
    try {
      const res = await apiAuth.login(email, password);
      authStore.login(res.token, res.memberId, email);
      goto('/today');
    } catch (e) {
      error = e instanceof ApiError ? e.message : 'Anmeldung fehlgeschlagen';
    } finally {
      loading = false;
    }
  }
</script>

<div class="page">
  <div class="card auth-card">
    <h1>Anmelden</h1>
    <form onsubmit={(e) => { e.preventDefault(); handleLogin(); }}>
      <div class="field">
        <label for="email">E-Mail</label>
        <input id="email" type="email" bind:value={email} required autocomplete="email" />
      </div>
      <div class="field">
        <label for="password">Passwort</label>
        <input id="password" type="password" bind:value={password} required autocomplete="current-password" />
      </div>
      {#if error}
        <p class="error-msg">{error}</p>
      {/if}
      <button type="submit" class="btn-primary" disabled={loading}>
        {loading ? '…' : 'Anmelden'}
      </button>
    </form>
    <p class="muted center-text">
      Noch kein Konto? <a href="/register">Registrieren</a>
    </p>
    <p class="muted center-text">
      <a href="/reset-password">Passwort zurücksetzen</a>
    </p>
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

  h1 { margin-bottom: 1.5rem; }

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
</style>
