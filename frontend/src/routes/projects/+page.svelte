<script lang="ts">
  import { onMount } from 'svelte';
  import { projects as projectsApi, tasks as tasksApi, ApiError } from '$lib/api';
  import type { Project, ProjectDetail } from '$lib/api';
  import TaskItem from '$lib/components/TaskItem.svelte';
  import ProgressBar from '$lib/components/ProgressBar.svelte';

  let projectList = $state<(Project & { expanded?: boolean })[]>([]);
  let detailCache = $state<Record<string, ProjectDetail>>({});
  let loading = $state(true);
  let loadError = $state('');

  let showCreateForm = $state(false);
  let newTitle = $state('');
  let newGoal = $state('');
  let creating = $state(false);

  let newTaskTitles = $state<Record<string, string>>({});
  let addingTask = $state<Record<string, boolean>>({});

  async function load() {
    loading = true;
    loadError = '';
    try {
      const list = await projectsApi.list();
      projectList = list.map((p) => ({ ...p }));
    } catch (e) {
      loadError = e instanceof ApiError ? e.message : 'Fehler beim Laden';
    } finally {
      loading = false;
    }
  }

  async function toggleExpand(id: string) {
    const idx = projectList.findIndex((p) => p.id === id);
    if (idx < 0) return;

    const isExpanded = !!(projectList[idx] as any).expanded;
    projectList = projectList.map((p, i) => i === idx ? { ...p, expanded: !isExpanded } : p);

    if (!isExpanded && !detailCache[id]) {
      try {
        detailCache = { ...detailCache, [id]: await projectsApi.get(id) };
      } catch {
        // ignore detail load errors
      }
    }
  }

  async function completeTask(taskId: string, projectId: string) {
    await tasksApi.complete(taskId);
    if (detailCache[projectId]) {
      detailCache = {
        ...detailCache,
        [projectId]: {
          ...detailCache[projectId],
          tasks: detailCache[projectId].tasks.map((t) =>
            t.id === taskId ? { ...t, status: 'DONE' } : t
          ),
        },
      };
    }
    projectList = projectList.map((p) => {
      if (p.id !== projectId) return p;
      const newCompleted = p.completedSteps + 1;
      return { ...p, completedSteps: newCompleted };
    });
  }

  async function addTask(projectId: string) {
    const title = (newTaskTitles[projectId] ?? '').trim();
    if (!title || addingTask[projectId]) return;
    addingTask = { ...addingTask, [projectId]: true };
    try {
      const task = await tasksApi.create({ title, projectId });
      if (detailCache[projectId]) {
        detailCache = {
          ...detailCache,
          [projectId]: {
            ...detailCache[projectId],
            tasks: [...detailCache[projectId].tasks, task],
          },
        };
      }
      projectList = projectList.map((p) =>
        p.id === projectId ? { ...p, totalSteps: p.totalSteps + 1 } : p
      );
      newTaskTitles = { ...newTaskTitles, [projectId]: '' };
    } finally {
      addingTask = { ...addingTask, [projectId]: false };
    }
  }

  async function completeProject(id: string) {
    await projectsApi.complete(id);
    projectList = projectList.filter((p) => p.id !== id);
  }

  async function createProject() {
    if (creating) return;
    const title = newTitle.trim();
    const goal = newGoal.trim();
    if (!title || !goal) return;
    creating = true;
    try {
      const project = await projectsApi.create({ title, goal });
      projectList = [...projectList, { ...project }];
      showCreateForm = false;
      newTitle = '';
      newGoal = '';
    } finally {
      creating = false;
    }
  }

  let activeProjects = $derived(projectList.filter((p) => p.status === 'ACTIVE'));
  let doneProjects = $derived(projectList.filter((p) => p.status === 'DONE'));

  onMount(load);
</script>

