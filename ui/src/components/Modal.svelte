<script context="module" lang="ts">
  const openModals: (() => void)[] = []

  if (typeof window !== 'undefined') {
    window.addEventListener('keyup', (e: KeyboardEvent) => {
      if (e.code === 'Escape') openModals.at(-1)?.()
    })
  }

  /** Registers a close handler so Escape closes the top-most open modal/dialog */
  export function trackModal(close: () => void, open: boolean) {
    const i = openModals.indexOf(close)
    if (open && i < 0) openModals.push(close)
    else if (!open && i >= 0) openModals.splice(i, 1)
    document.body.classList.toggle('modal-open', openModals.length > 0)
  }
</script>

<script lang="ts">
  import {t} from 'i18n'
  import type {FlyParams} from 'svelte/transition'
  import {fade, fly} from 'svelte/transition'
  import Icon from 'src/icons/Icon.svelte'
  import {onDestroy} from 'svelte'

  export let title: string = ''
  export let show: any|false = true
  export let wide = false
  export let flyParams: FlyParams = {y: -500, duration: window.e2eTest ? 0 : 400}
  export let description: string | undefined = undefined
  export let closable = true

  let modal: HTMLElement
  $: if (modal) document.body.appendChild(modal)

  $: trackModal(close, !!show)

  function close() {
    if (closable) show = false
  }

  onDestroy(() => {
    trackModal(close, false)
    modal?.remove()
  })
</script>

{#if show}
  <div bind:this={modal} class="modal transition-opacity fixed z-40 inset-0 overflow-y-auto" transition:fade={{duration: flyParams.duration}}>
    <div class="flex items-center justify-center min-h-screen p-4 md:pb-20 text-center">
      <div class="fixed inset-0 bg-gray-500/75 transition-opacity"></div>

      <div class="w-full {wide ? 'max-w-7xl': 'max-w-xl'} relative inline-block bg-white rounded-lg p-6 md:p-10 text-left overflow-hidden shadow-xl transform transition-all align-middle"
           role="dialog" transition:fly={flyParams}>
        {#if closable}<div class="block absolute -top-2 -right-2 pt-4 md:pt-8 pr-4 md:pr-8 z-40">
          <button type="button" title={t.general.close} onclick={close} class="bg-white flex items-center justify-center text-gray-400 hover:text-gray-500 h-10 w-10 rounded-full focus:outline-none focus:ring-2 focus:ring-inset focus:ring-primary-500">
            <Icon name="x"/>
          </button>
        </div>{/if}

        {#if title}<h3 class="mb-4">{title}</h3>
          {#if description}<p class="mb-4 text-sm text-gray-500">{description}</p>
          {/if}
        {/if}
        <slot/>
      </div>
    </div>
  </div>
{/if}
