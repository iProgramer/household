<script lang="ts">
  import { goto } from '$app/navigation';
  import { auth as apiAuth, ApiError } from '$lib/api';

  let email = $state('');
  let inviteCode = $state('');
  let newPassword = $state('');
  let confirmPassword = $state('');
  let error = $state('');
  let success = $state(false);
  let loading = $state(false);

  async function handleSubmit() {
    if (loading) return;
    error = '';
    if (newPassword !== confirmPassword) {
      error = 'Die Passwörter stimmen nicht überein';
      return;
    }
    if (newPassword.length < 8) {
      error = 'Das Passwort muss mindestens 8 Zeichen lang sein';
      return;
    }
    loading = true;
    try {
      await apiAuth.resetPassword(email, inviteCode, newPassword);
      success = true;
    } catch (e) {
      error = e instanceof ApiError ? e.message : 'Passwort konnte nicht zurückgesetzt werden';
    } finally {
      loading = false;
    }
  }
</script>

<div class="page">
  <div class="card auth-card">
    <h1>Passwort zurücksetzen</h1>
    {#if success}
      <p class="success-msg">Passwort erfolgreich zurückgesetzt.</p>
      <button class="btn-primary" onclick={() => goto('/login')}>Zur Anmeldung</button>
    {:else}
      <p class="hint">Gib deine E-Mail-Adresse und den Einladungscode deines Haushalts ein.</p>
      <form onsubmit={(e) => { e.preventDefault(); handleSubmit(); }}>
        <div class="field">
          <label for="email">E-Mail</label>
          <input id="email" type="email" bind:value={email} required autocomplete="email" />
        </div>
        <div class="field">
          <label for="invite">Einladungscode</label>
          <input id="invite" type="text" bind:value={inviteCode} required autocomplete="off" />
        </div>
        <div class="field">
          <label for="new">Neues Passwort</label>
          <input id="new" type="password" bind:value={newPassword} required autocomplete="new-password" />
        </div>
        <div class="field">
          <label for="confirm">Neues Passwort bestätigen</label>
          <input id="confirm" type="password" bind:value={confirmPassword} required autocomplete="new-password" />
        </div>
        {#if error}
          <p class="error-msg">{error}</p>
        {/if}
        <button type="submit" class="btn-primary" disabled={loading}>
          {loading ? '…' : 'Passwort zurücksetzen'}
        </button>
      </form>
      <p class="muted center-text">
        <a href="/login">Zurück zur Anmeldung</a>
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

  h1 { margin-bottom: 1rem; }

  .hint {
    font-size: 0.875rem;
    color: var(--color-muted);
    margin-bottom: 1.25rem;
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

  .success-msg {
    color: var(--accent-sage);
    font-size: 0.9375rem;
    margin-bottom: 1.25rem;
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
