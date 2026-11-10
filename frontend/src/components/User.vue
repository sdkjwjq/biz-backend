<template>
  <div class="user-profile-container">
    <el-row :gutter="20">
      <!-- 左侧：个人简介卡片 -->
      <el-col :span="8" :xs="24">
        <el-card class="box-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
            </div>
          </template>
          <div class="profile-header">
            <el-avatar :size="100" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
            <div class="profile-name">{{ userInfo.name }}</div>
            <div class="profile-role">{{ userInfo.roleName }}</div>
          </div>
          <div class="profile-body">
            <div class="info-item">
              <el-icon><User /></el-icon>
              <span class="label">工号：</span>
              <span>{{ userInfo.username }}</span>
            </div>
            <div class="info-item">
              <el-icon><OfficeBuilding /></el-icon>
              <span class="label">部门：</span>
              <span>{{ userInfo.dept }}</span>
            </div>
            <div class="info-item">
              <el-icon><Iphone /></el-icon>
              <span class="label">手机：</span>
              <span>{{ userInfo.phone }}</span>
            </div>
            <div class="info-item">
              <el-icon><Message /></el-icon>
              <span class="label">邮箱：</span>
              <span>{{ userInfo.email }}</span>
            </div>
          </div>
          <div class="profile-footer">
            <el-button type="primary" class="w-100" @click="openEditProfileDialog">修改资料</el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：详细资料与安全 -->
      <el-col :span="16" :xs="24">
        <el-card class="box-card" shadow="hover">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <!-- 基本资料 Tab -->
            <el-tab-pane label="基本资料" name="info">
              <el-descriptions title="详细信息" :column="2" border>
                <el-descriptions-item label="姓名">{{ userInfo.name }}</el-descriptions-item>
                <el-descriptions-item label="性别">男</el-descriptions-item>
                <el-descriptions-item label="入职日期">2018-09-01</el-descriptions-item>
                <el-descriptions-item label="职称">副教授</el-descriptions-item>
                <el-descriptions-item label="双高建设任务">
                  <el-tag size="small">核心成员</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag type="success" size="small">在职</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="办公地点" :span="2">行政楼 305 室</el-descriptions-item>
                <el-descriptions-item label="备注" :span="2">
                  负责双高计划“打造技术技能人才培养高地”项目统筹工作。
                </el-descriptions-item>
              </el-descriptions>
            </el-tab-pane>
            
            <!-- 账号安全 Tab -->
            <el-tab-pane label="账号安全" name="security">
              <div class="security-list">
                <div class="security-item">
                  <div class="sec-info">
                    <div class="sec-title">登录密码</div>
                    <div class="sec-desc">建议您定期更改密码以保护账户安全</div>
                  </div>
                  <el-button type="primary" link @click="openChangePasswordDialog">修改</el-button>
                </div>
                <el-divider />
                <div class="security-item">
                  <div class="sec-info">
                    <div class="sec-title">手机绑定</div>
                    <div class="sec-desc">已绑定手机：{{ userInfo.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') }}</div>
                  </div>
                  <el-button type="primary" link @click="openEditProfileDialog">修改</el-button>
                </div>
                <el-divider />
                <div class="security-item">
                  <div class="sec-info">
                    <div class="sec-title">邮箱绑定</div>
                    <div class="sec-desc">已绑定邮箱：{{ userInfo.email }}</div>
                  </div>
                  <el-button type="primary" link @click="openEditProfileDialog">修改</el-button>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <!-- 修改资料弹窗 -->
    <el-dialog v-model="editProfileVisible" title="修改个人资料" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="editForm.name" disabled></el-input>
          <span class="text-xs text-gray-400 ml-2">（姓名不可修改，请联系管理员）</span>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email"></el-input>
        </el-form-item>
        <el-form-item label="办公地点">
          <el-input v-model="editForm.address"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editProfileVisible = false">取消</el-button>
          <el-button type="primary" @click="saveProfile">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="passwordVisible" title="修改密码" width="450px" destroy-on-close @close="handlePasswordDialogClose">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px" status-icon>
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPasswordChange">确认修改</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User, OfficeBuilding, Iphone, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const activeTab = ref('info')

// --- 数据定义 ---
const userInfo = reactive({
  username: 'admin',
  name: '系统管理员',
  roleName: '超级管理员',
  dept: '“双高”建设办公室',
  phone: '13800138000',
  email: 'admin@xzcit.edu.cn',
  address: '行政楼 305 室'
})

// --- 修改资料逻辑 ---
const editProfileVisible = ref(false)
const editForm = reactive({ ...userInfo })

const openEditProfileDialog = () => {
  Object.assign(editForm, userInfo)
  editProfileVisible.value = true
}

const saveProfile = () => {
  Object.assign(userInfo, editForm)
  editProfileVisible.value = false
  ElMessage.success('个人资料修改成功')
}

// --- 修改密码逻辑 ---
const passwordVisible = ref(false)
const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

const openChangePasswordDialog = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordVisible.value = true
}

// 当关闭弹窗时，清除 URL 中的 action 参数
const handlePasswordDialogClose = () => {
  if (route.query.action) {
    const query = { ...route.query }
    delete query.action
    router.replace({ query })
  }
}

const submitPasswordChange = () => {
  if (!passwordFormRef.value) return
  passwordFormRef.value.validate((valid) => {
    if (valid) {
      ElMessage.success('密码修改成功，请重新登录')
      passwordVisible.value = false
    } else {
      return false
    }
  })
}

// --- 路由监听与初始化 ---
const handleTabChange = (name) => {
  router.replace({ query: { tab: name } })
}

const checkAutoOpen = (query) => {
  if (query.tab) {
    activeTab.value = query.tab
  }
  if (query.tab === 'security' && query.action === 'password') {
    openChangePasswordDialog()
  }
}

onMounted(() => {
  checkAutoOpen(route.query)
})

watch(
  () => route.query,
  (newQuery) => {
    checkAutoOpen(newQuery)
  },
  { deep: true }
)
</script>

<style scoped>
.user-profile-container {
  padding: 0;
}

.box-card {
  margin-bottom: 20px;
}

.profile-header {
  text-align: center;
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.profile-name {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-top: 15px;
}

.profile-role {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.profile-body {
  padding: 20px 10px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  font-size: 14px;
  color: #606266;
}

.info-item .el-icon {
  margin-right: 8px;
  font-size: 16px;
}

.info-item .label {
  margin-right: 8px;
  font-weight: 500;
  min-width: 45px;
}

.w-100 {
  width: 100%;
}

/* 安全设置样式 */
.security-list {
  padding: 0 10px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.sec-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 6px;
  font-weight: 500;
}

.sec-desc {
  font-size: 13px;
  color: #909399;
}
</style>
