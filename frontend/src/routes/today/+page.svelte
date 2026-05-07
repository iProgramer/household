<script lang="ts">
  import { onMount } from 'svelte';
  import { fixedEvents as eventsApi, mealNotes, ApiError } from '$lib/api';
  import type { Task, FixedEvent, MealNote } from '$lib/api';
  import { authStore } from '$lib/stores/auth';
  import { tasks as tasksApi } from '$lib/api';
  import TaskItem from '$lib/components/TaskItem.svelte';
  import PersonAvatar from '$lib/components/PersonAvatar.svelte';
  import AddTaskForm from '$lib/components/AddTaskForm.svelte';
  import CreateFixedEventForm from '$lib/components/CreateFixedEventForm.svelte';
  import { isoDate, formatDate } from '$lib/utils/dates';

  let todayTasks = $state<Task[]>([]);
  let todayEvents = $state<FixedEvent[]>([]);
  let mealNote = $state<MealNote | null>(null);
  let loading = $state(true);
  let loadError = $state('');
  let mealInput = $state('');
  let savingMeal = $state(false);
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

  async function reopenTask(id: string) {
    await tasksApi.reopen(id);
    todayTasks = todayTasks.map((t) => (t.id === id ? { ...t, status: 'OPEN' } : t));
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
            <TaskItem {task} oncomplete={() => completeTask(task.id)} onreopen={() => reopenTask(task.id)} />
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
