<template>
  <main class="sso-exit" role="status">正在退出平台，请稍候…</main>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { logout } from '@/api/user'
import { clearWorksCache } from '@/utils/worksCache'
import { resetCurrentAccess } from '@/utils/accessState'
import { passwordState } from '@/utils/passwordPolicy'

const router = useRouter()
onMounted(async () => {
  try {
    if (localStorage.getItem('token')) await logout()
  } catch { /* A stale token must not prevent local sign-out. */ }
  clearWorksCache()
  localStorage.removeItem('token')
  resetCurrentAccess()
  passwordState.required = false
  await router.replace('/login')
})
</script>

<style scoped>
.sso-exit { min-height: 100vh; display: grid; place-items: center; color: #53647a; }
</style>
