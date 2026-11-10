<template>
  <el-dialog v-model="visible" title="重置密码" width="440px" style="max-width: calc(100vw - 32px)" append-to-body
    :close-on-click-modal="!submitting" :close-on-press-escape="!submitting" :show-close="!submitting"
    :before-close="beforeClose" destroy-on-close @closed="clearForm">
    <p>请输入原密码或本人工号，再设置新密码。</p>
    <el-form label-position="top" @submit.prevent="submit">
      <el-form-item label="账号">
        <el-input v-model="form.userId" aria-label="账号" autocomplete="username" :disabled="submitting" />
      </el-form-item>
      <el-form-item label="原密码或工号">
        <el-input v-model="form.oldPassword" aria-label="原密码" type="password" show-password
          autocomplete="current-password" placeholder="请输入原密码或本人工号" :disabled="submitting" />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="form.newPassword" aria-label="新密码" type="password" show-password
          autocomplete="new-password" :disabled="submitting" />
        <span class="password-rule">{{ PASSWORD_RULE }}</span>
      </el-form-item>
      <el-form-item label="确认新密码">
        <el-input v-model="form.confirmPassword" aria-label="确认新密码" type="password" show-password
          autocomplete="new-password" :disabled="submitting" @keydown.enter.prevent="submit" />
      </el-form-item>
      <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
    </el-form>
    <template #footer>
      <el-button :disabled="submitting" @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" :disabled="submitting" @click="submit">确认重置</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { resetPassword } from '@/api/user'
import { PASSWORD_RULE, isPasswordValid } from '@/utils/passwordPolicy'

const emit = defineEmits(['completed'])
const visible = ref(false)
const submitting = ref(false)
const error = ref('')
const form = reactive({ userId: '', oldPassword: '', newPassword: '', confirmPassword: '' })
let disposed = false
const clearForm = () => {
  Object.assign(form, { userId: '', oldPassword: '', newPassword: '', confirmPassword: '' })
  error.value = ''
}
const open = (userId) => {
  if (submitting.value) return
  clearForm()
  form.userId = String(userId || '').trim()
  visible.value = true
}
const beforeClose = done => { if (!submitting.value) done() }
const submit = async () => {
  if (submitting.value) return
  error.value = ''
  const id = form.userId.trim()
  if (!/^[0-9]+$/.test(id) || BigInt(id) <= 0n || BigInt(id) > 9223372036854775807n) {
    error.value = '请输入有效的数字账号'
  } else if (!form.oldPassword) {
    error.value = '请输入原密码或本人工号'
  } else if (!isPasswordValid(form.newPassword)) {
    error.value = PASSWORD_RULE
  } else if (form.newPassword !== form.confirmPassword) {
    error.value = '两次输入的新密码不一致'
  }
  if (error.value) return
  submitting.value = true
  try {
    await resetPassword({ user_id: id, old_password: form.oldPassword, new_password: form.newPassword })
    if (disposed) return
    visible.value = false
    clearForm()
    emit('completed', id)
    ElMessage.success('密码重置成功，请使用新密码登录')
  } catch (e) {
    if (!disposed) error.value = e?.message || '密码重置失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}
onUnmounted(() => { disposed = true; clearForm() })
defineExpose({ open })
</script>

<style scoped>
.password-rule { color: #909399; font-size: 12px; line-height: 1.6; margin-top: 6px; }
</style>
