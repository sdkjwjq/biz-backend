<template><main class="platform-read"><div class="read-head"><h2>平台通知</h2><el-button @click="router.push('/home/notice')">返回消息中心</el-button></div><PlatformNoticeReading :id="route.params.id" @confirmed="done" /></main></template>
<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PlatformNoticeReading from './PlatformNoticeReading.vue'
const route = useRoute(), router = useRouter()
function done() { ElMessage.success('已确认阅读'); router.push('/home/notice') }
</script>
<style scoped>.platform-read{height:100%;overflow:auto;box-sizing:border-box;background:white;padding:25px;max-width:1100px;margin:auto}.read-head{display:flex;align-items:center;justify-content:space-between;gap:20px}.read-head h2{font-size:20px}</style>
