<template>
  <div class="proto-root">
    <div class="login-page">
      <!-- 大图轮播 -->
      <div id="dowebok" class="fader" aria-hidden="true">
        <img v-for="(src, idx) in slides" :key="src" class="slide" :class="{ active: idx === activeSlide }" :src="src"
          alt="" />
      </div>

      <!-- 原型顶部 logo -->
      <div class="logo">
        <img :src="protoLogo" alt="logo" />
      </div>

      <!-- 登录框： -->
      <div class="login-container">
        <div class="col-xs-12 text-center login-passtitle">
          <h1>“双高建设计划”</h1>
          <p>项目管理和绩效评价平台</p>
        </div>

        <div class="col-xs-12 login-form-wrap">
          <div class="login-form">
            <div class="login-middle">
              <form @submit.prevent="handleLogin">
                <p class="error text-left" v-if="errorMsg">{{ errorMsg }}</p>

                <div class="form-group">
                  <div class="input-group">
                    <div class="input-group-addon">
                      <img :src="icoUser" alt="" />
                    </div>
                    <input v-model="form.userId" class="form-control user-input" tabindex="1" placeholder="账号"
                      inputmode="numeric" autocomplete="username" @keydown.enter.prevent="handleLogin" />
                  </div>
                </div>

                <div class="form-group">
                  <div class="input-group">
                    <div class="input-group-addon">
                      <img :src="icoPwd" alt="" />
                    </div>
                    <input v-model="form.password" class="form-control pwd-input" tabindex="2" type="password"
                      placeholder="密码" autocomplete="current-password" @keydown.enter.prevent="handleLogin" />
                  </div>
                </div>

                <div class="login-info row">
                  <p class="password-hint">首次登录可使用工号作为密码。<br>登录后，请按提示设置您的新密码。</p>
                  <div class="col-lg-12 login-actions">
                    <button type="submit" class="btn btn-block btn-primary login-btn" :disabled="loading">
                      {{ loading ? '登录中...' : '登 录' }}
                    </button>
                    <el-button v-if="ssoEnabled" class="sso-login" :disabled="loading || ssoLoading" @click="handleSsoStart">
                      {{ ssoLoading ? '正在连接统一认证...' : '学校统一身份认证登录' }}
                    </el-button>
                    <el-button link type="primary" :disabled="loading" @click="passwordResetRef.open(form.userId)">重置密码</el-button>
                  </div>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>

    <LoginPasswordReset ref="passwordResetRef" @completed="handlePasswordReset" />
    <div class="login-footer">
      <p>
        版权所有 © 徐州工业职业技术学院<br />
        地址：徐州市鼓楼区襄王路1号
      </p>
    </div>
  </div>
</template>

<script setup>
import { clearWorksCache } from '@/utils/worksCache'
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login as apiLogin, getSsoStatus, startSso, exchangeSso } from '@/api/user'
import LoginPasswordReset from '@/components/LoginPasswordReset.vue'
import { safeLoginRedirect } from '@/utils/detailNavigation'
import protoLogo from '@/assets/统一身份认证平台_files/logo.png'
import icoUser from '@/assets/统一身份认证平台_files/ico01.png'
import icoPwd from '@/assets/统一身份认证平台_files/ico02.png'
import pic001 from '@/assets/统一身份认证平台_files/pic001.jpg'
import pic002 from '@/assets/统一身份认证平台_files/pic002.jpg'
import pic003 from '@/assets/统一身份认证平台_files/pic003.jpg'
import pic004 from '@/assets/统一身份认证平台_files/pic004.jpg'
import pic005 from '@/assets/统一身份认证平台_files/pic005.jpg'
import pic006 from '@/assets/统一身份认证平台_files/pic006.jpg'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const ssoLoading = ref(false)
const ssoEnabled = ref(false)
const errorMsg = ref('')
const passwordResetRef = ref(null)
const handlePasswordReset = userId => {
  form.userId = userId
  form.password = ''
  errorMsg.value = ''
}

const slides = [pic001, pic002, pic003, pic004, pic005, pic006]
const activeSlide = ref(0)
let slideTimer = null

const form = reactive({
  userId: '',
  password: ''
})

