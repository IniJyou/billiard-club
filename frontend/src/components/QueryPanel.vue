<template>
  <div class="query-panel">
    <div class="query-fields"><slot /></div>
    <div class="query-actions"><slot name="actions" /></div>
  </div>
</template>

<style scoped>
.query-panel { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 16px; margin-bottom: 16px; border: 1px solid #e4ebe7; border-radius: 12px; background: #f8fbf9; }
.query-fields, .query-actions { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.query-actions { justify-content: flex-end; }
@media (max-width: 900px) {
  .query-panel { align-items: stretch; flex-direction: column; }
  .query-actions { justify-content: flex-start; }
}
</style>
