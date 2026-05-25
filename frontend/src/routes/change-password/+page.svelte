<script lang="ts">
  import { goto } from '$app/navigation';
  import { auth as apiAuth, ApiError } from '$lib/api';

  let currentPassword = $state('');
  let newPassword = $state('');
  let confirmPassword = $state('');
  let error = $state('');
  let success = $state(false);
  let loading = $state(false);

  async function handleSubmit() {
    if (loading) return;
    error = '';
    if (newPassword !== confirmPassword) {
      error = 'Die neuen Passwörter stimmen nicht überein';
      return;
    }
    if (newPassword.length < 8) {
      error = 'Das neue Passwort muss mindestens 8 Zeichen lang sein';
      return;
    }
    loading = true;
    try {
      await apiAuth.changePassword(currentPassword, newPassword);
      success = true;
    } catch (e) {
      error = e instanceof ApiError ? e.message : 'Passwort konnte nicht geändert werden';
    } finally {
      loading = false;
    }
  }
</script>

<div class="page">
  <div class="card auth-card">
    <h1>Passwort ändern</h1>
    {#if success}
      <p class="success-msg">Passwort erfolgreich geändert.</p>
      <button class="btn-primary" onclick={() => goto('/today')}>Zurück</button>
    {:else}
      <form onsubmit={(e) => { e.preventDefault(); handleSubmit(); }}>
        <div class="field">
          <label for="current">Aktuelles Passwort</label>
          <input id="current" type="password" bind:value={currentPassword} required autocomplete="current-password" />
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
          {loading ? '…' : 'Passwort ändern'}
        </button>
      </form>
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
</style>
