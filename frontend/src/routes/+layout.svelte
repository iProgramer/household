<script lang="ts">
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
  import { browser } from '$app/environment';
  import { authStore, isAuthenticated } from '$lib/stores/auth';
  import { membersStore } from '$lib/stores/members';
  import '../app.css';

  let { children } = $props();

  const PUBLIC_ROUTES = ['/login', '/register'];

  $effect(() => {
    if (browser && !$isAuthenticated && !PUBLIC_ROUTES.some((r) => $page.url.pathname.startsWith(r))) {
      goto('/login');
    }
  });

  $effect(() => {
    if ($isAuthenticated) {
      membersStore.load();
    } else {
      membersStore.clear();
    }
  });

  const nav = [
    {
      href: '/today',
      label: 'Heute',
      icon: `<svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <rect x="1" y="1" width="6" height="6" rx="1"/>
        <rect x="9" y="1" width="6" height="6" rx="1"/>
        <rect x="1" y="9" width="6" height="6" rx="1"/>
        <rect x="9" y="9" width="6" height="6" rx="1"/>
      </svg>`,
    },
    {
      href: '/week',
      label: 'Woche',
      icon: `<svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <rect x="1" y="8" width="3" height="7" rx="0.5"/>
        <rect x="6" y="4" width="3" height="11" rx="0.5"/>
        <rect x="11" y="1" width="3" height="14" rx="0.5"/>
      </svg>`,
    },
    {
      href: '/projects',
      label: 'Projekte',
      icon: `<svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <line x1="2" y1="5" x2="14" y2="5"/>
        <line x1="2" y1="8" x2="14" y2="8"/>
        <line x1="2" y1="11" x2="9" y2="11"/>
      </svg>`,
    },
    {
      href: '/all',
      label: 'Alle Aufgaben',
      icon: `<svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="8" cy="8" r="7"/>
        <line x1="8" y1="4" x2="8" y2="12"/>
        <line x1="4" y1="8" x2="12" y2="8"/>
      </svg>`,
    },
  ];

  const bottomNav = nav.map((item, i) => ({
    ...item,
    label: ['Heute', 'Woche', 'Projekte', 'Alles'][i],
  }));

  function isActive(href: string) {
    return $page.url.pathname.startsWith(href);
  }

  const monthLabel = new Date().toLocaleDateString('de-DE', { month: 'long', year: 'numeric' });

  let isPublicRoute = $derived(PUBLIC_ROUTES.some((r) => $page.url.pathname.startsWith(r)));
</script>

{#if isPublicRoute}
  {@render children()}
{:else}
  <div class="app-shell">
    <aside class="sidebar">
      <div class="sidebar-brand">
        <span class="brand-name">Haushalt</span>
        <span class="brand-sub">{monthLabel}</span>
      </div>

      <nav class="sidebar-nav">
        <p class="nav-group-label">Ansichten</p>
        {#each nav as item}
          <a href={item.href} class="sidebar-link" class:active={isActive(item.href)}>
            <span class="nav-icon">{@html item.icon}</span>
            {item.label}
          </a>
        {/each}
        <div class="nav-divider"></div>
      </nav>

      <div class="sidebar-footer">
        <button onclick={() => { authStore.logout(); goto('/login'); }}>Abmelden</button>
      </div>
    </aside>

    <main class="main-content">
      {@render children()}
    </main>

    <nav class="bottom-nav" aria-label="Hauptnavigation">
      {#each bottomNav as item}
        <a href={item.href} class="bottom-nav-item" class:active={isActive(item.href)}>
          <span class="bottom-icon">{@html item.icon}</span>
          <span class="bottom-label">{item.label}</span>
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
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 0.2rem;
    color: var(--color-muted);
  }

  .bottom-nav-item.active {
    color: var(--color-text);
  }

  .bottom-icon {
    display: flex;
    align-items: center;
    opacity: 0.6;
  }

  .bottom-nav-item.active .bottom-icon {
    opacity: 1;
  }

  .bottom-label {
    font-size: 0.6875rem;
    font-weight: 500;
  }

  .bottom-nav-item.active .bottom-label {
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
      padding: 1.25rem 1.5rem 1rem;
      border-bottom: 1px solid var(--color-divider);
      display: flex;
      flex-direction: column;
      gap: 0.125rem;
    }

    .brand-name {
      font-size: 1.125rem;
      font-weight: 700;
      line-height: 1.2;
    }

    .brand-sub {
      font-size: 0.75rem;
      color: var(--color-muted);
    }

    .sidebar-nav {
      flex: 1;
      display: flex;
      flex-direction: column;
      padding: 1rem 1rem 0.5rem;
    }

    .nav-group-label {
      font-size: 0.6875rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.08em;
      color: var(--color-muted);
      padding: 0 0.75rem;
      margin-bottom: 0.375rem;
    }

    .sidebar-link {
      display: flex;
      align-items: center;
      gap: 0.625rem;
      padding: 0.5rem 0.875rem;
      font-size: 0.9375rem;
      font-weight: 500;
      color: var(--color-muted);
      border-radius: var(--border-radius-sm);
      margin-bottom: 0.125rem;
    }

    .sidebar-link:hover {
      color: var(--color-text);
      background: var(--color-divider);
    }

    .sidebar-link.active {
      color: var(--color-text);
      font-weight: 700;
      background: var(--color-surface);
      border: var(--border-width) solid var(--color-border);
      box-shadow: var(--shadow-card);
    }

    .nav-icon {
      display: flex;
      align-items: center;
      flex-shrink: 0;
      opacity: 0.7;
    }

    .sidebar-link.active .nav-icon {
      opacity: 1;
    }

    .nav-divider {
      height: 1px;
      background: var(--color-divider);
      margin: 0.75rem 0.75rem;
    }

    .sidebar-footer {
      padding: 1rem 1.5rem;
      border-top: 1px solid var(--color-divider);
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
