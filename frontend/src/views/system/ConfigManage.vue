<template>
  <div class="page-container">
    <!-- 未完成初始化配置的引导 -->
    <el-alert v-if="!setupCompleted" title="系统尚未完成初始化配置" type="warning" show-icon :closable="false"
      description="请先完成快速配置，选择经营模式并填写基本信息" style="margin-bottom: 20px;">
      <template #default>
        <el-button type="primary" size="small" @click="showSetupDialog = true" style="margin-top: 8px;">
          开始快速配置
        </el-button>
      </template>
    </el-alert>

    <el-tabs v-model="activeTab">
      <!-- 基础配置 -->
      <el-tab-pane label="基础配置" name="basic">
        <el-card shadow="never">
          <template #header>
            <span>基础信息</span>
          </template>
          <el-form label-width="160px" style="max-width: 600px;">
            <el-form-item label="经营模式">
              <el-tag :type="configs['system.business_mode'] === 'chain_store' ? 'primary' : 'success'" size="large">
                {{ configs['system.business_mode'] === 'chain_store' ? '连锁药店' : '单体药店' }}
              </el-tag>
            </el-form-item>
            <el-form-item label="企业名称">
              <el-input v-model="configs['system.company_name']" @blur="saveConfig('basic', 'system.company_name')" />
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 功能开关 -->
      <el-tab-pane label="功能开关" name="feature">
        <el-card shadow="never">
          <template #header>
            <span>模块功能开关</span>
          </template>
          <el-form label-width="160px" style="max-width: 600px;">
            <el-form-item label="中药管理">
              <el-switch v-model="featureToggles['feature.herb']" @change="saveFeature('feature.herb')" />
            </el-form-item>
            <el-form-item label="会员管理">
              <el-switch v-model="featureToggles['feature.member']" @change="saveFeature('feature.member')" />
            </el-form-item>
            <el-form-item label="GSP管理">
              <el-switch v-model="featureToggles['feature.gsp']" @change="saveFeature('feature.gsp')" />
            </el-form-item>
            <el-form-item label="处方管理">
              <el-switch v-model="featureToggles['feature.prescription']" @change="saveFeature('feature.prescription')" />
            </el-form-item>
            <el-form-item label="电子秤集成">
              <el-switch v-model="featureToggles['feature.scale']" @change="saveFeature('feature.scale')" />
            </el-form-item>
            <el-form-item label="温湿度监控">
              <el-switch v-model="featureToggles['feature.temp_humidity']" @change="saveFeature('feature.temp_humidity')" />
            </el-form-item>
            <el-form-item label="数据分析">
              <el-switch v-model="featureToggles['feature.analytics']" @change="saveFeature('feature.analytics')" />
            </el-form-item>
            <el-divider v-if="configs['system.business_mode'] === 'chain_store'" content-position="left">连锁模式专属</el-divider>
            <template v-if="configs['system.business_mode'] === 'chain_store'">
              <el-form-item label="总部统一采购">
                <el-switch v-model="featureToggles['feature.central_purchase']" @change="saveFeature('feature.central_purchase')" />
              </el-form-item>
              <el-form-item label="跨店调拨">
                <el-switch v-model="featureToggles['feature.cross_transfer']" @change="saveFeature('feature.cross_transfer')" />
              </el-form-item>
            </template>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- GSP配置 -->
      <el-tab-pane label="GSP配置" name="gsp">
        <el-card shadow="never">
          <template #header>
            <span>GSP合规参数</span>
          </template>
          <el-form label-width="160px" style="max-width: 600px;">
            <el-form-item label="入库需要验收">
              <el-switch :model-value="configs['gsp.acceptance_required'] === 'true'"
                @change="v => saveConfig('gsp', 'gsp.acceptance_required', v ? 'true' : 'false')" />
            </el-form-item>
            <el-form-item label="近效期预警天数">
              <el-input-number v-model.number="gspNearExpiryDays" :min="30" :max="365"
                @change="saveConfig('gsp', 'gsp.near_expiry_days', String(gspNearExpiryDays))" />
              <span style="margin-left: 8px; color: #909399;">天</span>
            </el-form-item>
            <el-form-item label="温湿度记录间隔">
              <el-input-number v-model.number="gspTempInterval" :min="30" :max="480"
                @change="saveConfig('gsp', 'gsp.temp_check_interval', String(gspTempInterval))" />
              <span style="margin-left: 8px; color: #909399;">分钟</span>
            </el-form-item>
            <el-form-item label="养护周期">
              <el-input-number v-model.number="gspMaintenanceCycle" :min="7" :max="90"
                @change="saveConfig('gsp', 'gsp.maintenance_cycle', String(gspMaintenanceCycle))" />
              <span style="margin-left: 8px; color: #909399;">天</span>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 库存配置 -->
      <el-tab-pane label="库存配置" name="inventory">
        <el-card shadow="never">
          <template #header>
            <span>库存管理参数</span>
          </template>
          <el-form label-width="160px" style="max-width: 600px;">
            <el-form-item label="库存不足预警阈值">
              <el-input-number v-model.number="invLowThreshold" :min="1" :max="999"
                @change="saveConfig('inventory', 'inventory.low_stock_threshold', String(invLowThreshold))" />
            </el-form-item>
            <el-form-item label="自动生成补货单">
              <el-switch :model-value="configs['inventory.auto_reorder'] === 'true'"
                @change="v => saveConfig('inventory', 'inventory.auto_reorder', v ? 'true' : 'false')" />
            </el-form-item>
            <el-form-item label="启用批号追踪">
              <el-switch :model-value="configs['inventory.batch_tracking'] === 'true'"
                @change="v => saveConfig('inventory', 'inventory.batch_tracking', v ? 'true' : 'false')" />
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 销售配置 -->
      <el-tab-pane label="销售配置" name="sale">
        <el-card shadow="never">
          <template #header>
            <span>销售参数</span>
          </template>
          <el-form label-width="160px" style="max-width: 600px;">
            <el-form-item label="允许赊账">
              <el-switch :model-value="configs['sale.allow_credit'] === 'true'"
                @change="v => saveConfig('sale', 'sale.allow_credit', v ? 'true' : 'false')" />
            </el-form-item>
            <el-form-item label="小票打印">
              <el-switch :model-value="configs['sale.receipt_printer'] === 'true'"
                @change="v => saveConfig('sale', 'sale.receipt_printer', v ? 'true' : 'false')" />
            </el-form-item>
            <el-form-item label="会员积分比例">
              <el-input-number v-model.number="salePointsRatio" :min="0" :max="100" :precision="1"
                @change="saveConfig('sale', 'sale.member_points_ratio', String(salePointsRatio))" />
              <span style="margin-left: 8px; color: #909399;">消费1元 = N积分</span>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 快速配置对话框 -->
    <el-dialog v-model="showSetupDialog" title="快速配置向导" width="700px" :close-on-click-modal="false">
      <el-steps :active="setupStep" align-center style="margin-bottom: 24px;">
        <el-step title="经营模式" />
        <el-step title="企业信息" />
        <el-step title="门店信息" />
        <el-step title="功能开关" />
      </el-steps>

      <!-- Step 0: 经营模式 -->
      <div v-if="setupStep === 0" style="text-align: center; padding: 20px 0;">
        <p style="margin-bottom: 20px; color: #606266;">请选择您的经营模式</p>
        <el-radio-group v-model="setupForm.businessMode" size="large">
          <el-radio-button value="single_store">
            <div style="padding: 10px 20px;">
              <div style="font-size: 16px; font-weight: bold;">单体药店</div>
              <div style="font-size: 12px; color: #909399; margin-top: 4px;">独立经营的单家药店</div>
            </div>
          </el-radio-button>
          <el-radio-button value="chain_store">
            <div style="padding: 10px 20px;">
              <div style="font-size: 16px; font-weight: bold;">连锁药店</div>
              <div style="font-size: 12px; color: #909399; margin-top: 4px;">含总部和多家分店</div>
            </div>
          </el-radio-button>
        </el-radio-group>
      </div>

      <!-- Step 1: 企业信息 -->
      <el-form v-if="setupStep === 1" :model="setupForm" label-width="120px" style="max-width: 500px; margin: 0 auto;">
        <el-form-item label="企业名称" required>
          <el-input v-model="setupForm.companyName" placeholder="请输入企业/药店名称" />
        </el-form-item>
        <el-form-item label="信用代码">
          <el-input v-model="setupForm.creditCode" placeholder="统一社会信用代码" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="setupForm.contactName" placeholder="负责人姓名" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="setupForm.contactPhone" placeholder="联系电话" />
        </el-form-item>
      </el-form>

      <!-- Step 2: 门店信息 -->
      <div v-if="setupStep === 2">
        <el-form :model="setupForm.mainStore" label-width="120px" style="max-width: 500px; margin: 0 auto;">
          <h4 style="margin-bottom: 12px;">{{ setupForm.businessMode === 'chain_store' ? '总部信息' : '门店信息' }}</h4>
          <el-form-item label="门店名称" required>
            <el-input v-model="setupForm.mainStore.name" placeholder="门店名称" />
          </el-form-item>
          <el-form-item label="地址">
            <el-input v-model="setupForm.mainStore.address" placeholder="详细地址" />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="setupForm.mainStore.phone" placeholder="门店电话" />
          </el-form-item>
          <el-form-item label="营业执照号">
            <el-input v-model="setupForm.mainStore.licenseNo" placeholder="营业执照号" />
          </el-form-item>
          <el-form-item label="GSP证书号">
            <el-input v-model="setupForm.mainStore.gspCertNo" placeholder="GSP证书编号" />
          </el-form-item>
        </el-form>
      </div>

      <!-- Step 3: 功能开关 -->
      <div v-if="setupStep === 3">
        <el-form label-width="140px" style="max-width: 500px; margin: 0 auto;">
          <el-form-item label="中药管理">
            <el-switch v-model="setupForm.features.enableHerb" />
          </el-form-item>
          <el-form-item label="会员管理">
            <el-switch v-model="setupForm.features.enableMember" />
          </el-form-item>
          <el-form-item label="GSP合规">
            <el-switch v-model="setupForm.features.enableGsp" />
          </el-form-item>
          <el-form-item label="处方管理">
            <el-switch v-model="setupForm.features.enablePrescription" />
          </el-form-item>
          <el-form-item label="电子秤集成">
            <el-switch v-model="setupForm.features.enableScale" />
          </el-form-item>
          <el-form-item label="温湿度监控">
            <el-switch v-model="setupForm.features.enableTempHumidity" />
          </el-form-item>
          <template v-if="setupForm.businessMode === 'chain_store'">
            <el-divider content-position="left">连锁模式</el-divider>
            <el-form-item label="总部统一采购">
              <el-switch v-model="setupForm.features.enableCentralPurchase" />
            </el-form-item>
            <el-form-item label="跨店调拨">
              <el-switch v-model="setupForm.features.enableCrossTransfer" />
            </el-form-item>
          </template>
        </el-form>
      </div>

      <template #footer>
        <el-button v-if="setupStep > 0" @click="setupStep--">上一步</el-button>
        <el-button v-if="setupStep < 3" type="primary" @click="setupStep++">下一步</el-button>
        <el-button v-if="setupStep === 3" type="primary" :loading="setupLoading" @click="submitSetup">
          完成配置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllConfig, setConfigValue, getSetupStatus, quickSetup, getFeatures, updateFeatures } from '@/api/system'

