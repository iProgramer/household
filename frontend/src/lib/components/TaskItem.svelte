<script lang="ts">
  import type { Task } from '$lib/api';
  import { taskAccent } from '$lib/utils/taskColor';
  import { membersStore, memberPalette } from '$lib/stores/members';

  let {
    task,
    projectTitle,
    oncomplete,
    onreopen,
    onunschedule,
    onedit,
    ondelete,
  }: {
    task: Task;
    projectTitle?: string;
    oncomplete?: () => void | Promise<void>;
    onreopen?: () => void | Promise<void>;
    onunschedule?: () => void | Promise<void>;
    onedit?: (newTitle: string, assignedTo: string | null | undefined) => Promise<void>;
    ondelete?: () => Promise<void>;
  } = $props();

  const accent = $derived(taskAccent(task.id));
  let busy = $state(false);
  let editing = $state(false);
  let editTitle = $state('');
  let editAssignedTo = $state<string | null | undefined>(undefined);
  let inputEl = $state<HTMLInputElement | null>(null);

  async function startEdit() {
    editTitle = task.title;
    editAssignedTo = task.assignedTo;
    editing = true;
    await Promise.resolve();
    inputEl?.focus();
    inputEl?.select();
  }

  async function saveEdit() {
    if (!editing) return;
    editing = false;
    const trimmed = editTitle.trim() || task.title;
    const titleChanged = trimmed !== task.title;
    const assignmentChanged = editAssignedTo !== task.assignedTo;
    if (titleChanged || assignmentChanged) {
      await onedit?.(trimmed, editAssignedTo);
    }
  }

  function handleEditKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter') { e.preventDefault(); saveEdit(); }
    if (e.key === 'Escape') { editing = false; }
  }

  const assignedMember = $derived(
    task.assignedTo ? $membersStore.find((m) => m.id === task.assignedTo) ?? null : null
  );
  const assignedPalette = $derived(
    assignedMember ? memberPalette($membersStore, assignedMember.id) : null
  );

  async function handleCheck() {
    if (busy) return;
    busy = true;
    try {
      if (task.status === 'DONE') {
        await onreopen?.();
      } else {
        await oncomplete?.();
      }
    } finally {
      busy = false;
    }
  }
</script>

<div
  class="task-item"
  class:done={task.status === 'DONE'}
  class:editing
  style="--accent-bg: {accent.bg}; --accent-border: {accent.border};"
