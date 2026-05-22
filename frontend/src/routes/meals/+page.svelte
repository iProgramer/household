<script lang="ts">
  import { onMount } from 'svelte';
  import { meals as mealsApi, ApiError } from '$lib/api';
  import type { Meal } from '$lib/api';

  let ideas = $state<Meal[]>([]);
  let loading = $state(true);
  let loadError = $state('');
  let newTitle = $state('');
  let saving = $state(false);
  let inputEl = $state<HTMLInputElement | undefined>(undefined);
  let editingId = $state<string | null>(null);
  let editTitle = $state('');
  let openMenuId = $state<string | null>(null);

  async function load() {
    loading = true;
    loadError = '';
    try {
      ideas = await mealsApi.ideas();
    } catch (e) {
      loadError = e instanceof ApiError ? e.message : 'Fehler beim Laden';
    } finally {
      loading = false;
    }
  }

  async function addIdea() {
    const title = newTitle.trim();
    if (!title || saving) return;
    saving = true;
    try {
      const meal = await mealsApi.create(title);
      ideas = [...ideas, meal];
      newTitle = '';
    } finally {
      saving = false;
      inputEl?.focus();
    }
  }

  async function deleteIdea(id: string) {
    await mealsApi.delete(id);
    ideas = ideas.filter((m) => m.id !== id);
  }

  let editInputEl = $state<HTMLInputElement | null>(null);

  async function startEdit(idea: Meal) {
    editingId = idea.id;
    editTitle = idea.title;
    await Promise.resolve();
    editInputEl?.focus();
    editInputEl?.select();
  }

  async function saveEdit() {
    if (!editingId) return;
    const id = editingId;
    editingId = null;
    const trimmed = editTitle.trim();
    const original = ideas.find((m) => m.id === id);
    if (!trimmed || trimmed === original?.title) return;
    const updated = await mealsApi.rename(id, trimmed);
    ideas = ideas.map((m) => (m.id === id ? updated : m));
  }

  function handleEditKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter') { e.preventDefault(); saveEdit(); }
    if (e.key === 'Escape') { editingId = null; }
  }

  function onKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter') {
      e.preventDefault();
      addIdea();
    }
  }

  onMount(() => { load(); });
</script>

<div class="page">
  <header class="page-header">
    <h1>Mahlzeiten-Pool</h1>
  </header>

  <p class="intro muted">Ideen sammeln und bei der Wochenplanung auf Tage einplanen.</p>

  <div class="add-row">
    <input
      bind:this={inputEl}
      type="text"
      placeholder="Neue Idee hinzufügen…"
      bind:value={newTitle}
      onkeydown={onKeydown}
      disabled={saving}
      class="add-input"
    />
    <button class="add-btn" onclick={addIdea} disabled={saving || !newTitle.trim()}>
      +
    </button>
  </div>

  {#if loading}
    <div class="state-msg muted">Laden…</div>
  {:else if loadError}
    <div class="state-msg" style="color: var(--accent-rose)">{loadError}</div>
  {:else if ideas.length === 0}
    <p class="state-msg muted">Noch keine Ideen im Pool.</p>
  {:else}
    <ul class="ideas-list">
      {#each ideas as idea (idea.id)}
        <li class="idea-item card">
          {#if openMenuId === idea.id}
            <div role="presentation" class="backdrop" onclick={() => { openMenuId = null; }}></div>
          {/if}

          {#if editingId === idea.id}
            <input
              bind:this={editInputEl}
              class="edit-input"
              bind:value={editTitle}
              onblur={saveEdit}
              onkeydown={handleEditKeydown}
            />
          {:else}
            <span class="idea-title">{idea.title}</span>
          {/if}

          {#if editingId !== idea.id}
            <div class="menu-wrap">
              <button
                class="menu-btn"
                onclick={(e) => { e.stopPropagation(); openMenuId = openMenuId === idea.id ? null : idea.id; }}
                aria-label="Aktionen"
              >⋮</button>

              {#if openMenuId === idea.id}
                <div class="menu-dropdown" role="menu">
                  <button class="menu-item" role="menuitem" onclick={() => { openMenuId = null; startEdit(idea); }}>
                    Bearbeiten
                  </button>
                  <button class="menu-item danger" role="menuitem" onclick={() => { openMenuId = null; deleteIdea(idea.id); }}>
                    Löschen
                  </button>
                </div>
              {/if}
            </div>
          {/if}
        </li>
      {/each}
    </ul>
  {/if}
</div>

<style>
  .page {
    padding: 1.25rem 1rem 1rem;
    max-width: 640px;
    margin: 0 auto;
  }

  .page-header {
    margin-bottom: 0.375rem;
  }

  .intro {
    font-size: 0.875rem;
    margin-bottom: 1.25rem;
  }

  .add-row {
    display: flex;
    gap: 0.5rem;
    margin-bottom: 1.25rem;
  }

  .add-input {
    flex: 1;
    padding: 0.5rem 0.75rem;
    font-size: 0.9375rem;
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    background: var(--color-surface);
    color: var(--color-text);
    outline: none;
    box-shadow: var(--shadow-card);
  }

  .add-input:focus {
    border-color: var(--color-text);
  }

  .add-btn {
    padding: 0.5rem 0.875rem;
    font-size: 1.125rem;
    font-weight: 700;
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    background: var(--color-surface);
    box-shadow: var(--shadow-card);
    color: var(--color-text);
    flex-shrink: 0;
  }

  .add-btn:disabled {
    opacity: 0.4;
    cursor: default;
  }

  .ideas-list {
    list-style: none;
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .idea-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0.625rem 1rem;
  }

  .idea-title {
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

  .backdrop {
    position: fixed;
    inset: 0;
    z-index: 10;
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

  .state-msg {
    padding: 2rem 1rem;
    text-align: center;
    font-size: 0.9375rem;
  }
</style>