const rules = { // eslint-disable-line no-unused-vars
  userId: [
    { required: true, message: '请输入用户ID', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        const v = String(value ?? '').trim()
        if (!v) return callback()
        if (!/^\d+$/.test(v)) return callback(new Error('用户ID必须为数字'))
        callback()
      },
      trigger: 'blur',
    },
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const getDefaultHomeByToken = () => '/home/board'

const handleSsoStart = async () => {
  errorMsg.value = ''
  ssoLoading.value = true
  try {
    const target = safeLoginRedirect(router, route.query.redirect, getDefaultHomeByToken())
    sessionStorage.setItem('ssoReturn', target)
    const result = await startSso()
    if (!result?.url) throw new Error('未取得学校统一认证地址')
    window.location.assign(result.url)
  } catch (error) {
    errorMsg.value = error?.message || '暂时无法连接学校统一认证'
    ssoLoading.value = false
  }
}

const completeSso = async (code, state) => {
  ssoLoading.value = true
  // The authorization code is short-lived and must not remain in browser history.
  window.history.replaceState(window.history.state, '', '/login')
  try {
    const result = await exchangeSso(code, state)
    if (!result?.token) throw new Error('统一认证成功，但平台未取得登录凭证')
    clearWorksCache()
    localStorage.setItem('token', result.token)
    const target = safeLoginRedirect(router, sessionStorage.getItem('ssoReturn'), getDefaultHomeByToken())
    sessionStorage.removeItem('ssoReturn')
    await router.replace(target)
  } catch (error) {
    errorMsg.value = error?.message || '统一认证登录失败，请重试'
    sessionStorage.removeItem('ssoReturn')
  } finally {
    ssoLoading.value = false
  }
}

const handleLogin = async () => {
  // 保持原接口不变：仍然用 userId(数字) + password 登录
  const uidRaw = String(form.userId ?? '').trim()
  const pwd = String(form.password ?? '')
  errorMsg.value = ''

  if (!uidRaw) {
    errorMsg.value = '请输入用户ID（数字）'
    return
  }
  if (!/^\d+$/.test(uidRaw)) {
    errorMsg.value = '用户ID必须为数字'
    return
  }
  if (!pwd) {
    errorMsg.value = '请输入密码'
    return
  }

  try {
    loading.value = true
    const uid = Number(uidRaw)
    const resp = await apiLogin(uid, pwd)
    // 兼容后端返回的字段名：token（可能为驼峰或下划线命名）
    const token = resp?.token
    if (token) {
      clearWorksCache()
      localStorage.setItem('token', token)
      ElMessage.success('登录成功')
      const redirect = safeLoginRedirect(router, route.query.redirect, getDefaultHomeByToken(token))
      router.replace(redirect)
    } else {
      ElMessage.error('登录失败：未获取到token')
    }
  } catch (e) {
    ElMessage.error(e?.message || '登录失败，请检查账号密码')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getSsoStatus().then(result => { ssoEnabled.value = result?.enabled === true }).catch(() => {})
  const code = String(route.query.code || '')
  const state = String(route.query.state || '')
  if (code) completeSso(code, state)
  else if (route.query.error) {
    errorMsg.value = '学校统一认证未完成，请重新登录'
    window.history.replaceState(window.history.state, '', '/login')
  }
  // 已登录则直接进入管理平台
  const token = localStorage.getItem('token')
  if (token && !code && !route.query.error) router.replace(safeLoginRedirect(router, route.query.redirect, getDefaultHomeByToken(token)))

  // 轮播：用户偏好减少动效则不启动
  try {
    const reduce = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches
    if (!reduce) {
      slideTimer = window.setInterval(() => {
        activeSlide.value = (activeSlide.value + 1) % slides.length
      }, 4500)
    }
  } catch (e) { // eslint-disable-line no-unused-vars
    // ignore
  }
})

onUnmounted(() => {
  if (slideTimer) window.clearInterval(slideTimer)
  slideTimer = null
})
</script>

<style scoped>
.sso-login { width: 100%; margin: 10px 0 0; }
.password-hint { margin: 4px 0 20px; color: #7a8799; font-size: 12px; line-height: 1.8; }
/* 复刻原型关键布局（不引入原型的 jQuery/Bootstrap 脚本） */
.proto-root {
  height: 100vh;
  overflow: hidden;
  background: #fff;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  margin: 0;
  padding: 0;
}

.logo {
  position: absolute;
  z-index: 1000;
  left: 20px;
  top: 18px;
}

.logo img {
  display: block;
  max-height: 70px;
  max-width: 100%;
}

.login-page {
  position: relative;
  width: 100%;
  height: 100vh;
  min-height: 100vh;
}

/* 背景轮播 */
.fader {
  height: 100vh;
  overflow: hidden;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 1;
}

.fader .slide {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 1;
  opacity: 0;
  transition: opacity 900ms ease;
  object-fit: cover;
}

.fader .slide.active {
  z-index: 3;
  opacity: 1;
}

/* 登录框 */
.login-container {
  max-width: 420px;
  width: 420px;
  background: rgba(255, 255, 255, 0.97);
  min-height: 330px;
  display: block;
  position: absolute;
  right: 15%;
  top: 50%;
  transform: translateY(-50%);
  border-radius: 16px;
  box-shadow: 0 16px 48px rgba(16, 42, 75, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.7);
  z-index: 999;
  padding: 32px;
  box-sizing: border-box;
}

.login-passtitle {
  text-align: center;
  margin-bottom: 28px;
}

.login-passtitle h1 {
  color: #0551a3;
  font-size: 24px;
  font-weight: 600;
  line-height: 1.4;
  margin: 0;
  display: block;
  text-align: center;
}
.login-passtitle p { margin: 8px 0 0; font-size: 15px; color: #62748a; line-height: 1.5; }

.form-group {
  margin-bottom: 14px;
}

.input-group {
  display: flex;
  align-items: stretch;
  border: 1px solid #dce4ee;
  background: #f8fafd;
  border-radius: 8px;
  transition: border-color .2s, box-shadow .2s;
}
.input-group:focus-within { border-color: #387bbd; box-shadow: 0 0 0 3px rgba(56, 123, 189, .12); background: #fff; }

.input-group-addon {
  width: 44px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
}

.input-group-addon img {
  width: 18px;
  height: 18px;
}

.form-control {
  flex: 1;
  height: 46px;
  min-width: 0;
  width: 100%;
  box-sizing: border-box;
  padding: 6px 10px;
  border: none;
  outline: none;
  box-shadow: none;
  border-radius: 8px;
  color: #24364b;
  background: transparent;
  font-size: 14px;
}

.form-control::placeholder {
  color: #98a5b5;
}

.error {
  color: #f56060;
  padding: 6px 0;
  margin: 0 0 6px;
  font-size: 12px;
}

/* 简化版 bootstrap 按钮 */
.btn {
  display: inline-block;
  padding: 8px 12px;
  border: 1px solid transparent;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-block {
  width: 100%;
}

.btn-primary {
  margin-top: 0;
  background: #004ea2;
  color: #fff;
}

.btn-primary:hover {
  background: #186cf7;
}

.btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.login-actions {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  justify-content: center;
  gap: 16px;
  margin-top: 0;
}
.login-actions :deep(.el-button) { align-self: flex-end; margin: 0; font-size: 13px; }

.login-btn {
  width: 100%;
  height: 46px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(0, 78, 162, .16);
}

.panel-footer {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.link-btn {
  background: transparent;
  border: none;
  color: #0a8fe6;
  cursor: pointer;
  padding: 0;
  font-size: 13px;
}

.link-btn:hover {
  color: #004ea2;
}

/* 底部版权条 */
.login-footer {
  width: 100%;
  position: fixed;
  z-index: 999;
  left: 0;
  bottom: 0;
  clear: both;
  background: #004ea2;
  padding: 30px 0;
}

.login-footer p {
  color: #fff;
  text-align: center;
  line-height: 180%;
  font-size: 12px;
  margin: 0;
}

@media (max-width: 768px) {
  .login-container {
    max-width: 420px;
    width: calc(100% - 32px);
    right: 50%;
    top: 50%;
    transform: translate(50%, -50%);
    padding: 28px 24px;
  }

  .login-page {
    height: 100vh !important;
  }

  .fader {
    height: 100vh;
  }
}
@media (max-height: 650px) {
  .proto-root { overflow-y: auto; }
  .login-page { height: 650px !important; min-height: 650px; }
  .login-container { top: 110px; transform: none; }
  .login-footer { position: relative; }
}
@media (max-height: 650px) and (max-width: 768px) {
  .login-container { transform: translateX(50%); }
}
</style>

<style>
/* 全局样式：禁止页面滚动 */
html,
body {
  margin: 0;
  padding: 0;
  height: 100%;
  overflow: hidden;
}

#app {
  height: 100%;
  overflow: hidden;
}
</style>
