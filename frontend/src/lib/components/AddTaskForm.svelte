<script lang="ts">
  import { tasks as tasksApi, ApiError } from '$lib/api';
  import type { Task } from '$lib/api';
  import { membersStore, memberPalette } from '$lib/stores/members';

  let {
    defaultDate = '',
    projectId = undefined,
    placeholder = '+ Neue Aufgabe',
    oncreated,
  }: {
    defaultDate?: string;
    projectId?: string;
    placeholder?: string;
    oncreated: (task: Task) => void;
  } = $props();

  let title = $state('');
  let date = $state('');
  $effect.pre(() => { date = defaultDate; });
  let expanded = $state(false);
  let saving = $state(false);
  let error = $state('');
  let assignedTo = $state<string | undefined>(undefined);

  // Re-sync when switching days in week view (only if form isn't actively edited)
  $effect(() => {
    if (!expanded) date = defaultDate;
  });

  async function handleSubmit() {
    if (saving || !title.trim()) return;
    saving = true;
    error = '';
    try {
      const task = await tasksApi.create({
        title: title.trim(),
        ...(date ? { date } : {}),
        ...(assignedTo ? { assignedTo } : {}),
        ...(projectId ? { projectId } : {}),
      });
      oncreated(task);
      title = '';
      expanded = false;
      date = defaultDate;
      assignedTo = undefined;
    } catch (e) {
      error = e instanceof ApiError ? e.message : 'Fehler';
    } finally {
      saving = false;
    }
  }
</script>

<form class="add-task" onsubmit={(e) => { e.preventDefault(); handleSubmit(); }}>
  <div class="main-row">
    <input
      type="text"
      {placeholder}
      bind:value={title}
      disabled={saving}
      onfocus={() => { expanded = true; }}
    />
    {#if title.trim()}
      <button type="submit" class="submit-btn" disabled={saving}>↵</button>
    {/if}
  </div>

  {#if expanded}
    <div class="extra-row">
      <label class="date-label">
        <span class="muted">Datum</span>
        <input type="date" bind:value={date} class="date-input" />
      </label>

      {#if $membersStore.length > 0}
        <div class="member-chips">
          {#each $membersStore as member}
            {@const palette = memberPalette($membersStore, member.id)}
            {@const selected = assignedTo === member.id}
            <button
              type="button"
              class="member-chip"
              class:selected
              style="--chip-color: {palette.color}; --chip-bg: {palette.bg};"
              onclick={() => { assignedTo = selected ? undefined : member.id; }}
              title={member.email}
              aria-label={`Zuweisen an ${member.email}`}
            >
              {member.email[0].toUpperCase()}
            </button>
          {/each}
        </div>
      {/if}

      {#if !title.trim()}
        <button type="button" class="cancel-btn muted" onclick={() => { expanded = false; date = defaultDate; assignedTo = undefined; }}>
          ✕
        </button>
      {/if}
    </div>
  {/if}

  {#if error}
    <p class="error-msg">{error}</p>
  {/if}
</form>

<style>
  .add-task {
    display: flex;
    flex-direction: column;
    gap: 0.375rem;
    margin-top: 0.375rem;
  }

  .main-row {
    display: flex;
    gap: 0.5rem;
  }

  .main-row input {
    flex: 1;
    border: var(--border-width) dashed var(--color-muted);
    border-radius: var(--border-radius-sm);
    padding: 0.625rem 0.75rem;
    background: transparent;
    outline: none;
    font-size: 0.9375rem;
    color: var(--color-muted);
  }

  .main-row input:focus {
    border-style: solid;
    border-color: var(--color-border);
    color: var(--color-text);
    background: var(--color-surface);
    box-shadow: var(--shadow-card);
  }

  .submit-btn {
    padding: 0 0.875rem;
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    background: var(--color-text);
    color: var(--color-surface);
    font-size: 1rem;
    box-shadow: var(--shadow-card);
  }

  .extra-row {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.375rem 0.625rem;
    background: var(--color-surface);
    border: var(--border-width) solid var(--color-divider);
    border-radius: var(--border-radius-sm);
  }

  .date-label {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    flex: 1;
    font-size: 0.875rem;
  }

  .date-input {
    border: var(--border-width) solid var(--color-divider);
    border-radius: var(--border-radius-sm);
    padding: 0.25rem 0.5rem;
    font-size: 0.875rem;
    font-family: var(--font);
    background: var(--color-bg);
    outline: none;
  }

  .member-chips {
    display: flex;
    gap: 0.25rem;
  }

  .member-chip {
    width: 26px;
    height: 26px;
    border-radius: 50%;
    border: var(--border-width) solid var(--chip-color);
    background: transparent;
    color: var(--chip-color);
    font-size: 0.75rem;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    padding: 0;
    transition: background 0.1s;
  }

  .member-chip.selected {
    background: var(--chip-bg);
    box-shadow: 1px 1px 0 var(--chip-color);
  }

  .cancel-btn {
    font-size: 0.875rem;
    padding: 0.25rem;
  }

  .error-msg {
    color: var(--accent-rose);
    font-size: 0.8125rem;
  }
</style>
