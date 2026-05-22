<script lang="ts">
  import { onMount } from 'svelte';
  import { tasks as tasksApi, fixedEvents as eventsApi, meals as mealsApi, projects as projectsApi, ApiError } from '$lib/api';
  import type { Task, FixedEvent, Meal } from '$lib/api';
  import { authStore } from '$lib/stores/auth';
  import TaskItem from '$lib/components/TaskItem.svelte';
  import AddTaskForm from '$lib/components/AddTaskForm.svelte';
  import CreateFixedEventForm from '$lib/components/CreateFixedEventForm.svelte';
  import MealCombobox from '$lib/components/MealCombobox.svelte';
  import { isoDate, getWeekMonday, addDays, getISOWeek, formatWeekRange } from '$lib/utils/dates';

  const DAY_LABELS = ['Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa', 'So'];
  const DAY_FULL   = ['Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag', 'Sonntag'];

  const todayIso       = isoDate(new Date());
  const currentMonday  = getWeekMonday();

  let weekOffset = $state(0);
  let monday     = $derived(addDays(currentMonday, weekOffset * 7));
  let days       = $derived(Array.from({ length: 7 }, (_, i) => addDays(monday, i)));
  let mondayIso  = $derived(isoDate(monday));
  let isCurrentWeek = $derived(weekOffset === 0);

  let weekTasks         = $state<Task[]>([]);
  let weekEvents        = $state<FixedEvent[]>([]);
  let weekPlannedMeals  = $state<Meal[]>([]);
  let mealIdeas         = $state<Meal[]>([]);
  let unplannedTasks    = $state<Task[]>([]);
  let projectsMap       = $state<Map<string, string>>(new Map());
  let loading   = $state(true);
  let loadError = $state('');

  type ViewMode = 'day' | 'overview';
  let viewMode = $state<ViewMode>('day');

  let selectedDayIdx = $state(0);

  // When week changes: reset selected day (clamp to today if current week, else Monday)
  $effect(() => {
    if (isCurrentWeek) {
      const idx = days.findIndex((d) => isoDate(d) === todayIso);
      selectedDayIdx = idx >= 0 ? idx : 0;
    } else {
      selectedDayIdx = 0;
    }
  });

  let showEventForm      = $state(false);
  let overviewEventForms = $state<Record<string, boolean>>({});

  let currentMemberId = $state<string | null>(null);
  const unsubAuth = authStore.subscribe((s) => { currentMemberId = s.memberId; });

  async function load() {
    loading = true;
    loadError = '';
    showEventForm = false;
    overviewEventForms = {};
    try {
      const [wt, we, wm, ideas, ut, projs] = await Promise.all([
        tasksApi.week(mondayIso),
        eventsApi.week(mondayIso),
        mealsApi.forWeek(mondayIso),
        mealsApi.ideas(),
        tasksApi.unplanned(),
        projectsApi.list(),
      ]);
      weekTasks        = wt;
      weekEvents       = we;
      weekPlannedMeals = wm;
      mealIdeas        = ideas;
      unplannedTasks   = ut;
      projectsMap      = new Map(projs.map((p) => [p.id, p.title]));
    } catch (e) {
      loadError = e instanceof ApiError ? e.message : 'Fehler beim Laden';
    } finally {
      loading = false;
    }
  }

  // Reload whenever the week changes
  $effect(() => {
    mondayIso; // track dependency
    load();
  });

  function tasksForDay(dayIso: string)  { return weekTasks.filter((t) => t.date === dayIso); }
  function eventsForDay(dayIso: string) { return weekEvents.filter((e) => e.date === dayIso); }
  function mealsForDay(dayIso: string)  { return weekPlannedMeals.filter((m) => m.date === dayIso); }

  async function unassignMeal(id: string) {
    const unassigned = await mealsApi.unassign(id);
    weekPlannedMeals = weekPlannedMeals.filter((m) => m.id !== id);
    mealIdeas = [...mealIdeas, unassigned];
  }

  async function completeTask(id: string) {
    await tasksApi.complete(id);
    weekTasks      = weekTasks.map((t) => (t.id === id ? { ...t, status: 'DONE' } : t));
    unplannedTasks = unplannedTasks.map((t) => (t.id === id ? { ...t, status: 'DONE' } : t));
  }

  async function reopenTask(id: string) {
    await tasksApi.reopen(id);
    weekTasks      = weekTasks.map((t) => (t.id === id ? { ...t, status: 'OPEN' } : t));
    unplannedTasks = unplannedTasks.map((t) => (t.id === id ? { ...t, status: 'OPEN' } : t));
  }

  async function scheduleTask(id: string, dateIso: string) {
    const updated = await tasksApi.update(id, { date: dateIso });
    unplannedTasks = unplannedTasks.filter((t) => t.id !== id);
    weekTasks = [...weekTasks, updated];
  }

  async function unscheduleTask(id: string) {
    const updated = await tasksApi.update(id, { date: null });
    weekTasks = weekTasks.filter((t) => t.id !== id);
    unplannedTasks = [...unplannedTasks, updated];
  }

  async function editTask(id: string, title: string) {
    const task = weekTasks.find((t) => t.id === id) ?? unplannedTasks.find((t) => t.id === id);
    if (!task) return;
    const updated = await tasksApi.update(id, { date: task.date, assignedTo: task.assignedTo, title });
    weekTasks = weekTasks.map((t) => (t.id === id ? updated : t));
    unplannedTasks = unplannedTasks.map((t) => (t.id === id ? updated : t));
  }

  async function deleteTask(id: string) {
    await tasksApi.delete(id);
    weekTasks = weekTasks.filter((t) => t.id !== id);
    unplannedTasks = unplannedTasks.filter((t) => t.id !== id);
  }

  let selectedDayIso   = $derived(isoDate(days[selectedDayIdx]));
  let selectedTasks    = $derived(tasksForDay(selectedDayIso));
  let selectedEvents   = $derived(eventsForDay(selectedDayIso));
  let selectedMeals    = $derived(mealsForDay(selectedDayIso));

  onMount(() => unsubAuth);
