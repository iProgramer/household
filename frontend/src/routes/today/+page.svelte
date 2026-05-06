<script lang="ts">
  import { onMount } from 'svelte';
  import { tasks as tasksApi, fixedEvents as eventsApi, mealNotes, ApiError } from '$lib/api';
  import type { Task, FixedEvent, MealNote } from '$lib/api';
  import { authStore } from '$lib/stores/auth';
  import TaskItem from '$lib/components/TaskItem.svelte';
  import PersonAvatar from '$lib/components/PersonAvatar.svelte';
  import { isoDate, formatDate } from '$lib/utils/dates';

  let todayTasks = $state<Task[]>([]);
  let todayEvents = $state<FixedEvent[]>([]);
  let mealNote = $state<MealNote | null>(null);
  let loading = $state(true);
  let loadError = $state('');
  let newTaskTitle = $state('');
  let addingTask = $state(false);
  let mealInput = $state('');
  let savingMeal = $state(false);

  const today = new Date();
  const todayIso = isoDate(today);
  const dateLabel = formatDate(today);

  let currentMemberId = $state<string | null>(null);
  let currentEmail = $state<string | null>(null);

  const unsubAuth = authStore.subscribe((s) => {
    currentMemberId = s.memberId;
    currentEmail = s.email;
  });

  async function load() {
    loading = true;
    loadError = '';
    try {
      const [t, e, m] = await Promise.all([
        tasksApi.today(),
        eventsApi.today(),
        mealNotes.today(),
      ]);
      todayTasks = t;
      todayEvents = e;
      mealNote = m;
      mealInput = m?.note ?? '';
    } catch (e) {
      loadError = e instanceof ApiError ? e.message : 'Fehler beim Laden';
    } finally {
      loading = false;
    }
  }

  async function completeTask(id: string) {
    await tasksApi.complete(id);
    todayTasks = todayTasks.map((t) => (t.id === id ? { ...t, status: 'DONE' } : t));
  }

  async function addTask() {
    const title = newTaskTitle.trim();
    if (!title || addingTask) return;
    addingTask = true;
    try {
      const task = await tasksApi.create({ title, date: todayIso });
      todayTasks = [...todayTasks, task];
      newTaskTitle = '';
    } finally {
      addingTask = false;
    }
  }

  async function saveMeal() {
    const note = mealInput.trim();
    if (savingMeal || note === (mealNote?.note ?? '')) return;
    savingMeal = true;
    try {
      mealNote = await mealNotes.set(todayIso, note);
    } finally {
      savingMeal = false;
    }
  }

  let openTasks = $derived(todayTasks.filter((t) => t.status === 'OPEN'));
  let doneTasks = $derived(todayTasks.filter((t) => t.status === 'DONE'));

  onMount(() => {
    load();
    return unsubAuth;
  });
</script>

<div class="page">
  <header class="page-header">
    <div>
      <h1>Heute</h1>
      <p class="muted">{dateLabel}</p>
    </div>
    {#if currentMemberId}
      <PersonAvatar memberId={currentMemberId} {currentMemberId} email={currentEmail} size={36} />
    {/if}
  </header>

  {#if loading}
    <div class="state-msg muted">Laden…</div>
  {:else if loadError}
    <div class="state-msg" style="color: var(--accent-rose)">{loadError}</div>
  {:else}
    {#if todayEvents.length > 0}
      <section class="section">
        <p class="section-label">Termine</p>
        {#each todayEvents as event (event.id)}
          <div class="event-item card">
            <span class="event-dot"></span>
            <span>{event.title}</span>
            {#if event.recurrence}
              <span class="muted badge">↻</span>
            {/if}
          </div>
        {/each}
      </section>
    {/if}

    <section class="section">
      <p class="section-label">Aufgaben</p>

      {#each openTasks as task (task.id)}
        <div class="task-row">
          <div class="task-wrap">
            <TaskItem {task} oncomplete={() => completeTask(task.id)} />
          </div>
          {#if task.assignedTo}
            <PersonAvatar memberId={task.assignedTo} {currentMemberId} email={currentEmail} size={24} />
          {/if}
        </div>
      {/each}

      <form class="add-task" onsubmit={(e) => { e.preventDefault(); addTask(); }}>
        <input
          type="text"
          placeholder="+ Neue Aufgabe"
          bind:value={newTaskTitle}
          disabled={addingTask}
        />
        {#if newTaskTitle.trim()}
          <button type="submit" disabled={addingTask}>↵</button>
        {/if}
      </form>

      {#if doneTasks.length > 0}
        <details class="done-section">
          <summary class="muted">Erledigt ({doneTasks.length})</summary>
          {#each doneTasks as task (task.id)}
            <TaskItem {task} />
          {/each}
        </details>
      {/if}
    </section>

    <section class="section">
      <p class="section-label">Mahlzeit</p>
      <div class="meal-box card">
        <textarea
          rows="2"
          placeholder="Was gibt's heute?"
          bind:value={mealInput}
          onblur={saveMeal}
        ></textarea>
        {#if savingMeal}
          <span class="muted saving-label">Speichern…</span>
        {/if}
      </div>
    </section>
  {/if}
</div>

<style>
  .page {
    padding: 1.25rem 1rem 1rem;
    max-width: 640px;
    margin: 0 auto;
  }

  .page-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    margin-bottom: 1.5rem;
  }

  .section {
    margin-bottom: 1.75rem;
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

  .badge {
    margin-left: auto;
    font-size: 0.75rem;
  }

  .task-row {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 0.5rem;
  }

  .task-wrap {
    flex: 1;
    min-width: 0;
  }

  .task-wrap :global(.task-item) {
    margin-bottom: 0;
  }

  .add-task {
    display: flex;
    gap: 0.5rem;
    margin-top: 0.25rem;
  }

  .add-task input {
    flex: 1;
    border: var(--border-width) dashed var(--color-muted);
    border-radius: var(--border-radius-sm);
    padding: 0.625rem 0.75rem;
    background: transparent;
    outline: none;
    font-size: 0.9375rem;
    color: var(--color-muted);
  }

  .add-task input:focus {
    border-style: solid;
    border-color: var(--color-border);
    color: var(--color-text);
    background: var(--color-surface);
    box-shadow: var(--shadow-card);
  }

  .add-task button {
    padding: 0 0.875rem;
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    background: var(--color-text);
    color: var(--color-surface);
    font-size: 1rem;
    box-shadow: var(--shadow-card);
  }

  .done-section {
    margin-top: 0.75rem;
  }

  .done-section summary {
    cursor: pointer;
    font-size: 0.875rem;
    margin-bottom: 0.5rem;
    list-style: none;
  }

  .meal-box {
    padding: 0.75rem;
    background: var(--accent-lilac-bg);
  }

  .meal-box textarea {
    width: 100%;
    border: none;
    background: transparent;
    resize: none;
    outline: none;
    font-size: 0.9375rem;
    line-height: 1.5;
  }

  .saving-label {
    font-size: 0.75rem;
    display: block;
    text-align: right;
    margin-top: 0.25rem;
  }

  .state-msg {
    padding: 3rem 1rem;
    text-align: center;
  }
</style>
