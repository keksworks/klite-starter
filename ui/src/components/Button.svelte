<script lang="ts">
  import Icon from 'src/icons/Icon.svelte'

  export let icon = ''
  export let size: 'sm'|''|'lg' = ''
  export let label = ''
  export let type: 'button'|'submit' = 'button'

  $: hasLabel = label || $$slots.default
</script>

<button {type} {...$$restProps} class="btn {$$props.class ?? 'default'} {size} inline-flex items-center gap-1" class:icon-only={icon && !hasLabel}>
  {#if icon}
    <Icon name={icon}/>
  {/if}
  {#if hasLabel}
    <slot>{label}</slot>
  {/if}
</button>
