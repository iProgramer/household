<script lang="ts">
  import type { Task } from '$lib/api';
  import { taskAccent } from '$lib/utils/taskColor';
  import { membersStore, memberPalette } from '$lib/stores/members';

  let {
    task,
    oncomplete,
    onreopen,
  }: {
    task: Task;
    oncomplete?: () => void | Promise<void>;
    onreopen?: () => void | Promise<void>;
  } = $props();

  const accent = $derived(taskAccent(task.id));
  let busy = $state(false);

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
  style="--accent-bg: {accent.bg}; --accent-border: {accent.border};"
>
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

  <span class="title" class:strikethrough={task.status === 'DONE'}>{task.title}</span>

  {#if task.recurrence}
    <span class="badge" title="Wiederkehrend">↻</span>
  {/if}

  {#if assignedMember && assignedPalette}
    <span
      class="member-badge"
      style="--chip-color: {assignedPalette.color}; --chip-bg: {assignedPalette.bg};"
      title={assignedMember.email}
    >
      {assignedMember.email[0].toUpperCase()}
    </span>
  {/if}
</div>

<style>
  .task-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
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

  .badge {
    font-size: 0.75rem;
    color: var(--color-muted);
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
