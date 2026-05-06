<script lang="ts">
  import { fixedEvents as eventsApi, ApiError } from '$lib/api';
  import type { FixedEvent } from '$lib/api';

  const WEEKDAYS = [
    { value: 'MONDAY',    label: 'Montag' },
    { value: 'TUESDAY',   label: 'Dienstag' },
    { value: 'WEDNESDAY', label: 'Mittwoch' },
    { value: 'THURSDAY',  label: 'Donnerstag' },
    { value: 'FRIDAY',    label: 'Freitag' },
    { value: 'SATURDAY',  label: 'Samstag' },
    { value: 'SUNDAY',    label: 'Sonntag' },
  ];

  let {
    defaultDate = '',
    oncreated,
    oncancel,
  }: {
    defaultDate?: string;
    oncreated: (event: FixedEvent) => void;
    oncancel: () => void;
  } = $props();

  let title = $state('');
  let date = $state('');
  $effect.pre(() => { date = defaultDate; });
  let recurrenceType = $state('NONE');
  let weekday = $state('MONDAY');
  let saving = $state(false);
  let error = $state('');

  async function handleSubmit() {
    if (saving || !title.trim() || !date) return;
    saving = true;
    error = '';
    try {
      const recurrence =
        recurrenceType === 'NONE'
          ? undefined
          : recurrenceType === 'ON_WEEKDAY'
          ? { type: 'ON_WEEKDAY' as const, weekday }
          : { type: recurrenceType as 'DAILY' | 'WEEKLY' | 'BIWEEKLY' | 'MONTHLY' };

      const event = await eventsApi.create({ title: title.trim(), date, recurrence });
      oncreated(event);
    } catch (e) {
      error = e instanceof ApiError ? e.message : 'Fehler beim Speichern';
    } finally {
      saving = false;
    }
  }
</script>

<div class="form card">
  <form onsubmit={(e) => { e.preventDefault(); handleSubmit(); }}>
    <div class="row">
      <input
        type="text"
        placeholder="Titel (z.B. Müllabfuhr)"
        bind:value={title}
        required
        class="input-title"
      />
      <input
        type="date"
        bind:value={date}
        required
        class="input-date"
      />
    </div>

    <div class="row">
      <select bind:value={recurrenceType} class="select">
        <option value="NONE">Einmalig</option>
        <option value="WEEKLY">Wöchentlich</option>
        <option value="BIWEEKLY">Zweiwöchentlich</option>
        <option value="MONTHLY">Monatlich</option>
        <option value="DAILY">Täglich</option>
        <option value="ON_WEEKDAY">Jeden bestimmten Wochentag</option>
      </select>

      {#if recurrenceType === 'ON_WEEKDAY'}
        <select bind:value={weekday} class="select">
          {#each WEEKDAYS as d}
            <option value={d.value}>{d.label}</option>
          {/each}
        </select>
      {/if}
    </div>

    {#if error}
      <p class="error-msg">{error}</p>
    {/if}

    <div class="actions">
      <button type="button" class="btn-cancel" onclick={oncancel}>Abbrechen</button>
      <button type="submit" class="btn-save" disabled={saving || !title.trim() || !date}>
        {saving ? '…' : 'Speichern'}
      </button>
    </div>
  </form>
</div>

<style>
  .form {
    padding: 0.875rem;
    margin-bottom: 0.75rem;
    background: var(--accent-amber-bg);
  }

  .row {
    display: flex;
    gap: 0.5rem;
    margin-bottom: 0.5rem;
    flex-wrap: wrap;
  }

  .input-title {
    flex: 1;
    min-width: 140px;
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    padding: 0.5rem 0.625rem;
    background: var(--color-surface);
    outline: none;
    font-size: 0.9375rem;
  }

  .input-date {
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    padding: 0.5rem 0.625rem;
    background: var(--color-surface);
    outline: none;
    font-size: 0.875rem;
  }

  .select {
    flex: 1;
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    padding: 0.5rem 0.625rem;
    background: var(--color-surface);
    font-size: 0.875rem;
    font-family: var(--font);
    outline: none;
  }

  .error-msg {
    color: var(--accent-rose);
    font-size: 0.8125rem;
    margin-bottom: 0.5rem;
  }

  .actions {
    display: flex;
    gap: 0.5rem;
    justify-content: flex-end;
  }

  .btn-cancel {
    padding: 0.4rem 0.875rem;
    font-size: 0.875rem;
    color: var(--color-muted);
    border: var(--border-width) solid var(--color-divider);
    border-radius: var(--border-radius-sm);
    background: var(--color-surface);
  }

  .btn-save {
    padding: 0.4rem 0.875rem;
    font-size: 0.875rem;
    font-weight: 700;
    background: var(--color-text);
    color: var(--color-surface);
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    box-shadow: var(--shadow-card);
  }

  .btn-save:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
</style>