>
  <div class="main-row">
    <button
      class="check"
      class:checked={task.status === 'DONE'}
      onclick={handleCheck}
      disabled={busy || (task.status === 'OPEN' && !oncomplete) || (task.status === 'DONE' && !onreopen)}
      aria-label={task.status === 'DONE' ? 'Aufgabe wieder öffnen' : 'Aufgabe abhaken'}
      title={task.status === 'DONE' ? 'Klicken zum Wiederherstellen' : ''}
    >
      {#if task.status === 'DONE'}✓{/if}
    </button>

    {#if editing}
      <input
        bind:this={inputEl}
        class="title-input"
        bind:value={editTitle}
        onblur={saveEdit}
        onkeydown={handleEditKeydown}
      />
    {:else}
      <span class="title" class:strikethrough={task.status === 'DONE'}>{task.title}</span>
    {/if}

    {#if !editing && projectTitle}
      <span class="project-badge" title={projectTitle}>{projectTitle}</span>
    {/if}

    {#if !editing && task.recurrence}
      <span class="badge" title="Wiederkehrend">↻</span>
    {/if}

    {#if !editing && assignedMember && assignedPalette}
      <span
        class="member-badge"
        style="--chip-color: {assignedPalette.color}; --chip-bg: {assignedPalette.bg};"
        title={assignedMember.email}
      >
        {assignedMember.email[0].toUpperCase()}
      </span>
    {/if}

    {#if onedit && task.status === 'OPEN'}
      <button class="edit-btn" onclick={startEdit} title="Bearbeiten" aria-label="Bearbeiten">
        <svg width="13" height="13" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M11 2l3 3-9 9H2v-3L11 2z"/>
        </svg>
      </button>
    {/if}

    {#if ondelete}
      <button class="delete-btn" onclick={ondelete} title="Löschen" aria-label="Aufgabe löschen">
        <svg width="13" height="13" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="2 4 14 4"/>
          <path d="M5 4V2h6v2"/>
          <path d="M3 4l1 10h8l1-10"/>
        </svg>
      </button>
    {/if}

    {#if onunschedule && task.date}
      <button class="unschedule-btn" onclick={onunschedule} title="Zurückstellen" aria-label="Zurückstellen">↩</button>
    {/if}
  </div>

  {#if editing && $membersStore.length > 0}
    <div class="edit-chips-row">
      {#each $membersStore as member}
        {@const palette = memberPalette($membersStore, member.id)}
        {@const selected = editAssignedTo === member.id}
        <button
          type="button"
          class="member-chip"
          class:selected
          style="--chip-color: {palette.color}; --chip-bg: {palette.bg};"
          onmousedown={(e) => e.preventDefault()}
          onclick={() => { editAssignedTo = selected ? null : member.id; }}
          title={member.email}
          aria-label={`Zuweisen an ${member.email}`}
        >
          {member.email[0].toUpperCase()}
        </button>
      {/each}
    </div>
  {/if}
</div>

<style>
  .task-item {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    padding: 0.75rem 1rem;
    background: var(--accent-bg);
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius);
    box-shadow: var(--shadow-card);
    margin-bottom: 0.5rem;
  }

  .task-item.done {
    opacity: 0.6;
  }

  .main-row {
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }

  .check {
    width: 22px;
    height: 22px;
    border-radius: 50%;
    border: var(--border-width) solid var(--color-border);
    background: var(--color-surface);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.7rem;
    flex-shrink: 0;
    cursor: pointer;
    padding: 0;
  }

  .check.checked {
    background: var(--color-border);
    color: var(--color-surface);
  }

  .check.checked:hover {
    background: var(--accent-rose);
    border-color: var(--accent-rose);
  }

  .check:disabled {
    cursor: default;
  }

  .title {
    flex: 1;
    font-size: 0.9375rem;
  }

  .strikethrough {
    text-decoration: line-through;
    color: var(--color-muted);
  }

  .project-badge {
    font-size: 0.6875rem;
    color: var(--color-muted);
    border: var(--border-width) solid var(--color-divider);
    border-radius: var(--border-radius-pill);
    padding: 0.1rem 0.45rem;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 8rem;
    flex-shrink: 0;
  }

  .badge {
    font-size: 0.75rem;
    color: var(--color-muted);
  }

  .title-input {
    flex: 1;
    font-size: 0.9375rem;
    border: none;
    border-bottom: var(--border-width) solid var(--color-border);
    background: transparent;
    outline: none;
    padding: 0;
    font-family: inherit;
  }

  .edit-btn {
    color: var(--color-muted);
    padding: 0.125rem 0.25rem;
    flex-shrink: 0;
    display: flex;
    align-items: center;
  }

  .edit-btn:hover {
    color: var(--color-text);
  }

  .delete-btn {
    color: var(--color-muted);
    padding: 0.125rem 0.25rem;
    flex-shrink: 0;
    display: flex;
    align-items: center;
  }

  .delete-btn:hover {
    color: var(--accent-rose);
  }

  .unschedule-btn {
    font-size: 0.875rem;
    color: var(--color-muted);
    padding: 0.125rem 0.25rem;
    flex-shrink: 0;
  }

  .unschedule-btn:hover {
    color: var(--color-text);
  }

  .edit-chips-row {
    display: flex;
    gap: 0.375rem;
    padding-left: calc(22px + 0.75rem);
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
  }

  .member-chip.selected {
    background: var(--chip-bg);
    box-shadow: 1px 1px 0 var(--chip-color);
  }

  .member-badge {
    width: 22px;
    height: 22px;
    border-radius: 50%;
    border: var(--border-width) solid var(--chip-color);
    background: var(--chip-bg);
    color: var(--chip-color);
    font-size: 0.6875rem;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }
</style>
