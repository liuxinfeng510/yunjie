<template>
  <div class="tenant-create">
    <el-card>
      <template #header>
        <div style="display: flex; align-items: center;">
          <el-button link @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <span style="margin-left: 8px;">开通新租户</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" style="max-width: 600px;">
        <el-divider content-position="left">基本信息</el-divider>
        
        <el-form-item label="租户名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入企业/药房名称" />
        </el-form-item>
        
        <el-form-item label="租户编码" prop="code">
          <el-input v-model="form.code" placeholder="唯一标识，如: yf001" />
        </el-form-item>
        
        <el-form-item label="信用代码" prop="creditCode">
          <el-input v-model="form.creditCode" placeholder="统一社会信用代码" />
        </el-form-item>
        
        <el-form-item label="经营模式" prop="businessMode">
          <el-radio-group v-model="form.businessMode">
            <el-radio-button label="single_store">
              <el-icon><Shop /></el-icon> 单店模式
            </el-radio-button>
            <el-radio-button label="chain">
              <el-icon><OfficeBuilding /></el-icon> 连锁模式
            </el-radio-button>
          </el-radio-group>
          <div class="form-tip">
            {{ form.businessMode === 'single_store' ? '适用于单个门店经营' : '适用于多门店连锁经营，支持门店管理' }}
          </div>
        </el-form-item>

        <el-divider content-position="left">联系信息</el-divider>
        
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>
        
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>

        <el-divider content-position="left">管理员账号</el-divider>
        
        <el-form-item label="管理员账号" prop="adminUsername">
          <el-input v-model="form.adminUsername" placeholder="租户管理员登录账号" />
        </el-form-item>
        
        <el-form-item label="初始密码" prop="adminPassword">
          <el-input v-model="form.adminPassword" type="password" show-password placeholder="初始登录密码" />
        </el-form-item>
        
        <el-form-item label="管理员姓名" prop="adminRealName">
          <el-input v-model="form.adminRealName" placeholder="管理员真实姓名" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            立即开通
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)

const form = ref({
  name: '',
  code: '',
  creditCode: '',
  businessMode: 'single_store',
  contactName: '',
  contactPhone: '',
  adminUsername: '',
  adminPassword: '',
  adminRealName: ''
})

const rules = {
  name: [{ required: true, message: '请输入租户名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入租户编码', trigger: 'blur' }],
  businessMode: [{ required: true, message: '请选择经营模式', trigger: 'change' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  adminUsername: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  adminPassword: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  adminRealName: [{ required: true, message: '请输入管理员姓名', trigger: 'blur' }]
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const res = await request.post('/tenant', form.value)
    if (res.code === 200) {
      ElMessage.success('租户开通成功')
      router.push('/admin/tenant')
    } else {
      ElMessage.error(res.message || '开通失败')
    }
  } catch (e) {
    ElMessage.error('开通失败')
  } finally {
    submitting.value = false
  }
}

function resetForm() {
  formRef.value?.resetFields()
}
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
