<script lang="ts">
  import { plannedMeals as plannedMealsApi, recipes as recipesApi } from '$lib/api';
  import type { Recipe, PlannedMeal } from '$lib/api';

  let {
    date,
    recipes,
    onplanned,
    onrecipecreated,
  }: {
    date: string;
    recipes: Recipe[];
    onplanned: (meal: PlannedMeal) => void;
    onrecipecreated?: (recipe: Recipe) => void;
  } = $props();

  let inputValue = $state('');
  let open = $state(false);
  let saving = $state(false);
  let inputEl = $state<HTMLInputElement | undefined>(undefined);

  let filtered = $derived(
    inputValue.trim().length === 0
      ? recipes
      : recipes.filter((r) => r.title.toLowerCase().includes(inputValue.trim().toLowerCase()))
  );

  async function selectRecipe(recipe: Recipe) {
    saving = true;
    try {
      const planned = await plannedMealsApi.plan(date, recipe.id);
      onplanned(planned);
      inputValue = '';
      open = false;
    } finally {
      saving = false;
    }
  }

  async function createAndPlan() {
    const title = inputValue.trim();
    if (!title || saving) return;
    saving = true;
    try {
      // Create as recipe in library, then plan it
      const created = await recipesApi.create(title);
      onrecipecreated?.(created);
      const planned = await plannedMealsApi.plan(date, created.id);
      onplanned(planned);
      inputValue = '';
      open = false;
    } finally {
      saving = false;
    }
  }

  function onKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter') {
      e.preventDefault();
      const exact = recipes.find((r) => r.title.toLowerCase() === inputValue.trim().toLowerCase());
      if (exact) {
        selectRecipe(exact);
      } else if (inputValue.trim()) {
        createAndPlan();
      }
    } else if (e.key === 'Escape') {
      open = false;
      inputValue = '';
    }
  }

  function onFocus() {
    open = true;
  }

  function onBlur() {
    // Delay so click on dropdown item fires first
    setTimeout(() => { open = false; }, 150);
  }
</script>

<div class="combobox">
  <input
    bind:this={inputEl}
    bind:value={inputValue}
    type="text"
    placeholder="Mahlzeit hinzufügen…"
    class="combobox-input"
    disabled={saving}
    onfocus={onFocus}
    onblur={onBlur}
    onkeydown={onKeydown}
    oninput={() => { open = true; }}
    autocomplete="off"
  />
  {#if open && (filtered.length > 0 || inputValue.trim())}
    <ul class="dropdown">
      {#each filtered as recipe (recipe.id)}
        <li>
          <button
            class="dropdown-item"
            onmousedown={(e) => { e.preventDefault(); selectRecipe(recipe); }}
          >
            {recipe.title}
          </button>
        </li>
      {/each}
      {#if inputValue.trim() && !recipes.some((r) => r.title.toLowerCase() === inputValue.trim().toLowerCase())}
        <li>
          <button
            class="dropdown-item new-item"
            onmousedown={(e) => { e.preventDefault(); createAndPlan(); }}
          >
            „{inputValue.trim()}" hinzufügen
          </button>
        </li>
      {/if}
    </ul>
  {/if}
</div>

<style>
  .combobox {
    position: relative;
  }

  .combobox-input {
    width: 100%;
    padding: 0.5rem 0.75rem;
    font-size: 0.9375rem;
    border: var(--border-width) solid var(--color-divider);
    border-radius: var(--border-radius-sm);
    background: var(--color-surface);
    color: var(--color-text);
    outline: none;
  }

  .combobox-input:focus {
    border-color: var(--color-border);
    box-shadow: var(--shadow-card);
  }

  .dropdown {
    position: absolute;
    top: calc(100% + 4px);
    left: 0;
    right: 0;
    background: var(--color-surface);
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    z-index: 50;
    list-style: none;
    padding: 0.25rem 0;
    max-height: 200px;
    overflow-y: auto;
  }

  .dropdown-item {
    display: block;
    width: 100%;
    padding: 0.5rem 0.875rem;
    font-size: 0.9375rem;
    text-align: left;
    color: var(--color-text);
  }

  .dropdown-item:hover {
    background: var(--color-divider);
  }

  .new-item {
    color: var(--color-muted);
    font-style: italic;
  }

  .new-item:hover {
    color: var(--color-text);
    background: var(--color-divider);
  }
</style>
