<script lang="ts">
  import {t} from 'i18n'
  import api from 'src/api/api'
  import {initSession} from 'src/stores/auth'
  import {navigate} from '@keksworks/svelte-tiny-router'
  import type {User} from 'src/api/types'

  export let waitingForCode = false

  let email = '', code = ''

  async function submit() {
    if (waitingForCode) {
      const u = await api.post<User>('auth/email/code', {email, code})
      initSession(u)
      let to = location.hash.substring(1)
      if (to == '/') to = '/'
      navigate(to)
    } else {
      await api.post('auth/email', {email})
      waitingForCode = true
      code = ''
    }
  }
</script>

<form on:submit|preventDefault={submit} class="w-full md:w-96 md:mt-10 mx-auto">
  {#if waitingForCode}
    <p class="mb-4">{t.login.codeSent.replace('{email}', email)}</p>
    <label class="block mb-2">
      <span class="text-sm font-medium">{t.login.code}</span>
      <input type="text" inputmode="numeric" bind:value={code} autofocus
        class="mt-1 block w-full rounded border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500" />
    </label>
    <button type="submit" class="btn lg w-full">{t.general.confirm}</button>
    <button type="button" class="btn outlined default lg w-full mt-2" on:click={() => waitingForCode = false}>{t.general.cancel}</button>
  {:else}
    <label class="block mb-4">
      <span class="text-sm font-medium">{t.login.email}</span>
      <input type="email" bind:value={email} autofocus required
        class="mt-1 block w-full rounded border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500" />
    </label>
    <button type="submit" class="btn lg w-full">{t.login.sendCode}</button>
  {/if}

  <slot/>
</form>
