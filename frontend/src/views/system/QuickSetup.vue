<template>
  <div class="quick-setup">
    <el-card>
      <template #header>
        <span>快速部署向导</span>
      </template>

      <el-steps :active="currentStep" finish-status="success" align-center style="margin-bottom: 30px;">
        <el-step title="基础配置" description="企业信息设置" />
        <el-step title="门店设置" description="添加门店信息" />
        <el-step title="初始数据" description="导入基础数据" />
        <el-step title="完成部署" description="系统初始化完成" />
      </el-steps>

      <!-- 步骤1：基础配置 -->
      <div v-show="currentStep === 0" class="step-content">
        <el-form ref="basicFormRef" :model="basicForm" :rules="basicRules" label-width="140px" style="max-width: 600px; margin: 0 auto;">
          <el-form-item label="企业名称" prop="companyName">
            <el-input v-model="basicForm.companyName" placeholder="请输入企业名称" />
          </el-form-item>
          <el-form-item label="统一社会信用代码" prop="creditCode">
            <el-input v-model="basicForm.creditCode" placeholder="请输入统一社会信用代码" />
          </el-form-item>
          <el-form-item label="经营模式" prop="businessMode">
            <el-radio-group v-model="basicForm.businessMode">
              <el-radio label="single_store">单店模式</el-radio>
              <el-radio label="chain_store">连锁模式</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="联系人" prop="contactName">
            <el-input v-model="basicForm.contactName" placeholder="请输入联系人姓名" />
          </el-form-item>
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="basicForm.contactPhone" placeholder="请输入联系电话" />
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤2：门店设置 -->
      <div v-show="currentStep === 1" class="step-content">
        <el-form ref="storeFormRef" :model="storeForm" :rules="storeRules" label-width="120px" style="max-width: 600px; margin: 0 auto;">
          <el-form-item label="门店名称" prop="storeName">
            <el-input v-model="storeForm.storeName" placeholder="请输入门店名称" />
          </el-form-item>
          <el-form-item label="门店类型" prop="storeType">
            <el-select v-model="storeForm.storeType" placeholder="请选择门店类型">
              <el-option label="零售药店" value="retail" />
              <el-option label="批发企业" value="wholesale" />
              <el-option label="连锁总部" value="headquarters" />
            </el-select>
          </el-form-item>
          <el-form-item label="门店地址" prop="address">
            <el-input v-model="storeForm.address" placeholder="请输入门店地址" />
          </el-form-item>
          <el-form-item label="GSP证书号" prop="gspCertNo">
            <el-input v-model="storeForm.gspCertNo" placeholder="请输入GSP证书号" />
          </el-form-item>
          <el-form-item label="营业执照号" prop="businessLicense">
            <el-input v-model="storeForm.businessLicense" placeholder="请输入营业执照号" />
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤3：初始数据 -->
      <div v-show="currentStep === 2" class="step-content">
        <div style="max-width: 600px; margin: 0 auto;">
          <el-alert title="初始化数据" type="info" :closable="false" style="margin-bottom: 20px;">
            系统将自动初始化以下基础数据，请根据需要选择导入项目
          </el-alert>
          <el-checkbox-group v-model="initDataOptions">
            <div class="init-option">
              <el-checkbox label="drugCategory">药品分类（国家基本药物目录分类）</el-checkbox>
            </div>
            <div class="init-option">
              <el-checkbox label="commonDrugs">常用药品数据（500+常见药品）</el-checkbox>
            </div>
            <div class="init-option">
              <el-checkbox label="herbData">中药饮片数据（常用中药材）</el-checkbox>
            </div>
            <div class="init-option">
              <el-checkbox label="memberLevel">会员等级配置（默认3级）</el-checkbox>
            </div>
            <div class="init-option">
              <el-checkbox label="gspTemplate">GSP检查模板</el-checkbox>
            </div>
          </el-checkbox-group>
        </div>
      </div>

      <!-- 步骤4：完成 -->
      <div v-show="currentStep === 3" class="step-content">
        <el-result icon="success" title="部署完成" sub-title="系统已完成初始化，可以开始使用">
          <template #extra>
            <el-button type="primary" @click="goToDashboard">进入系统</el-button>
          </template>
        </el-result>
      </div>

      <!-- 底部按钮 -->
      <div class="step-buttons" v-if="currentStep < 3">
        <el-button v-if="currentStep > 0" @click="prevStep">上一步</el-button>
        <el-button type="primary" @click="nextStep" :loading="submitting">
          {{ currentStep === 2 ? '开始部署' : '下一步' }}
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { quickSetup } from '@/api/system'

const router = useRouter()
const currentStep = ref(0)
const submitting = ref(false)
const basicFormRef = ref(null)
const storeFormRef = ref(null)

const basicForm = reactive({
  companyName: '',
  creditCode: '',
  businessMode: 'single_store',
  contactName: '',
  contactPhone: ''
})

const storeForm = reactive({
  storeName: '',
  storeType: 'retail',
  address: '',
  gspCertNo: '',
  businessLicense: ''
})

const initDataOptions = ref(['drugCategory', 'memberLevel', 'gspTemplate'])

const basicRules = {
  companyName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  businessMode: [{ required: true, message: '请选择经营模式', trigger: 'change' }]
}

const storeRules = {
  storeName: [{ required: true, message: '请输入门店名称', trigger: 'blur' }],
  storeType: [{ required: true, message: '请选择门店类型', trigger: 'change' }]
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const nextStep = async () => {
  if (currentStep.value === 0) {
    const valid = await basicFormRef.value.validate().catch(() => false)
    if (!valid) return
  } else if (currentStep.value === 1) {
    const valid = await storeFormRef.value.validate().catch(() => false)
    if (!valid) return
  } else if (currentStep.value === 2) {
    await doSetup()
    return
  }
  currentStep.value++
}

const doSetup = async () => {
  submitting.value = true
  try {
    await quickSetup({
      ...basicForm,
      store: storeForm,
      initData: initDataOptions.value
    })
    ElMessage.success('部署成功')
    currentStep.value = 3
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '部署失败')
  } finally {
    submitting.value = false
  }
}

const goToDashboard = () => {
  router.push('/dashboard')
}
</script>

<style scoped>
.quick-setup {
  padding: 20px;
}
.step-content {
  min-height: 300px;
  padding: 20px 0;
}
.step-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
.init-option {
  margin-bottom: 16px;
}
</style>
