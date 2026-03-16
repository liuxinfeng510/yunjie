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
        
        <el-form-item label="租户编码">
          <el-input placeholder="提交后自动生成" disabled />
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

        <el-form-item label="管理员账号">
          <el-input placeholder="提交后自动生成" disabled />
        </el-form-item>

        <el-form-item label="初始密码">
          <el-input placeholder="提交后自动生成" disabled />
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

    <!-- 开通成功弹窗，展示自动生成的账号密码 -->
    <el-dialog v-model="showResult" title="租户开通成功" width="460px" :close-on-click-modal="false">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="租户编码">{{ resultData.tenantCode }}</el-descriptions-item>
        <el-descriptions-item label="管理员账号">{{ resultData.adminUsername }}</el-descriptions-item>
        <el-descriptions-item label="初始密码">
          <span style="color: #E6A23C; font-weight: bold;">{{ resultData.adminPassword }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <div style="margin-top: 12px; color: #909399; font-size: 13px;">
        请妥善保存以上信息，初始密码建议登录后立即修改。
      </div>
      <template #footer>
        <el-button type="primary" @click="copyAndClose">复制信息并关闭</el-button>
      </template>
    </el-dialog>
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
const showResult = ref(false)
const resultData = ref({ tenantCode: '', adminUsername: '', adminPassword: '' })

const form = ref({
  name: '',
  creditCode: '',
  businessMode: 'single_store',
  contactName: '',
  contactPhone: '',
  adminRealName: ''
})

const rules = {
  name: [{ required: true, message: '请输入租户名称', trigger: 'blur' }],
  businessMode: [{ required: true, message: '请选择经营模式', trigger: 'change' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  adminRealName: [{ required: true, message: '请输入管理员姓名', trigger: 'blur' }]
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const res = await request.post('/tenant', form.value)
    if (res.code === 200) {
      resultData.value = res.data
      showResult.value = true
      ElMessage.success('租户开通成功')
    } else {
      ElMessage.error(res.message || '开通失败')
    }
  } catch (e) {
    ElMessage.error('开通失败')
  } finally {
    submitting.value = false
  }
}

function copyAndClose() {
  const text = `租户编码: ${resultData.value.tenantCode}\n管理员账号: ${resultData.value.adminUsername}\n初始密码: ${resultData.value.adminPassword}`
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.info('请手动复制')
  })
  showResult.value = false
  router.push('/admin/tenant')
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
