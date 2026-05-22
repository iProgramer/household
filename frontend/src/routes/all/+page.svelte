<script lang="ts">
  import { onMount } from 'svelte';
  import { tasks as tasksApi, ApiError } from '$lib/api';
  import type { Task, TaskStatus } from '$lib/api';
  import TaskItem from '$lib/components/TaskItem.svelte';
  import AddTaskForm from '$lib/components/AddTaskForm.svelte';
  import { formatShortDate } from '$lib/utils/dates';

  type Filter = 'all' | 'open' | 'done';

  let allTasks = $state<Task[]>([]);
  let loading = $state(true);
  let loadError = $state('');
  let filter = $state<Filter>('open');

  async function load() {
    loading = true;
    loadError = '';
    try {
      const [today, unplanned] = await Promise.all([
        tasksApi.today(),
        tasksApi.unplanned(),
      ]);
      // Merge deduped
      const seen = new Set<string>();
      const merged: Task[] = [];
      for (const t of [...today, ...unplanned]) {
        if (!seen.has(t.id)) { seen.add(t.id); merged.push(t); }
      }
      allTasks = merged;
    } catch (e) {
      loadError = e instanceof ApiError ? e.message : 'Fehler beim Laden';
    } finally {
      loading = false;
    }
  }

  async function completeTask(id: string) {
    await tasksApi.complete(id);
    allTasks = allTasks.map((t) => (t.id === id ? { ...t, status: 'DONE' } : t));
  }

  async function reopenTask(id: string) {
    await tasksApi.reopen(id);
    allTasks = allTasks.map((t) => (t.id === id ? { ...t, status: 'OPEN' } : t));
  }

  async function editTask(id: string, title: string) {
    const task = allTasks.find((t) => t.id === id);
    if (!task) return;
    const updated = await tasksApi.update(id, { date: task.date, assignedTo: task.assignedTo, title });
    allTasks = allTasks.map((t) => (t.id === id ? updated : t));
  }

  function sortTasks(tasks: Task[]) {
    return [...tasks].sort((a, b) => {
      if (a.date && b.date) return a.date.localeCompare(b.date);
      if (a.date) return -1;
      if (b.date) return 1;
      return 0;
    });
  }

  let filtered = $derived(
    sortTasks(
      filter === 'all' ? allTasks :
      filter === 'open' ? allTasks.filter((t) => t.status === 'OPEN') :
      allTasks.filter((t) => t.status === 'DONE')
    )
  );

  onMount(load);
</script>

<div class="page">
  <header class="page-header">
    <h1>Alles</h1>
  </header>

  <div class="filter-row">
    {#each (['open', 'all', 'done'] as Filter[]) as f}
      <button class="filter-btn" class:active={filter === f} onclick={() => { filter = f; }}>
        {f === 'open' ? 'Offen' : f === 'all' ? 'Alle' : 'Erledigt'}
      </button>
    {/each}
  </div>

  {#if loading}
    <div class="state-msg muted">Laden…</div>
  {:else if loadError}
    <div class="state-msg" style="color: var(--accent-rose)">{loadError}</div>
  {:else}
    <div class="tasks-list">
      {#each filtered as task (task.id)}
        <div class="task-meta-row">
          <TaskItem {task} oncomplete={() => completeTask(task.id)} onreopen={() => reopenTask(task.id)} onedit={(title) => editTask(task.id, title)} />
          {#if task.date}
            <span class="date-badge muted">{formatShortDate(task.date)}</span>
          {/if}
        </div>
      {/each}

      {#if filtered.length === 0}
        <p class="state-msg muted">Keine Aufgaben</p>
      {/if}
    </div>

    {#if filter !== 'done'}
      <AddTaskForm
        placeholder="+ Neue Aufgabe"
        oncreated={(task) => { allTasks = [...allTasks, task]; }}
      />
    {/if}
  {/if}
</div>

<style>
  .page {
    padding: 1.25rem 1rem 1rem;
    max-width: 640px;
    margin: 0 auto;
  }

  .page-header {
    margin-bottom: 1rem;
  }

  .filter-row {
    display: flex;
    gap: 0.375rem;
    margin-bottom: 1.25rem;
  }

  .filter-btn {
    padding: 0.375rem 0.875rem;
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-pill);
    background: var(--color-surface);
    font-size: 0.875rem;
    font-weight: 500;
    color: var(--color-muted);
    box-shadow: var(--shadow-card);
  }

  .filter-btn.active {
    background: var(--color-text);
    color: var(--color-surface);
    font-weight: 700;
  }

  .tasks-list {
    display: flex;
    flex-direction: column;
    gap: 0;
  }

  .task-meta-row {
    position: relative;
  }

  .date-badge {
    position: absolute;
    right: 0.75rem;
    top: 50%;
    transform: translateY(-50%);
    font-size: 0.75rem;
    pointer-events: none;
  }

  .state-msg {
    padding: 2rem 1rem;
    text-align: center;
  }
</style>
