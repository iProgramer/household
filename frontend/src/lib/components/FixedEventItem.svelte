<script lang="ts">
  import type { FixedEvent, Recurrence } from '$lib/api';

  const WEEKDAYS = [
    { value: 'MONDAY',    label: 'Montag' },
    { value: 'TUESDAY',   label: 'Dienstag' },
    { value: 'WEDNESDAY', label: 'Mittwoch' },
    { value: 'THURSDAY',  label: 'Donnerstag' },
    { value: 'FRIDAY',    label: 'Freitag' },
    { value: 'SATURDAY',  label: 'Samstag' },
    { value: 'SUNDAY',    label: 'Sonntag' },
  ];

  const RECURRENCE_TYPES = [
    { value: 'WEEKLY',    label: 'Wöchentlich' },
    { value: 'BIWEEKLY',  label: 'Zweiwöchentlich' },
    { value: 'MONTHLY',   label: 'Monatlich' },
    { value: 'DAILY',     label: 'Täglich' },
    { value: 'ON_WEEKDAY', label: 'Jeden Wochentag' },
  ];

  let {
    event,
    onedit,
    ondelete,
  }: {
    event: FixedEvent;
    onedit?: (data: { title: string; date: string; recurrence: Recurrence }) => Promise<void>;
    ondelete?: () => Promise<void>;
  } = $props();

  let editing = $state(false);
  let menuOpen = $state(false);
  let editTitle = $state('');
  let editDate = $state('');
  let editRecurrenceType = $state('WEEKLY');
  let editWeekday = $state('MONDAY');
  let titleInputEl = $state<HTMLInputElement | null>(null);

  async function startEdit() {
    editTitle = event.title;
    editDate = event.date;
    editRecurrenceType = event.recurrence?.type ?? 'WEEKLY';
    editWeekday = event.recurrence?.weekday ?? 'MONDAY';
    editing = true;
    await Promise.resolve();
    titleInputEl?.focus();
    titleInputEl?.select();
  }

  async function saveEdit() {
    if (!editing) return;
    editing = false;
    const trimmed = editTitle.trim();
    if (!trimmed || !editDate) return;
    const recurrence: Recurrence =
      editRecurrenceType === 'ON_WEEKDAY'
        ? { type: 'ON_WEEKDAY', weekday: editWeekday }
        : { type: editRecurrenceType as Recurrence['type'] };
    await onedit?.({ title: trimmed, date: editDate, recurrence });
  }

  function cancelEdit() {
    editing = false;
  }
</script>

