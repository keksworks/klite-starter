<script lang="ts">
  import {t} from 'i18n'
  import api from 'src/api/api'
  import {initSession} from 'src/stores/auth'
  import {navigate} from '@keksworks/svelte-tiny-router'
  import Form from 'src/forms/Form.svelte'
  import FormField from 'src/forms/FormField.svelte'
  import Button from 'src/components/Button.svelte'
  import type {User} from 'src/api/types'

  export let waitingForCode = false

  let email = '', code = ''

  async function submit() {
    if (waitingForCode) {
      initSession(await api.post<User>('auth/email/code', {email, code}))
      let to = location.hash.substring(1)
      if (!to || to == '/') to = '/'
      navigate(to)
    } else {
      await api.post('auth/email', {email})
      waitingForCode = true
      code = ''
    }
  }
</script>

<div class="w-full md:w-96 md:mt-10 mx-auto">
  <Form {submit}>
    {#if waitingForCode}
      <p>{@html t.login.codeSent.replace('{email}', '<b>' + email + '</b>')}</p>
      <FormField placeholder={t.login.code} type="text" inputmode="numeric" bind:value={code} autofocus class="lg"/>
      <Button type="submit" label={t.general.confirm} class="w-full lg"/>
      <Button type="button" label={t.general.cancel} class="outline w-full lg" onclick={() => waitingForCode = false}/>
    {:else}
      <slot/>
      <FormField placeholder={t.login.email} type="email" bind:value={email} autofocus class="lg"/>
      <Button type="submit" label={t.login.sendCode} class="w-full lg"/>
    {/if}
  </Form>
</div>
