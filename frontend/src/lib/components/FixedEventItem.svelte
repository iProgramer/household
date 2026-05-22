<script lang="ts">
  import type { FixedEvent } from '$lib/api';

  let {
    event,
    onrename,
    ondelete,
  }: {
    event: FixedEvent;
    onrename?: (title: string) => Promise<void>;
    ondelete?: () => Promise<void>;
  } = $props();

  let editing = $state(false);
  let editTitle = $state('');
  let inputEl = $state<HTMLInputElement | null>(null);

  async function startEdit() {
    editTitle = event.title;
    editing = true;
    await Promise.resolve();
    inputEl?.focus();
    inputEl?.select();
  }

  async function saveEdit() {
    if (!editing) return;
    editing = false;
    const trimmed = editTitle.trim();
    if (trimmed && trimmed !== event.title) await onrename?.(trimmed);
  }

  function handleKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter') { e.preventDefault(); saveEdit(); }
    if (e.key === 'Escape') { editing = false; }
  }
</script>

<div class="event-item card">
  <span class="event-dot"></span>

  {#if editing}
    <input
      bind:this={inputEl}
      class="edit-input"
      bind:value={editTitle}
      onblur={saveEdit}
      onkeydown={handleKeydown}
    />
  {:else}
    <span class="event-title">{event.title}</span>
    {#if event.recurrence}
      <span class="muted badge">↻</span>
    {/if}
  {/if}

  {#if onrename && !editing}
    <button class="icon-btn" onclick={startEdit} title="Bearbeiten" aria-label="Bearbeiten">
      <svg width="12" height="12" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M11 2l3 3-9 9H2v-3L11 2z"/>
      </svg>
    </button>
  {/if}

  {#if ondelete}
    <button class="delete-btn" onclick={ondelete} title="Löschen" aria-label="Termin löschen">✕</button>
  {/if}
</div>

<style>
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

  .edit-input {
    flex: 1;
    font-size: 0.9375rem;
    border: none;
    border-bottom: var(--border-width) solid var(--color-border);
    background: transparent;
    outline: none;
    padding: 0;
    font-family: inherit;
  }

  .badge {
    font-size: 0.75rem;
    margin-left: auto;
  }

  .icon-btn {
    color: var(--color-muted);
    padding: 0.125rem 0.25rem;
    display: flex;
    align-items: center;
    flex-shrink: 0;
  }

  .icon-btn:hover {
    color: var(--color-text);
  }

  .delete-btn {
    color: var(--color-muted);
    font-size: 0.8125rem;
    padding: 0.125rem 0.25rem;
    flex-shrink: 0;
  }

  .delete-btn:hover {
    color: var(--accent-rose);
  }
</style>
