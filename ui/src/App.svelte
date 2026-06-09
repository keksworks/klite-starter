<script lang="ts">
  import {t} from 'i18n'
  import Toasts from 'src/components/Toasts.svelte'
  import {activePath, Link, Route, Router} from '@keksworks/svelte-tiny-router'
  import HomePage from 'src/pages/HomePage.svelte'
  import NotFoundPage from 'src/pages/NotFoundPage.svelte'

  const pages = [
    {path: '/', title: t.home.title, component: HomePage},
  ]

  $: activePage = pages.find(p => p.path === $activePath)
</script>

<svelte:head>
  <title>{activePage?.title} - {t.title}</title>
</svelte:head>

<Toasts/>

<menu class="flex gap-4 p-4 bg-gray-100">
  <span class="font-bold">LOGO</span>
  {#each pages as page}
    <Link to={page.path} label={page.title} class="{page === activePage ? 'text-underline!' : ''}"/>
  {/each}
</menu>

<main class="p-4">
  <h1 class="mb-4">{activePage?.title}</h1>
  <Router routes={pages}>
    <Route component={NotFoundPage}/>
  </Router>
</main>
