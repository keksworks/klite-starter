<script lang="ts">
  import Icon from 'src/icons/Icon.svelte'

  export let label: string
  export let tags: string[] = []
  export let type = 'text'
  export let id = label?.replace(/\W/g, '-')

  let input = ''
  let inputEl: HTMLInputElement

  function add() {
    const value = input.trim()
    if (!value || tags.includes(value)) return
    tags = [...tags, value]
    input = ''
  }

  function remove(index: number) {
    tags = tags.filter((_, i) => i !== index)
  }

  function onKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter') { e.preventDefault(); add() }
    if (e.key === 'Backspace' && !input && tags.length) remove(tags.length - 1)
  }
</script>

<div class="form-field">
  <label for={id}>{label}</label>
  <!-- svelte-ignore a11y_click_events_have_key_events -->
  <!-- svelte-ignore a11y_no_static_element_interactions -->
  <div class="chip-input" onclick={() => inputEl?.focus()}>
    {#each tags as tag, i}
      <span class="badge">
        {tag}
        <button type="button" onclick={e => { e.stopPropagation(); remove(i) }} class="chip-remove">
          <Icon name="x"/>
        </button>
      </span>
    {/each}
    <input bind:this={inputEl} bind:value={input} {id} {type} onkeydown={onKeydown} onblur={add} onchange={add} class="chip-text-input">
  </div>
</div>
