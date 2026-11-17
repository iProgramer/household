<script lang="ts">
  import { onMount } from 'svelte';
  import { recipes as recipesApi, plannedMeals as plannedMealsApi, projects as projectsApi, ApiError } from '$lib/api';
  import type { Task, Recipe, PlannedMeal } from '$lib/api';
  import { authStore } from '$lib/stores/auth';
  import { tasks as tasksApi } from '$lib/api';
  import TaskItem from '$lib/components/TaskItem.svelte';
  import PersonAvatar from '$lib/components/PersonAvatar.svelte';
  import AddTaskForm from '$lib/components/AddTaskForm.svelte';
  import MealCombobox from '$lib/components/MealCombobox.svelte';
  import { isoDate, formatDate } from '$lib/utils/dates';

  let todayTasks = $state<Task[]>([]);
  let overdueTasks = $state<Task[]>([]);
  let todayMeal = $state<PlannedMeal | null>(null);
  let recipeList = $state<Recipe[]>([]);
  let projectsMap = $state<Map<string, string>>(new Map());
  let loading = $state(true);
  let loadError = $state('');

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
      const [t, o, meal, recs, projs] = await Promise.all([
        tasksApi.today(),
        tasksApi.overdue(),
        plannedMealsApi.forDate(todayIso),
        recipesApi.list(),
        projectsApi.list(),
      ]);
      todayTasks = t;
      overdueTasks = o;
      todayMeal = meal;
      recipeList = recs;
      projectsMap = new Map(projs.map((p) => [p.id, p.title]));
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

  async function editTask(id: string, title: string, assignedTo: string | null | undefined) {
    const task = todayTasks.find((t) => t.id === id) ?? overdueTasks.find((t) => t.id === id);
    if (!task) return;
    const updated = await tasksApi.update(id, { date: task.date, assignedTo: assignedTo ?? null, title });
    todayTasks = todayTasks.map((t) => (t.id === id ? updated : t));
    overdueTasks = overdueTasks.map((t) => (t.id === id ? updated : t));
  }

  async function deleteTask(id: string) {
    await tasksApi.delete(id);
    todayTasks = todayTasks.filter((t) => t.id !== id);
    overdueTasks = overdueTasks.filter((t) => t.id !== id);
  }

  async function completeOverdue(id: string) {
    await tasksApi.complete(id);
    overdueTasks = overdueTasks.map((t) => (t.id === id ? { ...t, status: 'DONE' } : t));
  }

  async function unplanMeal(id: string) {
    await plannedMealsApi.unplan(id);
    todayMeal = null;
  }

  async function toggleMealFavorite(id: string) {
    const result = await plannedMealsApi.toggleFavorite(id);
    if (todayMeal) {
      todayMeal = { ...todayMeal, recipeId: result.recipeId, favorite: result.favorite };
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
    {#if overdueTasks.filter(t => t.status === 'OPEN').length > 0}
      <section class="section overdue-section">
        <p class="section-label overdue-label">Überfällig</p>
        {#each overdueTasks.filter(t => t.status === 'OPEN') as task (task.id)}
          <TaskItem {task} projectTitle={task.projectId ? projectsMap.get(task.projectId) : undefined} oncomplete={() => completeOverdue(task.id)} onunschedule={() => unscheduleTask(task.id)} onedit={(title, assignedTo) => editTask(task.id, title, assignedTo)} ondelete={() => deleteTask(task.id)} />
        {/each}
      </section>
    {/if}

    <section class="section">
      <p class="section-label">Aufgaben</p>

      {#each openTasks as task (task.id)}
        <TaskItem {task} projectTitle={task.projectId ? projectsMap.get(task.projectId) : undefined} oncomplete={() => completeTask(task.id)} onreopen={() => reopenTask(task.id)} onunschedule={() => unscheduleTask(task.id)} onedit={(title, assignedTo) => editTask(task.id, title, assignedTo)} ondelete={() => deleteTask(task.id)} />
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
      {#if todayMeal}
        <div class="meal-chip card">
          <button
            class="star-btn"
            class:active={todayMeal.favorite === true}
            onclick={() => toggleMealFavorite(todayMeal!.id)}
            aria-label={todayMeal.favorite ? 'Favorit entfernen' : 'Als Favorit markieren'}
          >{todayMeal.favorite ? '★' : '☆'}</button>
          <span>{todayMeal.title}</span>
          <button class="meal-unassign" onclick={() => unplanMeal(todayMeal!.id)} title="Entplanen" aria-label="Entfernen">✕</button>
        </div>
      {:else}
        <MealCombobox
          date={todayIso}
          recipes={recipeList}
          onplanned={(meal) => { todayMeal = meal; }}
        />
      {/if}
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

  .meal-chip {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.5rem 0.75rem;
    background: var(--accent-lilac-bg);
    font-size: 0.9375rem;
  }

  .meal-chip span { flex: 1; }

  .star-btn {
    font-size: 1.125rem;
    line-height: 1;
    color: var(--color-muted);
    padding: 0;
    flex-shrink: 0;
  }

  .star-btn.active {
    color: var(--accent-amber);
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
