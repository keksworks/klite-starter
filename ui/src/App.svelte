<script lang="ts">
  import {t} from 'i18n'
  import {logout, user} from 'src/stores/auth'
  import Toasts from 'src/components/Toasts.svelte'
  import {Link, Route, Router} from '@keksworks/svelte-tiny-router'
  import HomePage from 'src/pages/HomePage.svelte'
  import NotFoundPage from 'src/pages/NotFoundPage.svelte'
  import LoginPage from 'src/pages/login/LoginPage.svelte'

  const pages = {
    '/': {title: t.home.title, component: HomePage},
  }

  let matchedPath: keyof typeof pages
  $: activePage = pages[matchedPath]
</script>

<svelte:head>
  <title>{t.title}</title>
</svelte:head>

<Toasts/>

<menu class="flex gap-4 p-4 bg-gray-100 items-center">
  <span class="font-bold">LOGO</span>
  {#each Object.entries(pages) as [path, page]}
    <Link to={path} label={page.title} class="{path === matchedPath ? 'font-bold' : ''}"/>
  {/each}
  <span class="ml-auto">
    {#if $user}
      <span class="mr-2 text-sm text-gray-600">{$user.name}</span>
      <button class="btn outlined text-sm" on:click={logout}>{t.login.logout}</button>
    {:else}
      <Link to="/login" label={t.login.link}/>
    {/if}
  </span>
</menu>

<main class="p-4">
  <Router routes={pages} bind:matchedPath>
    <Route path="/login" component={LoginPage}/>
    <Route component={NotFoundPage}/>
  </Router>
</main>
