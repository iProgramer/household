<script lang="ts">
  import { page } from '$app/stores';

  let { children } = $props();

  const nav = [
    { href: '/today',    label: 'Heute' },
    { href: '/week',     label: 'Woche' },
    { href: '/projects', label: 'Projekte' },
    { href: '/all',      label: 'Alles' },
  ];
</script>

<div class="app">
  <main>
    {@render children()}
  </main>

  <nav class="bottom-nav">
    {#each nav as item}
      <a href={item.href} class:active={$page.url.pathname.startsWith(item.href)}>
        {item.label}
      </a>
    {/each}
  </nav>
</div>

<style>
  .app {
    display: flex;
    flex-direction: column;
    min-height: 100dvh;
  }

  main {
    flex: 1;
    padding: 1rem;
  }

  .bottom-nav {
    display: flex;
    justify-content: space-around;
    padding: 0.75rem 0;
    border-top: 1px solid #e2e8f0;
    position: sticky;
    bottom: 0;
    background: white;
  }

  .bottom-nav a {
    text-decoration: none;
    color: #94a3b8;
    font-size: 0.875rem;
    padding: 0.25rem 0.75rem;
  }

  .bottom-nav a.active {
    color: #1e293b;
    font-weight: 600;
  }
</style>
