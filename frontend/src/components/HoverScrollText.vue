<template>
  <span ref="viewport" class="hover-scroll-text" :class="{ truncated }" :tabindex="truncated ? 0 : undefined"
    :aria-label="text" :style="style">
    <span ref="content" class="scroll-content">{{ text }}</span>
    <span ref="naturalWidth" class="natural-width" aria-hidden="true">{{ text }}</span>
  </span>
</template>
<script setup>
import { ref, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue';
const props = defineProps({ text: { type: String, default: '' } });
const viewport = ref(null), content = ref(null), naturalWidth = ref(null), distance = ref(0);
const truncated = computed(() => distance.value > 1);
const style = computed(() => ({ '--scroll-end': `-${distance.value}px`, '--scroll-duration': `${Math.max(4, distance.value / 36)}s` }));
let observer;
// 独立测量不换行的原始宽度，避免减少动态效果时换行触发反复测量和闪烁。
const measure = () => { distance.value = Math.max(0, (naturalWidth.value?.scrollWidth || 0) - (viewport.value?.clientWidth || 0)); };
watch(() => props.text, async () => { await nextTick(); measure(); });
onMounted(() => { observer = new ResizeObserver(measure); observer.observe(viewport.value); measure(); });
onBeforeUnmount(() => observer?.disconnect());
</script>
<style scoped>
.hover-scroll-text { position: relative; display: block; max-width: 100%; min-width: 0; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; }
.natural-width { position: absolute; width: max-content; white-space: nowrap; visibility: hidden; pointer-events: none; top: 0; left: 0; }
.scroll-content { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.truncated:hover, .truncated:focus { text-overflow: clip; outline-offset: 2px; }
.truncated:hover .scroll-content, .truncated:focus .scroll-content { width: max-content; overflow: visible; animation: reveal-name var(--scroll-duration) linear .5s infinite alternate; }
@keyframes reveal-name { from { transform: translateX(0); } to { transform: translateX(var(--scroll-end)); } }
@media (prefers-reduced-motion: reduce) { .truncated:hover, .truncated:focus { white-space: normal; } .truncated:hover .scroll-content, .truncated:focus .scroll-content { width: auto; animation: none; white-space: normal; overflow-wrap: anywhere; } }
</style>
