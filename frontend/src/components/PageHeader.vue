<template>
  <div class="page-header">
    <div>
      <h1>{{ title }}</h1>
      <p v-if="description">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="page-actions"><slot name="actions" /></div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  description: { type: String, default: '' }
})
</script>

<style scoped>
.page-header { display: flex; align-items: flex-end; justify-content: space-between; gap: 20px; margin-bottom: 20px; }
h1 { color: #172d25; font-size: 26px; line-height: 1.25; }
p { margin-top: 6px; color: #748079; font-size: 14px; }
.page-actions { display: flex; gap: 10px; flex-wrap: wrap; justify-content: flex-end; }
@media (max-width: 640px) {
  .page-header { align-items: flex-start; flex-direction: column; }
  .page-actions { width: 100%; justify-content: flex-start; }
}
</style>
