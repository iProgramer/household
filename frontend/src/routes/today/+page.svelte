<script lang="ts">
  import { onMount } from 'svelte';
  import { fixedEvents as eventsApi, meals as mealsApi, ApiError } from '$lib/api';
  import type { Task, FixedEvent, Meal } from '$lib/api';
  import { authStore } from '$lib/stores/auth';
  import { tasks as tasksApi } from '$lib/api';
  import TaskItem from '$lib/components/TaskItem.svelte';
  import PersonAvatar from '$lib/components/PersonAvatar.svelte';
  import AddTaskForm from '$lib/components/AddTaskForm.svelte';
  import CreateFixedEventForm from '$lib/components/CreateFixedEventForm.svelte';
  import MealCombobox from '$lib/components/MealCombobox.svelte';
  import { isoDate, formatDate } from '$lib/utils/dates';

  let todayTasks = $state<Task[]>([]);
  let overdueTasks = $state<Task[]>([]);
  let todayEvents = $state<FixedEvent[]>([]);
  let todayMeals = $state<Meal[]>([]);
  let mealIdeas = $state<Meal[]>([]);
  let loading = $state(true);
  let loadError = $state('');
  let showEventForm = $state(false);

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
      const [t, o, e, m, ideas] = await Promise.all([
        tasksApi.today(),
        tasksApi.overdue(),
        eventsApi.today(),
        mealsApi.forDate(todayIso),
        mealsApi.ideas(),
      ]);
      todayTasks = t;
      overdueTasks = o;
      todayEvents = e;
      todayMeals = m;
      mealIdeas = ideas;
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

  async function reopenTask(id: string) {
    await tasksApi.reopen(id);
    todayTasks = todayTasks.map((t) => (t.id === id ? { ...t, status: 'OPEN' } : t));
  }

  async function unscheduleTask(id: string) {
    await tasksApi.update(id, { date: null });
    todayTasks = todayTasks.filter((t) => t.id !== id);
    overdueTasks = overdueTasks.filter((t) => t.id !== id);
  }

  async function editTask(id: string, title: string) {
    const task = todayTasks.find((t) => t.id === id) ?? overdueTasks.find((t) => t.id === id);
    if (!task) return;
    const updated = await tasksApi.update(id, { date: task.date, assignedTo: task.assignedTo, title });
    todayTasks = todayTasks.map((t) => (t.id === id ? updated : t));
    overdueTasks = overdueTasks.map((t) => (t.id === id ? updated : t));
  }

  async function completeOverdue(id: string) {
    await tasksApi.complete(id);
    overdueTasks = overdueTasks.map((t) => (t.id === id ? { ...t, status: 'DONE' } : t));
  }

  async function unassignMeal(id: string) {
    const unassigned = await mealsApi.unassign(id);
    todayMeals = todayMeals.filter((m) => m.id !== id);
    mealIdeas = [...mealIdeas, unassigned];
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
    {#if overdueTasks.filter(t => t.status === 'OPEN').length > 0}
      <section class="section overdue-section">
        <p class="section-label overdue-label">Überfällig</p>
        {#each overdueTasks.filter(t => t.status === 'OPEN') as task (task.id)}
          <div class="task-row">
            <div class="task-wrap">
              <TaskItem {task} oncomplete={() => completeOverdue(task.id)} onunschedule={() => unscheduleTask(task.id)} onedit={(title) => editTask(task.id, title)} />
            </div>
          </div>
        {/each}
      </section>
    {/if}

    <section class="section">
      <div class="section-header">
        <p class="section-label">Termine</p>
        <button class="add-section-btn" onclick={() => { showEventForm = !showEventForm; }}>
          {showEventForm ? '✕' : '+ Termin'}
        </button>
      </div>

      {#if showEventForm}
        <CreateFixedEventForm
          defaultDate={todayIso}
          oncreated={(ev) => { todayEvents = [...todayEvents, ev]; showEventForm = false; }}
          oncancel={() => { showEventForm = false; }}
        />
      {/if}

      {#each todayEvents as event (event.id)}
        <div class="event-item card">
          <span class="event-dot"></span>
          <span>{event.title}</span>
          {#if event.recurrence}
            <span class="muted badge">↻</span>
          {/if}
        </div>
      {/each}

      {#if todayEvents.length === 0 && !showEventForm}
        <p class="muted empty-hint">Keine Termine heute</p>
      {/if}
    </section>

    <section class="section">
      <p class="section-label">Aufgaben</p>

      {#each openTasks as task (task.id)}
        <div class="task-row">
          <div class="task-wrap">
            <TaskItem {task} oncomplete={() => completeTask(task.id)} onreopen={() => reopenTask(task.id)} onunschedule={() => unscheduleTask(task.id)} onedit={(title) => editTask(task.id, title)} />
          </div>
          {#if task.assignedTo}
            <PersonAvatar memberId={task.assignedTo} {currentMemberId} email={currentEmail} size={24} />
          {/if}
        </div>
      {/each}

      <AddTaskForm
        defaultDate={todayIso}
        oncreated={(task) => { todayTasks = [...todayTasks, task]; }}
      />

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
      <p class="section-label">Mahlzeiten</p>
      <div class="meal-chips">
        {#each todayMeals as meal (meal.id)}
          <div class="meal-chip card">
            <span>{meal.title}</span>
            <button class="meal-unassign" onclick={() => unassignMeal(meal.id)} title="Zurück in den Pool" aria-label="Entfernen">✕</button>
          </div>
        {/each}
      </div>
      <MealCombobox
        date={todayIso}
        ideas={mealIdeas}
        onassigned={(meal) => { todayMeals = [...todayMeals, meal]; mealIdeas = mealIdeas.filter((m) => m.id !== meal.id); }}
        onideacreated={(idea) => { mealIdeas = [...mealIdeas, idea]; }}
      />
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

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 0.5rem;
  }

  .section-header .section-label {
    margin-bottom: 0;
  }

  .add-section-btn {
    font-size: 0.8125rem;
    font-weight: 600;
    color: var(--color-muted);
    padding: 0.2rem 0.5rem;
    border: var(--border-width) solid var(--color-divider);
    border-radius: var(--border-radius-pill);
    background: var(--color-surface);
  }

  .add-section-btn:hover {
    color: var(--color-text);
    border-color: var(--color-border);
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

  .empty-hint {
    font-size: 0.875rem;
    padding: 0.25rem 0;
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

  .done-section {
    margin-top: 0.75rem;
  }

  .done-section summary {
    cursor: pointer;
    font-size: 0.875rem;
    margin-bottom: 0.5rem;
    list-style: none;
  }

  .overdue-section {
    border-left: 3px solid var(--accent-rose);
    padding-left: 0.75rem;
  }

  .overdue-label {
    color: var(--accent-rose);
  }

  .meal-chips {
    display: flex;
    flex-direction: column;
    gap: 0.375rem;
    margin-bottom: 0.5rem;
  }

  .meal-chip {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0.5rem 0.75rem;
    background: var(--accent-lilac-bg);
    font-size: 0.9375rem;
  }

  .meal-unassign {
    color: var(--color-muted);
    font-size: 0.75rem;
    padding: 0.125rem 0.25rem;
  }

  .meal-unassign:hover {
    color: var(--accent-rose);
  }

  .state-msg {
    padding: 3rem 1rem;
    text-align: center;
  }
</style>