<div class="page">
  <header class="page-header">
    <h1>Projekte</h1>
    <button class="add-btn" onclick={() => { showCreateForm = !showCreateForm; }}>
      {showCreateForm ? '✕' : '+ Neu'}
    </button>
  </header>

  {#if showCreateForm}
    <div class="card create-form">
      <form onsubmit={(e) => { e.preventDefault(); createProject(); }}>
        <div class="field">
          <label for="proj-title">Name</label>
          <input id="proj-title" type="text" bind:value={newTitle} placeholder="Küche renovieren" required />
        </div>
        <div class="field">
          <label for="proj-goal">Ziel</label>
          <textarea id="proj-goal" rows="2" bind:value={newGoal} placeholder="Was soll am Ende erreicht sein?"></textarea>
        </div>
        <div class="form-actions">
          <button type="button" class="btn-secondary" onclick={() => { showCreateForm = false; }}>Abbrechen</button>
          <button type="submit" class="btn-primary" disabled={creating}>
            {creating ? '…' : 'Erstellen'}
          </button>
        </div>
      </form>
    </div>
  {/if}

  {#if loading}
    <div class="state-msg muted">Laden…</div>
  {:else if loadError}
    <div class="state-msg" style="color: var(--accent-rose)">{loadError}</div>
  {:else}
    {#if activeProjects.length === 0 && !showCreateForm}
      <p class="state-msg muted">Noch keine Projekte. Leg ein neues an!</p>
    {/if}

    {#each activeProjects as project (project.id)}
      <div class="project-card card" class:expanded={(project as any).expanded}>
        <button class="project-header" onclick={() => toggleExpand(project.id)}>
          <div class="project-title-row">
            <span class="project-title">{project.title}</span>
            <span class="chevron">{(project as any).expanded ? '▲' : '▼'}</span>
          </div>
          <p class="muted project-goal">{project.goal}</p>
          <div class="progress-row">
            <ProgressBar total={project.totalSteps} completed={project.completedSteps} />
          </div>
        </button>

        {#if (project as any).expanded && detailCache[project.id]}
          {@const detail = detailCache[project.id]}
          <div class="project-tasks">
            {#each detail.tasks as task (task.id)}
              <TaskItem {task} oncomplete={() => completeTask(task.id, project.id)} />
            {/each}

            <form class="add-task" onsubmit={(e) => { e.preventDefault(); addTask(project.id); }}>
              <input
                type="text"
                placeholder="+ Schritt hinzufügen"
                bind:value={newTaskTitles[project.id]}
                disabled={addingTask[project.id]}
              />
              {#if (newTaskTitles[project.id] ?? '').trim()}
                <button type="submit" disabled={addingTask[project.id]}>↵</button>
              {/if}
            </form>

            {#if project.totalSteps > 0 && project.completedSteps === project.totalSteps}
              <button class="complete-btn" onclick={() => completeProject(project.id)}>
                Projekt abschließen ✓
              </button>
            {/if}
          </div>
        {/if}
      </div>
    {/each}

    {#if doneProjects.length > 0}
      <details class="done-projects">
        <summary class="section-label" style="cursor: pointer; list-style: none;">
          Abgeschlossen ({doneProjects.length})
        </summary>
        {#each doneProjects as project (project.id)}
          <div class="project-card card done">
            <span class="project-title">{project.title}</span>
            <span class="muted" style="font-size: 0.8rem;">✓ Fertig</span>
          </div>
        {/each}
      </details>
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

  .add-btn {
    padding: 0.375rem 0.875rem;
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    background: var(--color-surface);
    font-weight: 700;
    font-size: 0.875rem;
    box-shadow: var(--shadow-card);
  }

  .create-form {
    padding: 1rem;
    margin-bottom: 1.25rem;
    background: var(--accent-sage-bg);
  }

  .field {
    display: flex;
    flex-direction: column;
    gap: 0.375rem;
    margin-bottom: 0.875rem;
  }

  label {
    font-size: 0.875rem;
    font-weight: 600;
  }

  input, textarea {
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    padding: 0.5rem 0.75rem;
    background: var(--color-surface);
    outline: none;
    width: 100%;
    resize: none;
  }

  input:focus, textarea:focus {
    box-shadow: var(--shadow-card);
  }

  .form-actions {
    display: flex;
    gap: 0.5rem;
    justify-content: flex-end;
  }

  .btn-primary {
    padding: 0.5rem 1rem;
    background: var(--color-text);
    color: var(--color-surface);
    border-radius: var(--border-radius-sm);
    font-weight: 700;
    font-size: 0.875rem;
    border: var(--border-width) solid var(--color-border);
    box-shadow: var(--shadow-card);
  }

  .btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }

  .btn-secondary {
    padding: 0.5rem 1rem;
    border: var(--border-width) solid var(--color-border);
    border-radius: var(--border-radius-sm);
    font-size: 0.875rem;
    background: var(--color-surface);
  }

  .project-card {
    margin-bottom: 0.75rem;
    overflow: hidden;
  }

  .project-card.done {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0.75rem 1rem;
    opacity: 0.6;
  }

  .project-header {
    width: 100%;
    text-align: left;
    padding: 1rem;
    display: flex;
    flex-direction: column;
    gap: 0.375rem;
    background: none;
    border: none;
    cursor: pointer;
  }

  .project-title-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .project-title {
    font-weight: 700;
    font-size: 1rem;
  }

  .chevron {
    font-size: 0.75rem;
    color: var(--color-muted);
  }

  .project-goal {
    font-size: 0.875rem;
    text-align: left;
  }

  .progress-row {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-top: 0.25rem;
  }

  .project-tasks {
    padding: 0.75rem 1rem 1rem;
    border-top: var(--border-width) solid var(--color-divider);
    display: flex;
    flex-direction: column;
  }

  .add-task {
    display: flex;
    gap: 0.5rem;
    margin-top: 0.5rem;
  }

  .add-task input {
    flex: 1;
    border: var(--border-width) dashed var(--color-muted);
    border-radius: var(--border-radius-sm);
    padding: 0.5rem 0.75rem;
    background: transparent;
    font-size: 0.875rem;
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
    font-size: 0.875rem;
    box-shadow: var(--shadow-card);
  }

  .complete-btn {
    margin-top: 0.75rem;
    padding: 0.5rem 1rem;
    background: var(--accent-sage-bg);
    border: var(--border-width) solid var(--accent-sage);
    border-radius: var(--border-radius-sm);
    font-size: 0.875rem;
    font-weight: 600;
    color: var(--color-person-a-text);
    box-shadow: 2px 2px 0 var(--accent-sage);
    align-self: flex-start;
  }

  .done-projects {
    margin-top: 0.5rem;
  }

  .state-msg {
    padding: 3rem 1rem;
    text-align: center;
  }
</style>
