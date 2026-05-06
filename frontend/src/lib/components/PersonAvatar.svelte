<script lang="ts">
  let {
    memberId,
    currentMemberId,
    email,
    size = 28,
  }: {
    memberId: string | null;
    currentMemberId: string | null;
    email: string | null;
    size?: number;
  } = $props();

  let isCurrentUser = $derived(!!memberId && memberId === currentMemberId);
  let initial = $derived(isCurrentUser && email ? email[0].toUpperCase() : '');
</script>

{#if memberId}
  <span
    class="avatar"
    class:person-a={isCurrentUser}
    class:person-b={!isCurrentUser}
    style="width: {size}px; height: {size}px; font-size: {size * 0.43}px;"
    title={isCurrentUser ? (email ?? 'Du') : 'Partner'}
  >
    {initial}
  </span>
{/if}

<style>
  .avatar {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    border: var(--border-width) solid var(--color-border);
    font-weight: 700;
    flex-shrink: 0;
    user-select: none;
  }

  .person-a {
    background: var(--color-person-a);
    color: var(--color-person-a-text);
  }

  .person-b {
    background: var(--color-person-b);
    color: var(--color-person-b-text);
  }
</style>
