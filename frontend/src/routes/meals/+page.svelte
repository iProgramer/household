<script lang="ts">
  import { onMount } from 'svelte';
  import { recipes as recipesApi, ApiError } from '$lib/api';
  import type { Recipe } from '$lib/api';

  let recipeList = $state<Recipe[]>([]);
  let loading = $state(true);
  let loadError = $state('');
  let newTitle = $state('');
  let saving = $state(false);
  let inputEl = $state<HTMLInputElement | undefined>(undefined);
  let editingId = $state<string | null>(null);
  let editTitle = $state('');
  let openMenuId = $state<string | null>(null);
  let menuAbove = $state(false);

  async function load() {
    loading = true;
    loadError = '';
    try {
      recipeList = await recipesApi.list();
    } catch (e) {
      loadError = e instanceof ApiError ? e.message : 'Fehler beim Laden';
    } finally {
      loading = false;
    }
  }

  async function addRecipe() {
    const title = newTitle.trim();
    if (!title || saving) return;
    saving = true;
    try {
      const recipe = await recipesApi.create(title);
      recipeList = [...recipeList, recipe];
      newTitle = '';
    } finally {
      saving = false;
      inputEl?.focus();
    }
  }

  async function deleteRecipe(id: string) {
    await recipesApi.delete(id);
    recipeList = recipeList.filter((r) => r.id !== id);
  }

  let editInputEl = $state<HTMLInputElement | null>(null);

  async function startEdit(recipe: Recipe) {
    editingId = recipe.id;
    editTitle = recipe.title;
    await Promise.resolve();
    editInputEl?.focus();
    editInputEl?.select();
  }

  async function saveEdit() {
    if (!editingId) return;
    const id = editingId;
    editingId = null;
    const trimmed = editTitle.trim();
    const original = recipeList.find((r) => r.id === id);
    if (!trimmed || trimmed === original?.title) return;
    const updated = await recipesApi.rename(id, trimmed);
    recipeList = recipeList.map((r) => (r.id === id ? updated : r));
  }

  function handleEditKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter') { e.preventDefault(); saveEdit(); }
    if (e.key === 'Escape') { editingId = null; }
  }

  function onKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter') {
      e.preventDefault();
      addRecipe();
    }
  }

  onMount(() => { load(); });
</script>

<div class="page">
  <header class="page-header">
    <h1>Rezept-Bibliothek</h1>
  </header>

  <p class="intro muted">Eure Lieblingsgerichte und Klassiker – immer griffbereit für die Wochenplanung.</p>

  <div class="add-row">
    <input
      bind:this={inputEl}
      type="text"
      placeholder="Neues Rezept hinzufügen…"
      bind:value={newTitle}
      onkeydown={onKeydown}
      disabled={saving}
      class="add-input"
    />
    <button class="add-btn" onclick={addRecipe} disabled={saving || !newTitle.trim()}>
      +
    </button>
  </div>

  {#if loading}
    <div class="state-msg muted">Laden…</div>
  {:else if loadError}
    <div class="state-msg" style="color: var(--accent-rose)">{loadError}</div>
  {:else if recipeList.length === 0}
    <p class="state-msg muted">Noch keine Rezepte in der Bibliothek.</p>
  {:else}
    <ul class="ideas-list">
      {#each recipeList as recipe (recipe.id)}
        <li class="idea-item card">
          {#if openMenuId === recipe.id}
            <div role="presentation" class="backdrop" onclick={() => { openMenuId = null; }}></div>
          {/if}

          {#if editingId === recipe.id}
            <input
              bind:this={editInputEl}
              class="edit-input"
              bind:value={editTitle}
              onblur={saveEdit}
              onkeydown={handleEditKeydown}
            />
          {:else}
            <span class="idea-title">{recipe.title}</span>
          {/if}

          {#if editingId !== recipe.id}
            <div class="menu-wrap">
              <button
                class="menu-btn"
                onclick={(e) => {
                  e.stopPropagation();
                  if (openMenuId !== recipe.id) {
                    const rect = (e.currentTarget as HTMLElement).getBoundingClientRect();
                    menuAbove = window.innerHeight - rect.bottom < 150;
                    openMenuId = recipe.id;
                  } else {
                    openMenuId = null;
                  }
                }}
                aria-label="Aktionen"
              >⋮</button>

              {#if openMenuId === recipe.id}
                <div class="menu-dropdown" class:above={menuAbove} role="menu">
                  <button class="menu-item" role="menuitem" onclick={() => { openMenuId = null; startEdit(recipe); }}>
                    Bearbeiten
                  </button>
                  <button class="menu-item danger" role="menuitem" onclick={() => { openMenuId = null; deleteRecipe(recipe.id); }}>
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
    flex-wrap: wrap;
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

  .menu-dropdown.above {
    top: auto;
    bottom: calc(100% + 4px);
    box-shadow: 2px -2px 0 var(--color-border);
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