</script>

<div class="page">
  <header class="page-header">
    <h1>Woche</h1>
    <div class="view-toggle">
      <button class:active={viewMode === 'day'}      onclick={() => { viewMode = 'day'; }}>Tag</button>
      <button class:active={viewMode === 'overview'} onclick={() => { viewMode = 'overview'; }}>Übersicht</button>
    </div>
  </header>

  <div class="week-nav">
    <button class="week-nav-btn" onclick={() => { weekOffset -= 1; }} aria-label="Vorherige Woche">‹</button>
    <div class="week-nav-label">
      <span class="week-kw" class:current={isCurrentWeek}>KW {getISOWeek(monday)}</span>
      <span class="week-range muted">{formatWeekRange(monday)}</span>
    </div>
    {#if !isCurrentWeek}
      <button class="week-today-btn muted" onclick={() => { weekOffset = 0; }}>Heute</button>
    {/if}
    <button class="week-nav-btn" onclick={() => { weekOffset += 1; }} aria-label="Nächste Woche">›</button>
  </div>

  {#if loading}
    <div class="state-msg muted">Laden…</div>
  {:else if loadError}
    <div class="state-msg" style="color: var(--accent-rose)">{loadError}</div>

  {:else if viewMode === 'day'}
    <!-- ── TAG-ANSICHT ─────────────────────────────────────── -->
    <div class="day-strip">
      {#each days as day, i}
        {@const iso = isoDate(day)}
        {@const count = tasksForDay(iso).filter(t => t.status === 'OPEN').length}
        <button
          class="day-btn"
          class:active={selectedDayIdx === i}
          class:today={iso === todayIso}
          onclick={() => { selectedDayIdx = i; showEventForm = false; }}
        >
          <span class="day-label">{DAY_LABELS[i]}</span>
          <span class="day-num">{day.getDate()}</span>
          {#if count > 0}<span class="day-dot"></span>{/if}
        </button>
      {/each}
    </div>

    <div class="day-detail">
      <div class="detail-section-header">
        <span class="section-label" style="margin-bottom:0">Termine</span>
        <button class="add-section-btn" onclick={() => { showEventForm = !showEventForm; }}>
          {showEventForm ? '✕' : '+ Termin'}
        </button>
      </div>

      {#if showEventForm}
        <CreateFixedEventForm
          defaultDate={selectedDayIso}
          oncreated={(ev) => { weekEvents = [...weekEvents, ev]; showEventForm = false; }}
          oncancel={() => { showEventForm = false; }}
        />
      {/if}

      {#if selectedEvents.length > 0}
        <div class="events-row">
          {#each selectedEvents as event (event.id)}
            <span class="event-chip">{event.title}{#if event.recurrence} ↻{/if}</span>
          {/each}
        </div>
      {/if}

      {#if selectedMeals.length > 0}
        <div class="meal-chips-row">
          {#each selectedMeals as meal (meal.id)}
            <span class="meal-chip">
              {meal.title}
              <button class="meal-unassign" onclick={() => unassignMeal(meal.id)} title="Zurück in den Pool" aria-label="Entfernen">✕</button>
            </span>
          {/each}
        </div>
      {/if}
      <div class="meal-combobox-wrap">
        <MealCombobox
          date={selectedDayIso}
          ideas={mealIdeas}
          onassigned={(meal) => { weekPlannedMeals = [...weekPlannedMeals, meal]; mealIdeas = mealIdeas.filter((m) => m.id !== meal.id); }}
          onideacreated={(idea) => { mealIdeas = [...mealIdeas, idea]; }}
        />
      </div>

      <div class="tasks-list">
        {#each selectedTasks as task (task.id)}
          <TaskItem {task} projectTitle={task.projectId ? projectsMap.get(task.projectId) : undefined} oncomplete={() => completeTask(task.id)} onreopen={() => reopenTask(task.id)} onunschedule={() => unscheduleTask(task.id)} onedit={(title) => editTask(task.id, title)} ondelete={() => deleteTask(task.id)} />
        {/each}
        <AddTaskForm
          defaultDate={selectedDayIso}
          oncreated={(task) => { weekTasks = [...weekTasks, task]; }}
        />
      </div>
    </div>

    {#if unplannedTasks.length > 0}
      <section class="section">
        <p class="section-label">Nicht eingeplant</p>
        {#each unplannedTasks as task (task.id)}
          <div class="unplanned-row">
            <TaskItem {task} projectTitle={task.projectId ? projectsMap.get(task.projectId) : undefined} oncomplete={() => completeTask(task.id)} onreopen={() => reopenTask(task.id)} onedit={(title) => editTask(task.id, title)} ondelete={() => deleteTask(task.id)} />
            <button class="schedule-btn" title="Auf ausgewählten Tag einplanen"
              onclick={() => scheduleTask(task.id, selectedDayIso)}>→</button>
          </div>
        {/each}
      </section>
    {/if}

  {:else}
    <!-- ── ÜBERSICHT ───────────────────────────────────────── -->
    {#each days as day, i}
      {@const iso = isoDate(day)}
      {@const dayTasks  = tasksForDay(iso)}
      {@const dayEvents = eventsForDay(iso)}
      {@const dayMeals  = mealsForDay(iso)}
      {@const isEmpty   = dayTasks.length === 0 && dayEvents.length === 0 && dayMeals.length === 0}

      <div class="agenda-day" class:today={iso === todayIso} class:empty={isEmpty}>
        <div class="agenda-day-header">
          <div class="agenda-day-label">
            <span class="agenda-weekday">{DAY_FULL[i]}</span>
            <span class="agenda-date muted">{day.getDate()}.{day.getMonth() + 1}.</span>
          </div>
          <div class="agenda-day-actions">
            <button
              class="add-section-btn"
              onclick={() => { overviewEventForms = { ...overviewEventForms, [iso]: !overviewEventForms[iso] }; }}
            >
              {overviewEventForms[iso] ? '✕' : '+ Termin'}
            </button>
          </div>
        </div>

        {#if overviewEventForms[iso]}
          <CreateFixedEventForm
            defaultDate={iso}
            oncreated={(ev) => { weekEvents = [...weekEvents, ev]; overviewEventForms = { ...overviewEventForms, [iso]: false }; }}
            oncancel={() => { overviewEventForms = { ...overviewEventForms, [iso]: false }; }}
          />
        {/if}

        {#if dayEvents.length > 0}
          <div class="events-row">
            {#each dayEvents as event (event.id)}
              <span class="event-chip">{event.title}{#if event.recurrence} ↻{/if}</span>
            {/each}
          </div>
        {/if}

        {#if dayMeals.length > 0}
          <div class="meal-chips-row">
            {#each dayMeals as meal (meal.id)}
              <span class="meal-chip">
                {meal.title}
                <button class="meal-unassign" onclick={() => unassignMeal(meal.id)} title="Zurück in den Pool" aria-label="Entfernen">✕</button>
              </span>
            {/each}
          </div>
        {/if}

        {#each dayTasks as task (task.id)}
          <TaskItem {task} projectTitle={task.projectId ? projectsMap.get(task.projectId) : undefined} oncomplete={() => completeTask(task.id)} onreopen={() => reopenTask(task.id)} onunschedule={() => unscheduleTask(task.id)} onedit={(title) => editTask(task.id, title)} ondelete={() => deleteTask(task.id)} />
        {/each}

        <AddTaskForm
          defaultDate={iso}
          placeholder="+ Aufgabe"
          oncreated={(task) => { weekTasks = [...weekTasks, task]; }}
        />
      </div>
    {/each}

    {#if unplannedTasks.length > 0}
      <section class="section" style="margin-top: 0.5rem;">
        <p class="section-label">Nicht eingeplant</p>
        {#each unplannedTasks as task (task.id)}
          <div class="unplanned-row">
            <TaskItem {task} projectTitle={task.projectId ? projectsMap.get(task.projectId) : undefined} oncomplete={() => completeTask(task.id)} onreopen={() => reopenTask(task.id)} onedit={(title) => editTask(task.id, title)} ondelete={() => deleteTask(task.id)} />
          </div>
        {/each}
      </section>
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
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 1.25rem;
  }

  /* Week navigation */
  .week-nav {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 1.25rem;
  }

  .week-nav-btn {
    font-size: 1.25rem;
    line-height: 1;
    padding: 0.25rem 0.5rem;
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    background: var(--color-surface);
    box-shadow: var(--shadow-card);
    color: var(--color-text);
  }

  .week-nav-label {
    flex: 1;
    display: flex;
    align-items: baseline;
    gap: 0.5rem;
  }

  .week-kw {
    font-size: 0.9375rem;
    font-weight: 700;
  }

  .week-kw.current {
    color: var(--accent-sage);
  }

  .week-range {
    font-size: 0.8125rem;
  }

  .week-today-btn {
    font-size: 0.8125rem;
    padding: 0.25rem 0.625rem;
    border: var(--border-width) solid var(--color-divider);
    border-radius: var(--border-radius-pill);
    background: var(--color-surface);
  }

  /* View toggle */
  .view-toggle {
    display: flex;
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    overflow: hidden;
    box-shadow: var(--shadow-card);
  }

  .view-toggle button {
    padding: 0.375rem 0.875rem;
    font-size: 0.875rem;
    font-weight: 500;
    color: var(--color-muted);
    border-right: var(--border-width) solid var(--color-border);
  }

  .view-toggle button:last-child { border-right: none; }

  .view-toggle button.active {
    background: var(--color-text);
    color: var(--color-surface);
    font-weight: 700;
  }

  /* Day strip */
  .day-strip {
    display: flex;
    gap: 0.25rem;
    margin-bottom: 1.25rem;
    overflow-x: auto;
    padding-bottom: 0.25rem;
  }

  .day-btn {
    flex: 1;
    min-width: 40px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.2rem;
    padding: 0.5rem 0.25rem;
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    background: var(--color-surface);
    box-shadow: var(--shadow-card);
    cursor: pointer;
  }

  .day-btn.active { background: var(--color-text); color: var(--color-surface); }
  .day-btn.today:not(.active) { border-color: var(--accent-sage); background: var(--accent-sage-bg); }

  .day-label { font-size: 0.6875rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.04em; }
  .day-num   { font-size: 1rem; font-weight: 700; }

  .day-dot { width: 5px; height: 5px; border-radius: 50%; background: var(--accent-rose); }
  .day-btn.active .day-dot { background: var(--color-surface); }

  /* Day detail (tag view) */
  .day-detail { margin-bottom: 1.75rem; }

  .detail-section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 0.5rem;
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

  .add-section-btn:hover { color: var(--color-text); border-color: var(--color-border); }

  .events-row { display: flex; flex-wrap: wrap; gap: 0.375rem; margin-bottom: 0.625rem; }

  .event-chip {
    padding: 0.25rem 0.625rem;
    background: var(--accent-amber-bg);
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-pill);
    font-size: 0.8125rem;
    font-weight: 500;
  }

  .meal-chips-row { display: flex; flex-wrap: wrap; gap: 0.375rem; margin-bottom: 0.5rem; }

  .meal-chip {
    display: inline-flex;
    align-items: center;
    gap: 0.375rem;
    padding: 0.25rem 0.625rem;
    background: var(--accent-lilac-bg);
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-pill);
    font-size: 0.8125rem;
    font-weight: 500;
  }

  .meal-unassign {
    color: var(--color-muted);
    font-size: 0.7rem;
    line-height: 1;
  }

  .meal-unassign:hover { color: var(--accent-rose); }

  .meal-combobox-wrap { margin-bottom: 0.75rem; }

  .tasks-list { display: flex; flex-direction: column; }

  /* Agenda / overview */
  .agenda-day {
    border-left: 3px solid var(--color-divider);
    padding: 0.625rem 0 0.625rem 1rem;
    margin-bottom: 0.25rem;
  }

  .agenda-day.today {
    border-left-color: var(--accent-sage);
  }

  .agenda-day.empty {
    opacity: 0.5;
  }

  .agenda-day-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 0.5rem;
  }

  .agenda-day-label {
    display: flex;
    align-items: baseline;
    gap: 0.5rem;
  }

  .agenda-weekday {
    font-weight: 700;
    font-size: 0.9375rem;
  }

  .agenda-date {
    font-size: 0.8125rem;
  }

  /* Shared */
  .section { margin-bottom: 1.75rem; }

  .unplanned-row {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 0.5rem;
  }

  .unplanned-row :global(.task-item) { flex: 1; margin-bottom: 0; }

  .schedule-btn {
    padding: 0.375rem 0.625rem;
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    background: var(--color-surface);
    font-size: 0.875rem;
    box-shadow: var(--shadow-card);
    flex-shrink: 0;
  }

  .state-msg { padding: 3rem 1rem; text-align: center; }
</style>
