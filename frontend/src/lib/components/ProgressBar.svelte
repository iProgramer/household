<script lang="ts">
  let {
    total,
    completed,
  }: { total: number; completed: number } = $props();

  let pct = $derived(total > 0 ? Math.round((completed / total) * 100) : 0);
</script>

<div class="progress-wrap">
  <div class="progress-bar" style="width: {pct}%;"></div>
</div>
<span class="progress-label muted">{completed}/{total}</span>

<style>
  .progress-wrap {
    flex: 1;
    height: 6px;
    background: var(--color-divider);
    border-radius: 99px;
    border: 1px solid var(--color-border);
    overflow: hidden;
  }

  .progress-bar {
    height: 100%;
    background: var(--accent-sage);
    border-radius: 99px;
    transition: width 0.3s ease;
  }

  .progress-label {
    white-space: nowrap;
    font-size: 0.8125rem;
  }
</style>