const activeTab = ref('basic')
const configs = reactive({})
const featureToggles = reactive({})
const setupCompleted = ref(true)

// GSP数值配置
const gspNearExpiryDays = ref(180)
const gspTempInterval = ref(120)
const gspMaintenanceCycle = ref(30)
const invLowThreshold = ref(10)
const salePointsRatio = ref(1)

// 快速配置向导
const showSetupDialog = ref(false)
const setupStep = ref(0)
const setupLoading = ref(false)
const setupForm = reactive({
  businessMode: 'single_store',
  companyName: '',
  creditCode: '',
  contactName: '',
  contactPhone: '',
  mainStore: {
    name: '',
    address: '',
    phone: '',
    licenseNo: '',
    gspCertNo: ''
  },
  features: {
    enableHerb: true,
    enableMember: true,
    enableGsp: true,
    enablePrescription: true,
    enableScale: false,
    enableTempHumidity: true,
    enableAnalytics: true,
    enableCentralPurchase: false,
    enableCrossTransfer: false
  }
})

onMounted(() => {
  loadConfigs()
  checkSetupStatus()
})

async function loadConfigs() {
  try {
    const res = await getAllConfig()
    const grouped = res.data || {}
    // 扁平化所有配置到 configs
    for (const group in grouped) {
      const list = grouped[group]
      if (Array.isArray(list)) {
        list.forEach(item => {
          configs[item.configKey] = item.configValue
        })
      }
    }
    // 提取功能开关为布尔值
    const features = grouped['feature'] || []
    features.forEach(item => {
      featureToggles[item.configKey] = item.configValue === 'true'
    })
    // 提取数值配置
    gspNearExpiryDays.value = parseInt(configs['gsp.near_expiry_days'] || '180')
    gspTempInterval.value = parseInt(configs['gsp.temp_check_interval'] || '120')
    gspMaintenanceCycle.value = parseInt(configs['gsp.maintenance_cycle'] || '30')
    invLowThreshold.value = parseInt(configs['inventory.low_stock_threshold'] || '10')
    salePointsRatio.value = parseFloat(configs['sale.member_points_ratio'] || '1')
  } catch (e) {
    console.error('加载配置失败', e)
  }
}

