<script lang="ts">
  export let label: string
  export let value: any = undefined
</script>

{#if value || $$slots.default}
  <div {...$$restProps}>
    <span class="text-muted">{label}</span>
    <p class="font-medium text-gray-800"><slot>{value}</slot></p>
  </div>
{/if}
