<template>
  <main class="required-notices"><section class="notice-card">
    <header><div><h1>平台重要通知</h1><p>请阅读并确认全部重要通知后进入平台</p></div><el-button :loading="leaving" @click="leave">退出登录</el-button></header>
    <el-alert v-if="error" :title="error" type="error" :closable="false"><el-button link @click="refresh">重新加载</el-button></el-alert>
    <p v-if="busy" role="status">正在核对待阅读通知…</p>
    <template v-else-if="pending.length"><div class="queue">当前第 {{ completed + 1 }} 条，共 {{ completed + pending.length }} 条</div><PlatformNoticeReading :id="pending[0].id" :key="pending[0].id" :button-text="pending.length > 1 ? '确认已阅，查看下一条' : '确认已阅，进入平台'" @confirmed="confirmed" @unavailable="refresh" /></template>
  </section></main>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { checkPlatformNotices, platformNoticeState } from '@/utils/platformNoticeState'
import { safeLoginRedirect } from '@/utils/detailNavigation'
import { logout } from '@/api/user'
import { clearWorksCache } from '@/utils/worksCache'
import { resetCurrentAccess } from '@/utils/accessState'
import PlatformNoticeReading from '@/components/PlatformNoticeReading.vue'
const router = useRouter(), route = useRoute(), busy = ref(false), error = ref(''), completed = ref(0), leaving = ref(false)
const pending = computed(() => platformNoticeState.pending)
async function refresh() {
  if (busy.value) return
  busy.value = true; error.value = ''
  try { const list = await checkPlatformNotices(true); if (!list.length) { const target = safeLoginRedirect(router, route.query.return, '/home/board'); await router.replace(target.startsWith('/required-notices') ? '/home/board' : target) } }
  catch (e) { error.value = e.message || '重要通知加载失败，请重试' }
  finally { busy.value = false }
}
async function confirmed() { completed.value++; await refresh() }
async function leave() {
  if (leaving.value) return; leaving.value = true
  try { await logout() } catch { /* Local sign-out must remain available. */ }
  finally { clearWorksCache(); localStorage.removeItem('token'); resetCurrentAccess(); await router.replace('/login'); leaving.value = false }
}
onMounted(refresh)
</script>
<style scoped>
.required-notices{min-height:100vh;background:#f0f3f8;padding:32px 20px;box-sizing:border-box}.notice-card{max-width:1000px;margin:auto;background:white;border:1px solid #e2e8f0;border-radius:10px;padding:28px 32px;box-shadow:0 6px 28px #20365b0a}header{display:flex;justify-content:space-between;align-items:flex-start;gap:20px;border-bottom:1px solid #e8edf3;padding-bottom:15px}h1{font-size:23px;margin:0 0 8px}header p{color:#7a8798;font-size:14px;margin:0}.queue{font-size:14px;color:#3975b9;margin-top:20px}@media(max-width:650px){.required-notices{padding:12px}.notice-card{padding:18px}header{flex-wrap:wrap}}
</style>
