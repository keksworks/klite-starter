<script lang="ts">
  import {t} from 'i18n'
  import Toasts from 'src/components/Toasts.svelte'
  import {Link, Route, Router} from '@keksworks/svelte-tiny-router'
  import HomePage from 'src/pages/HomePage.svelte'
  import NotFoundPage from 'src/pages/NotFoundPage.svelte'

  const pages = {
    '/': {title: t.home.title, component: HomePage},
  }

  let matchedPath: keyof typeof pages
  $: activePage = pages[matchedPath]
</script>

<svelte:head>
  <title>{activePage ? activePage.title + ' - ' : ''}{t.title}</title>
</svelte:head>

<Toasts/>

<menu class="flex gap-4 p-4 bg-gray-100">
  <span class="font-bold">LOGO</span>
  {#each Object.entries(pages) as [path, page]}
    <Link to={path} label={page.title} class="{path === matchedPath ? 'font-bold' : ''}"/>
  {/each}
</menu>

<main class="p-4">
  <h1 class="mb-4">{activePage?.title}</h1>
  <Router routes={pages} bind:matchedPath noMatch={NotFoundPage}/>
</main>
