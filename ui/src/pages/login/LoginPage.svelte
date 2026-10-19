<script lang="ts">
  import {t} from 'i18n'
  import LoginCode from 'src/pages/login/LoginCode.svelte'
  import LoginButton from 'src/pages/login/LoginButton.svelte'

  let waitingForCode = false
</script>

<div class="max-w-md mx-auto mt-10 px-4">
  <h1 class="text-2xl font-bold mb-6 text-center">{t.login.link}</h1>
  <LoginCode bind:waitingForCode>
    <LoginButton label="Google Login" class="outline lg w-full" redirect={location.hash.substring(1)}/>
  </LoginCode>
</div>
