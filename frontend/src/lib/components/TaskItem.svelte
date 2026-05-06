<script lang="ts">
  import type { Task } from '$lib/api';
  import { taskAccent } from '$lib/utils/taskColor';

  let {
    task,
    oncomplete,
  }: { task: Task; oncomplete?: () => void | Promise<void> } = $props();

  const accent = $derived(taskAccent(task.id));
  let completing = $state(false);

  async function handleComplete() {
    if (completing || task.status === 'DONE' || !oncomplete) return;
    completing = true;
    try {
      await oncomplete();
    } finally {
      completing = false;
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
    onclick={handleComplete}
    disabled={task.status === 'DONE' || completing}
    aria-label="Aufgabe abhaken"
  >
    {#if task.status === 'DONE'}✓{/if}
  </button>

  <span class="title" class:strikethrough={task.status === 'DONE'}>{task.title}</span>

  {#if task.recurrence}
    <span class="badge" title="Wiederkehrend">↻</span>
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

  .check:disabled {
    cursor: default;
    background: var(--color-border);
    color: var(--color-surface);
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
</style>
