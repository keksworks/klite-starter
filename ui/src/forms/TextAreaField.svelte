<script lang="ts">
  import {t} from 'i18n'
  import FormField from 'src/forms/FormField.svelte'

  export let label: string|undefined = undefined
  export let value: string = ''
  export let maxlength = 1000
  export let rows = (value?.split('\n').length ?? 0) + 1
  export let required = true

  $: {
    const lines = value?.split('\n').length ?? 0
    if (lines > rows) rows = lines
  }

  let textarea: HTMLTextAreaElement
  $: tooLong = value?.length > maxlength
  $: textarea?.setCustomValidity(tooLong ? t.errors.tooLong : '')
</script>

<FormField {label} let:id class={$$props.class}>
  <div class="float-right text-sm -mt-6" class:text-red-500={tooLong}>
    {value?.length ?? 0} / {maxlength}
  </div>
  <textarea {id} bind:this={textarea} bind:value {rows} {required} {...$$restProps}></textarea>
</FormField>
