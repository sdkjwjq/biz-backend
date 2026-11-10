<template>
  <el-dialog :model-value="passwordState.required" title="请先修改密码" width="420px"
    class="required-password-dialog" append-to-body :show-close="false" :close-on-click-modal="false"
    :close-on-press-escape="false" :before-close="keepOpen">
    <div class="password-notice">当前账号需要修改密码，请按下方要求设置新密码。修改成功后即可正常使用平台。</div>
    <el-form label-position="top" @submit.prevent="save">
      <el-form-item label="新密码" :error="errors.password">
        <el-input v-model="form.password" placeholder="请输入新密码" type="password" show-password autocomplete="new-password" :disabled="saving || leaving" />
      </el-form-item>
      <p class="password-rule">{{ PASSWORD_RULE }}</p>
      <el-form-item label="确认密码" :error="errors.confirm">
        <el-input v-model="form.confirm" placeholder="请再次输入新密码" type="password" show-password autocomplete="new-password" :disabled="saving || leaving" @keyup.enter="save" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="leaving" :disabled="saving || leaving" @click="returnToLogin">返回登录</el-button>
      <el-button type="primary" :loading="saving" :disabled="saving || leaving" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword, logout } from '@/api/user'
import { useRouter } from 'vue-router'
import { clearWorksCache } from '@/utils/worksCache'
import { passwordState, PASSWORD_RULE, isPasswordValid } from '@/utils/passwordPolicy'

const form = reactive({ password: '', confirm: '' })
const errors = reactive({ password: '', confirm: '' })
const saving = ref(false)
const leaving = ref(false)
const router = useRouter()
const returnToLogin = async () => {
  if (saving.value || leaving.value) return
  const token = localStorage.getItem('token')
  leaving.value = true
  try {
    try { await logout() } catch { /* 登出接口失败仍清理本地登录状态 */ }
    if (localStorage.getItem('token') && localStorage.getItem('token') !== token) return
    clearWorksCache()
    localStorage.removeItem('token')
    form.password = ''; form.confirm = ''
    errors.password = ''; errors.confirm = ''
    await router.replace('/login')
  } finally { leaving.value = false }
}
const keepOpen = () => { /* 必须保存合规密码后才能继续使用 */ }
const save = async () => {
  if (saving.value || leaving.value) return
  errors.password = !isPasswordValid(form.password) ? PASSWORD_RULE : ''
  errors.confirm = !form.confirm ? '请再次输入新密码' : form.confirm !== form.password ? '两次输入的密码不一致' : ''
  if (errors.password || errors.confirm) return
  const token = localStorage.getItem('token')
  saving.value = true
  try {
    await changePassword(form.password)
    if (localStorage.getItem('token') !== token) return
    form.password = ''
    form.confirm = ''
    ElMessage.success('密码修改成功')
    // 重建页面，重新核对状态并加载之前被阻止的业务数据。
    window.location.reload()
  } catch (error) {
    if (localStorage.getItem('token') === token) ElMessage.error(error?.message || '密码修改失败，请重试')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.password-notice { background: #f5f7fa; color: #606266; padding: 14px; border-radius: 4px; line-height: 1.8; font-size: 14px; margin-bottom: 20px; }
.password-rule { color: #909399; font-size: 12px; line-height: 1.6; margin: -6px 0 18px; }
</style>
<style>
.required-password-dialog { max-width: calc(100vw - 32px); }
</style>
