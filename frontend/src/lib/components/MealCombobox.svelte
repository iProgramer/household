<script lang="ts">
  import { meals as mealsApi } from '$lib/api';
  import type { Meal } from '$lib/api';

  let {
    date,
    ideas,
    onassigned,
    onideacreated,
  }: {
    date: string;
    ideas: Meal[];
    onassigned: (meal: Meal) => void;
    onideacreated?: (meal: Meal) => void;
  } = $props();

  let inputValue = $state('');
  let open = $state(false);
  let saving = $state(false);
  let inputEl = $state<HTMLInputElement | undefined>(undefined);

  let filtered = $derived(
    inputValue.trim().length === 0
      ? ideas
      : ideas.filter((m) => m.title.toLowerCase().includes(inputValue.trim().toLowerCase()))
  );

  async function selectIdea(idea: Meal) {
    saving = true;
    try {
      const assigned = await mealsApi.assign(idea.id, date);
      onassigned(assigned);
      inputValue = '';
      open = false;
    } finally {
      saving = false;
    }
  }

  async function createAndAssign() {
    const title = inputValue.trim();
    if (!title || saving) return;
    saving = true;
    try {
      const created = await mealsApi.create(title);
      onideacreated?.(created);
      const assigned = await mealsApi.assign(created.id, date);
      onassigned(assigned);
      inputValue = '';
      open = false;
    } finally {
      saving = false;
    }
  }

  function onKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter') {
      e.preventDefault();
      const exact = ideas.find((m) => m.title.toLowerCase() === inputValue.trim().toLowerCase());
      if (exact) {
        selectIdea(exact);
      } else if (inputValue.trim()) {
        createAndAssign();
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
      {#each filtered as idea (idea.id)}
        <li>
          <button
            class="dropdown-item"
            onmousedown={(e) => { e.preventDefault(); selectIdea(idea); }}
          >
            {idea.title}
          </button>
        </li>
      {/each}
      {#if inputValue.trim() && !ideas.some((m) => m.title.toLowerCase() === inputValue.trim().toLowerCase())}
        <li>
          <button
            class="dropdown-item new-item"
            onmousedown={(e) => { e.preventDefault(); createAndAssign(); }}
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
