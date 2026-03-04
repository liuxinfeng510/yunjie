<template>
  <div style="height:100vh; display:flex; justify-content:center; align-items:center; background:linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
    <el-card style="width:400px; border-radius:12px;" shadow="always">
      <div style="text-align:center; margin-bottom:30px;">
        <h1 style="color:#409eff; font-size:26px; margin-bottom:8px;">云界智慧药房管理系统</h1>
        <p style="color:#666; font-size:13px; font-weight:500;">CloudRealm Smart Pharmacy</p>
        <p style="color:#999; font-size:12px; margin-top:6px;">GSP合规 / SaaS多租户 / AI智能</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" placeholder="密码" prefix-icon="Lock" size="large"
            type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin"
            style="width:100%;" size="large">登 录</el-button>
        </el-form-item>
      </el-form>
      <div style="text-align:center; color:#999; font-size:12px;">默认账号: admin / Admin@123</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>
