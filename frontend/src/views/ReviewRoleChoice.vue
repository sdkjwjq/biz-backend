<template>
 <main class="role-page"><section><h1>选择使用角色</h1><p>信息填报和专家评审分别操作，可在个人菜单中切换。</p>
  <el-alert v-if="error" :title="error" type="error" :closable="false" /><div v-loading="loading" class="role-options">
   <el-button v-if="accessState.businessAvailable" :disabled="busy" @click="choose('BUSINESS')"><strong>信息填报</strong><span>进入原有业务和管理功能</span></el-button>
   <el-button v-if="accessState.expertAvailable" :disabled="busy" @click="choose('EXPERT')"><strong>专家评审</strong><span>查看获准任务并提交评语和结论</span></el-button>
  </div><el-button v-if="error" @click="load">重新加载</el-button><el-button v-if="accessState.historyAvailable" link @click="router.push('/review-history')">查看历史评审</el-button><el-button link @click="exit">返回登录</el-button>
 </section></main>
</template>
<script setup>
import {ref,onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {accessState,loadCurrentAccess,resetCurrentAccess} from '@/utils/accessState'
import {post} from '@/utils/request'
import {getSsoStatus} from '@/api/user'
import {clearWorksCache} from '@/utils/worksCache'
const router=useRouter(),loading=ref(true),busy=ref(false),error=ref('')
async function load(){loading.value=true;error.value='';try{await loadCurrentAccess()}catch(e){error.value=e.message}finally{loading.value=false}}
async function choose(experience){if(busy.value)return;busy.value=true;error.value='';try{const result=await post('/system/review-context',{experience});clearWorksCache();resetCurrentAccess();localStorage.setItem('token',result.token);await router.replace(experience==='EXPERT'?'/home/works':'/home/board')}catch(e){error.value=e.message}finally{busy.value=false}}
async function exit(){
 const token=localStorage.getItem('token')
 let ssoSession=false
 try{ssoSession=JSON.parse(atob(token.split('.')[1].replace(/-/g,'+').replace(/_/g,'/'))).loginSource==='SSO'}catch{/* Invalid sessions still need local sign-out. */}
 try{await post('/system/logout')}catch{/* Expired tokens need no server cleanup. */}
 localStorage.removeItem('token');resetCurrentAccess()
 if(ssoSession){
  try{const config=await getSsoStatus();if(config?.logoutUrl){window.location.assign(config.logoutUrl);return}}catch{/* Continue to local login if CAS is unavailable. */}
 }
 router.replace('/login')
}
onMounted(load)
</script>
<style scoped>.role-page{min-height:100dvh;background:#f1f5fa;display:grid;place-items:center;padding:24px;box-sizing:border-box}.role-page section{background:white;padding:36px;border-radius:12px;max-width:760px;width:100%;box-sizing:border-box}.role-page h1{margin-top:0;color:#24354b}.role-page p{color:#758499;line-height:1.7}.role-options{display:flex;gap:20px;margin:28px 0;flex-wrap:wrap}.role-options .el-button{height:auto;min-height:120px;flex:1;margin:0;padding:24px}.role-options :deep(.el-button span){display:flex;flex-direction:column;gap:14px;white-space:normal}.role-options strong{font-size:22px}</style>
