<template><div class="unavailable"><el-empty description="当前账号尚未获准使用任何业务功能，请联系管理员配置权限。" /><el-button @click="logout">返回登录</el-button></div></template>
<script setup>
import { useRouter } from 'vue-router'
import { resetCurrentAccess } from '@/utils/accessState'
import { clearWorksCache } from '@/utils/worksCache'
const router = useRouter()
const logout = () => { clearWorksCache(); localStorage.removeItem('token'); resetCurrentAccess(); router.replace('/login') }
</script>
<style scoped>.unavailable{padding:40px;text-align:center;background:#fff;height:100%;box-sizing:border-box}</style>