{#if menuOpen}
  <div role="presentation" class="backdrop" onclick={() => { menuOpen = false; }}></div>
{/if}

<div class="event-item card">
  {#if editing}
    <div class="edit-form">
      <div class="edit-row">
        <input
          bind:this={titleInputEl}
          class="edit-input-title"
          bind:value={editTitle}
          placeholder="Titel"
          onkeydown={(e) => { if (e.key === 'Escape') cancelEdit(); }}
        />
        <input
          type="date"
          class="edit-input-date"
          bind:value={editDate}
        />
      </div>
      <div class="edit-row">
        <select class="edit-select" bind:value={editRecurrenceType}>
          {#each RECURRENCE_TYPES as r}
            <option value={r.value}>{r.label}</option>
          {/each}
        </select>
        {#if editRecurrenceType === 'ON_WEEKDAY'}
          <select class="edit-select" bind:value={editWeekday}>
            {#each WEEKDAYS as d}
              <option value={d.value}>{d.label}</option>
            {/each}
          </select>
        {/if}
      </div>
      <div class="edit-actions">
        <button class="btn-cancel" onclick={cancelEdit}>Abbrechen</button>
        <button
          class="btn-save"
          disabled={!editTitle.trim() || !editDate}
          onclick={saveEdit}
        >Speichern</button>
      </div>
    </div>
  {:else}
    <span class="event-dot"></span>
    <span class="event-title">{event.title}</span>
    {#if event.recurrence}
      <span class="muted badge">↻</span>
    {/if}

    {#if onedit || ondelete}
      <div class="menu-wrap">
        <button
          class="menu-btn"
          onclick={(e) => { e.stopPropagation(); menuOpen = !menuOpen; }}
          aria-label="Aktionen"
        >⋮</button>

        {#if menuOpen}
          <div class="menu-dropdown" role="menu">
            {#if onedit}
              <button class="menu-item" role="menuitem" onclick={() => { menuOpen = false; startEdit(); }}>
                Bearbeiten
              </button>
            {/if}
            {#if ondelete}
              <button class="menu-item danger" role="menuitem" onclick={() => { menuOpen = false; ondelete?.(); }}>
                Löschen
              </button>
            {/if}
          </div>
        {/if}
      </div>
    {/if}
  {/if}
</div>

<style>
  .backdrop {
    position: fixed;
    inset: 0;
    z-index: 10;
  }

  .event-item {
    display: flex;
    align-items: center;
    gap: 0.625rem;
    padding: 0.625rem 1rem;
    margin-bottom: 0.5rem;
    background: var(--accent-amber-bg);
  }

  .event-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: var(--accent-amber);
    border: 1px solid var(--color-border);
    flex-shrink: 0;
  }

  .event-title {
    flex: 1;
    font-size: 0.9375rem;
  }

  .badge {
    font-size: 0.75rem;
    margin-left: auto;
  }

  /* Edit form */
  .edit-form {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 0.375rem;
  }

  .edit-row {
    display: flex;
    gap: 0.375rem;
    flex-wrap: wrap;
  }

  .edit-input-title {
    flex: 1;
    min-width: 100px;
    font-size: 0.9375rem;
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    background: var(--color-surface);
    outline: none;
    padding: 0.375rem 0.5rem;
    font-family: inherit;
  }

  .edit-input-date {
    font-size: 0.875rem;
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    background: var(--color-surface);
    outline: none;
    padding: 0.375rem 0.5rem;
  }

  .edit-select {
    flex: 1;
    font-size: 0.875rem;
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    background: var(--color-surface);
    outline: none;
    padding: 0.375rem 0.5rem;
    font-family: inherit;
  }

  .edit-actions {
    display: flex;
    gap: 0.375rem;
    justify-content: flex-end;
    margin-top: 0.125rem;
  }

  .btn-cancel {
    padding: 0.3rem 0.625rem;
    font-size: 0.8125rem;
    color: var(--color-muted);
    border: var(--border-width) solid var(--color-divider);
    border-radius: var(--border-radius-sm);
    background: var(--color-surface);
  }

  .btn-save {
    padding: 0.3rem 0.625rem;
    font-size: 0.8125rem;
    font-weight: 700;
    background: var(--color-text);
    color: var(--color-surface);
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
  }

  .btn-save:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  /* Kebab menu */
  .menu-wrap {
    position: relative;
    flex-shrink: 0;
  }

  .menu-btn {
    color: var(--color-muted);
    font-size: 1.125rem;
    line-height: 1;
    padding: 0 0.3rem;
    letter-spacing: -0.05em;
  }

  .menu-btn:hover {
    color: var(--color-text);
  }

  .menu-dropdown {
    position: absolute;
    right: 0;
    top: calc(100% + 4px);
    z-index: 20;
    background: var(--color-surface);
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    box-shadow: 2px 2px 0 var(--color-border);
    min-width: 9rem;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .menu-item {
    padding: 0.625rem 0.875rem;
    text-align: left;
    font-size: 0.875rem;
    color: var(--color-text);
    white-space: nowrap;
    border-bottom: var(--border-width) solid var(--color-divider);
  }

  .menu-item:last-child {
    border-bottom: none;
  }

  .menu-item:hover {
    background: var(--color-bg);
  }

  .menu-item.danger {
    color: var(--accent-rose);
  }
</style>
