<script lang="ts">
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
  import { browser } from '$app/environment';
  import { authStore, isAuthenticated } from '$lib/stores/auth';
  import '../app.css';

  let { children } = $props();

  const PUBLIC_ROUTES = ['/login', '/register'];

  $effect(() => {
    if (browser && !$isAuthenticated && !PUBLIC_ROUTES.some((r) => $page.url.pathname.startsWith(r))) {
      goto('/login');
    }
  });

  const nav = [
    { href: '/today',    label: 'Heute' },
    { href: '/week',     label: 'Woche' },
    { href: '/projects', label: 'Projekte' },
    { href: '/all',      label: 'Alles' },
  ];

  function isActive(href: string) {
    return $page.url.pathname.startsWith(href);
  }

  let isPublicRoute = $derived(PUBLIC_ROUTES.some((r) => $page.url.pathname.startsWith(r)));
</script>

{#if isPublicRoute}
  {@render children()}
{:else}
  <div class="app-shell">
    <aside class="sidebar">
      <div class="sidebar-brand">Haushalt</div>
      <nav class="sidebar-nav">
        {#each nav as item}
          <a href={item.href} class="sidebar-link" class:active={isActive(item.href)}>
            {item.label}
          </a>
        {/each}
      </nav>
      <div class="sidebar-footer">
        <button onclick={() => { authStore.logout(); goto('/login'); }}>Abmelden</button>
      </div>
    </aside>

    <main class="main-content">
      {@render children()}
    </main>

    <nav class="bottom-nav" aria-label="Hauptnavigation">
      {#each nav as item}
        <a href={item.href} class="bottom-nav-item" class:active={isActive(item.href)}>
          {item.label}
        </a>
      {/each}
    </nav>
  </div>
{/if}

<style>
  .app-shell {
    display: flex;
    min-height: 100dvh;
  }

  /* Sidebar – desktop only */
  .sidebar {
    display: none;
  }

  .main-content {
    flex: 1;
    min-width: 0;
    padding-bottom: var(--nav-height);
  }

  /* Bottom nav – mobile */
  .bottom-nav {
    display: flex;
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    height: var(--nav-height);
    background: var(--color-surface);
    border-top: var(--border-width) solid var(--color-border);
    z-index: 100;
  }

  .bottom-nav-item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.8125rem;
    font-weight: 500;
    color: var(--color-muted);
  }

  .bottom-nav-item.active {
    color: var(--color-text);
    font-weight: 700;
  }

  @media (min-width: 768px) {
    .sidebar {
      display: flex;
      flex-direction: column;
      width: var(--sidebar-width);
      background: var(--color-sidebar);
      border-right: var(--border-width) solid var(--color-border);
      position: sticky;
      top: 0;
      height: 100dvh;
      flex-shrink: 0;
    }

    .sidebar-brand {
      padding: 1.5rem 1.25rem 1rem;
      font-size: 1.125rem;
      font-weight: 700;
      border-bottom: var(--border-width) solid var(--color-border);
    }

    .sidebar-nav {
      flex: 1;
      display: flex;
      flex-direction: column;
      padding: 0.75rem 0;
    }

    .sidebar-link {
      padding: 0.625rem 1.25rem;
      font-size: 0.9375rem;
      font-weight: 500;
      color: var(--color-muted);
      border-left: 3px solid transparent;
    }

    .sidebar-link:hover {
      color: var(--color-text);
    }

    .sidebar-link.active {
      color: var(--color-text);
      font-weight: 700;
      border-left-color: var(--color-border);
      background: var(--color-divider);
    }

    .sidebar-footer {
      padding: 1rem 1.25rem;
      border-top: var(--border-width) solid var(--color-border);
    }

    .sidebar-footer button {
      font-size: 0.8125rem;
      color: var(--color-muted);
    }

    .sidebar-footer button:hover {
      color: var(--color-text);
    }

    .main-content {
      padding-bottom: 0;
    }

    .bottom-nav {
      display: none;
    }
  }
</style>