async function checkSetupStatus() {
  try {
    const res = await getSetupStatus()
    setupCompleted.value = res.data?.setupCompleted || false
  } catch (e) {
    console.error('检查初始化状态失败', e)
  }
}

async function saveConfig(group, key, value) {
  try {
    const val = value !== undefined ? value : configs[key]
    await setConfigValue({ group, key, value: val })
    if (value !== undefined) {
      configs[key] = val
    }
    ElMessage.success('配置已保存')
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

async function saveFeature(key) {
  try {
    await updateFeatures({ [key]: String(featureToggles[key]) })
    ElMessage.success('功能开关已更新')
  } catch (e) {
    ElMessage.error('更新失败')
  }
}

async function submitSetup() {
  if (!setupForm.companyName) {
    ElMessage.warning('请填写企业名称')
    setupStep.value = 1
    return
  }
  if (!setupForm.mainStore.name) {
    ElMessage.warning('请填写门店名称')
    setupStep.value = 2
    return
  }
  setupLoading.value = true
  try {
    await quickSetup(setupForm)
    ElMessage.success('初始化配置完成')
    showSetupDialog.value = false
    setupCompleted.value = true
    loadConfigs()
  } catch (e) {
    ElMessage.error('配置失败: ' + (e.response?.data?.message || e.message))
  } finally {
    setupLoading.value = false
  }
}
</script>
