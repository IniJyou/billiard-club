<template>
  <div class="metric-card" :class="`tone-${tone}`">
    <span>{{ label }}</span>
    <strong>{{ value }}</strong>
    <small v-if="hint">{{ hint }}</small>
  </div>
</template>

<script setup>
defineProps({
  label: { type: String, required: true },
  value: { type: [String, Number], required: true },
  hint: { type: String, default: '' },
  tone: { type: String, default: 'green' }
})
</script>

<style scoped>
.metric-card { position: relative; overflow: hidden; min-height: 132px; padding: 22px; border: 1px solid #e4e9e6; border-radius: 14px; background: #fff; box-shadow: 0 8px 24px rgba(24, 55, 43, .05); }
.metric-card::after { content: ''; position: absolute; top: -24px; right: -20px; width: 88px; height: 88px; border-radius: 50%; background: var(--metric-accent); opacity: .12; }
span { display: block; color: #718078; font-size: 14px; }
strong { display: block; margin-top: 14px; color: #18372e; font-size: 28px; line-height: 1; }
small { display: block; margin-top: 12px; color: #8b9791; font-size: 12px; }
.tone-green { --metric-accent: #16865f; }
.tone-blue { --metric-accent: #3f7cac; }
.tone-gold { --metric-accent: #d79a32; }
.tone-coral { --metric-accent: #cc6654; }
</style>
