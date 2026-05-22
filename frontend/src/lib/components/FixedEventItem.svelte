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
  let menuOpen = $state(false);
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

{#if menuOpen}
  <div role="presentation" class="backdrop" onclick={() => { menuOpen = false; }}></div>
{/if}

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

  {#if !editing && (onrename || ondelete)}
    <div class="menu-wrap">
      <button
        class="menu-btn"
        onclick={(e) => { e.stopPropagation(); menuOpen = !menuOpen; }}
        aria-label="Aktionen"
      >⋮</button>

      {#if menuOpen}
        <div class="menu-dropdown" role="menu">
          {#if onrename}
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
