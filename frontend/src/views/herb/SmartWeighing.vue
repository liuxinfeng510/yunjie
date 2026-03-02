<template>
  <div class="smart-weighing-container">
    <el-row :gutter="20">
      <!-- 左侧：称重操作区 -->
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>智能称重</span>
              <el-tag v-if="selectedDevice" type="success">{{ selectedDevice.deviceName }} - 在线</el-tag>
              <el-tag v-else type="info">未连接设备</el-tag>
            </div>
          </template>

          <!-- 设备选择 -->
          <el-form label-width="100px">
            <el-form-item label="选择设备">
              <el-select v-model="selectedDeviceId" placeholder="选择电子秤" style="width: 200px" @change="onDeviceChange">
                <el-option
                  v-for="d in devices"
                  :key="d.id"
                  :label="d.deviceName"
                  :value="d.id"
                />
              </el-select>
              <el-button type="primary" link @click="connectSimDevice" style="margin-left: 12px">
                连接模拟设备
              </el-button>
            </el-form-item>

            <el-form-item label="当前重量">
              <div class="weight-display">
                <span class="weight-value">{{ currentWeight }}</span>
                <span class="weight-unit">g</span>
              </div>
              <el-button @click="readWeight" :disabled="!selectedDevice">读取</el-button>
              <el-button @click="tareScale" :disabled="!selectedDevice">去皮</el-button>
            </el-form-item>

            <el-form-item label="操作类型">
              <el-radio-group v-model="operationType">
                <el-radio-button value="dispense">调配</el-radio-button>
                <el-radio-button value="stock_in">入库</el-radio-button>
                <el-radio-button value="stock_out">出库</el-radio-button>
                <el-radio-button value="check">盘点</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-form>

          <!-- 图片上传识别 -->
          <el-divider>AI识别</el-divider>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-upload
                class="upload-area"
                drag
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleImageChange"
                accept="image/*"
              >
                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                <div class="el-upload__text">拍摄或上传中药图片</div>
              </el-upload>
              <div v-if="imagePreview" style="margin-top: 12px">
                <img :src="imagePreview" style="max-width: 100%; max-height: 200px" />
              </div>
            </el-col>
            <el-col :span="12">
              <el-button type="primary" size="large" :loading="recognizing" @click="doSmartWeighing" :disabled="!currentWeight || currentWeight === '0.00'">
                智能称重
              </el-button>

              <div v-if="weighingResult" style="margin-top: 16px">
                <el-descriptions :column="1" border size="small">
                  <el-descriptions-item label="识别结果">
                    <el-tag :type="weighingResult.success ? 'success' : 'danger'">
                      {{ weighingResult.matchedHerbName || '未识别' }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="置信度" v-if="weighingResult.confidence">
                    {{ weighingResult.confidence.toFixed(1) }}%
                  </el-descriptions-item>
                  <el-descriptions-item label="识别方式">
                    {{ weighingResult.recognitionMethod === 'ai' ? 'AI识别' : '手动选择' }}
                  </el-descriptions-item>
                </el-descriptions>

                <!-- 配伍警告 -->
                <el-alert
                  v-if="weighingResult.hasIncompatibility"
                  type="warning"
                  :closable="false"
                  style="margin-top: 12px"
                >
                  <template #title>
                    <span style="font-weight: bold">存在配伍禁忌!</span>
                  </template>
                  <div v-for="w in weighingResult.incompatibilityCheck?.warnings" :key="w.herbA + w.herbB">
                    {{ w.typeName }}: {{ w.herbA }} 与 {{ w.herbB }} - {{ w.description }}
                  </div>
                </el-alert>
              </div>
            </el-col>
          </el-row>

          <!-- 手动选择药材 -->
          <el-divider>手动选择</el-divider>
          <el-form-item label="选择中药">
            <el-select
              v-model="manualHerbId"
              filterable
              remote
              :remote-method="searchHerbs"
              placeholder="输入药材名称搜索"
              style="width: 300px"
            >
              <el-option
                v-for="h in herbOptions"
                :key="h.id"
                :label="h.name"
                :value="h.id"
              />
            </el-select>
          </el-form-item>
        </el-card>
      </el-col>

      <!-- 右侧：处方药材列表 -->
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>当前处方药材</span>
              <el-button type="danger" size="small" @click="clearPrescription">清空</el-button>
            </div>
          </template>

          <el-table :data="prescriptionItems" size="small" border max-height="300">
            <el-table-column prop="herbName" label="药材" />
            <el-table-column prop="weight" label="重量(g)" width="100" />
            <el-table-column label="操作" width="80">
              <template #default="{ $index }">
                <el-button type="danger" size="small" link @click="removeItem($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div style="margin-top: 16px">
            <el-statistic title="总重量">
              <template #default>
                <span style="font-size: 24px">{{ totalWeight }}</span>
                <span style="font-size: 14px; margin-left: 4px">g</span>
              </template>
            </el-statistic>
          </div>

          <el-button type="primary" style="margin-top: 16px" @click="checkAllCompatibility" :disabled="prescriptionItems.length < 2">
            检查配伍
          </el-button>

          <!-- 配伍检查结果 -->
          <div v-if="compatibilityResult" style="margin-top: 16px">
            <el-alert
              :type="compatibilityResult.safe ? 'success' : 'error'"
              :closable="false"
            >
              <template #title>
                {{ compatibilityResult.safe ? '配伍安全' : `发现 ${compatibilityResult.forbiddenCount + compatibilityResult.cautionCount} 项配伍问题` }}
              </template>
              <div v-if="!compatibilityResult.safe">
                <div v-for="w in compatibilityResult.warnings" :key="w.herbA + w.herbB" style="margin-top: 4px">
                  <el-tag :type="w.forbidden ? 'danger' : 'warning'" size="small">{{ w.typeName }}</el-tag>
                  {{ w.herbA }} - {{ w.herbB }}: {{ w.description }}
                </div>
              </div>
            </el-alert>
          </div>
        </el-card>

        <!-- 配伍禁忌速查 -->
        <el-card shadow="never" style="margin-top: 16px">
          <template #header>配伍禁忌速查</template>
          <el-tabs v-model="incompatibilityTab">
            <el-tab-pane label="十八反" name="18oppose">
              <el-table :data="oppose18List" size="small" max-height="200">
                <el-table-column prop="herbA" label="药材A" />
                <el-table-column prop="herbB" label="药材B" />
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="十九畏" name="19fear">
              <el-table :data="fear19List" size="small" max-height="200">
                <el-table-column prop="herbA" label="药材A" />
                <el-table-column prop="herbB" label="药材B" />
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { smartWeighing, checkCompatibility, get18Oppose, get19Fear, getOnlineScales } from '@/api/weighing'
import { connectSimulatedScale } from '@/api/device'
import { getHerbPage } from '@/api/herb'

// 设备相关
const devices = ref([])
const selectedDeviceId = ref(null)
const selectedDevice = computed(() => devices.value.find(d => d.id === selectedDeviceId.value))
const currentWeight = ref('0.00')

// 操作类型
const operationType = ref('dispense')

// 图片识别
const imagePreview = ref('')
const imageBase64 = ref('')
const recognizing = ref(false)
const weighingResult = ref(null)

// 手动选择
const manualHerbId = ref(null)
const herbOptions = ref([])

// 处方药材
const prescriptionItems = ref([])
const totalWeight = computed(() => {
  return prescriptionItems.value.reduce((sum, item) => sum + parseFloat(item.weight || 0), 0).toFixed(2)
})
const compatibilityResult = ref(null)

// 配伍禁忌
const incompatibilityTab = ref('18oppose')
const oppose18List = ref([])
const fear19List = ref([])

async function loadDevices() {
  try {
    // 假设门店ID为1，实际应从store获取
    const res = await getOnlineScales(1)
    devices.value = res.data || []
  } catch (e) {
    console.error('获取设备列表失败', e)
  }
}

async function connectSimDevice() {
  try {
    const res = await connectSimulatedScale()
    ElMessage.success('模拟设备已连接')
    await loadDevices()
    // 自动选择新连接的设备
    if (res.data?.deviceId) {
      // 模拟设备没有在数据库中，这里只是示意
    }
  } catch (e) {
    ElMessage.error('连接失败')
  }
}

function onDeviceChange() {
  currentWeight.value = '0.00'
}

function readWeight() {
  // 模拟读取重量
  currentWeight.value = (Math.random() * 100 + 10).toFixed(2)
}

function tareScale() {
  currentWeight.value = '0.00'
  ElMessage.success('已去皮')
}

function handleImageChange(file) {
  const reader = new FileReader()
  reader.onload = (e) => {
    imagePreview.value = e.target.result
    imageBase64.value = e.target.result.split(',')[1]
  }
  reader.readAsDataURL(file.raw)
}

async function doSmartWeighing() {
  if (!currentWeight.value || currentWeight.value === '0.00') {
    ElMessage.warning('请先称重')
    return
  }

  recognizing.value = true
  try {
    const existingHerbs = prescriptionItems.value.map(item => item.herbName)
    const res = await smartWeighing({
      deviceId: selectedDeviceId.value,
      weight: parseFloat(currentWeight.value),
      imageBase64: imageBase64.value,
      manualHerbId: manualHerbId.value,
      operationType: operationType.value,
      existingHerbs: existingHerbs,
      saveLog: true
    })

    weighingResult.value = res.data

    if (res.data.success && res.data.matchedHerbName) {
      // 添加到处方列表
      prescriptionItems.value.push({
        herbId: res.data.matchedHerbId,
        herbName: res.data.matchedHerbName,
        weight: currentWeight.value
      })

      // 清空当前称重
      currentWeight.value = '0.00'
      imagePreview.value = ''
      imageBase64.value = ''
      manualHerbId.value = null

      if (res.data.hasIncompatibility) {
        ElMessage.warning('存在配伍禁忌，请注意!')
      } else {
        ElMessage.success('称重完成')
      }
    }
  } catch (e) {
    ElMessage.error('称重失败')
  } finally {
    recognizing.value = false
  }
}

async function searchHerbs(query) {
  if (!query) return
  try {
    const res = await getHerbPage({ name: query, pageNum: 1, pageSize: 20 })
    herbOptions.value = res.data?.records || []
  } catch (e) {
    console.error('搜索药材失败', e)
  }
}

function removeItem(index) {
  prescriptionItems.value.splice(index, 1)
  compatibilityResult.value = null
}

function clearPrescription() {
  prescriptionItems.value = []
  compatibilityResult.value = null
}

async function checkAllCompatibility() {
  if (prescriptionItems.value.length < 2) {
    ElMessage.warning('至少需要2味药材')
    return
  }

  try {
    const herbs = prescriptionItems.value.map(item => item.herbName)
    const res = await checkCompatibility(herbs)
    compatibilityResult.value = res.data
  } catch (e) {
    ElMessage.error('检查失败')
  }
}

async function loadIncompatibilityRules() {
  try {
    const [res18, res19] = await Promise.all([get18Oppose(), get19Fear()])
    oppose18List.value = res18.data || []
    fear19List.value = res19.data || []
  } catch (e) {
    console.error('加载配伍禁忌失败', e)
  }
}

onMounted(() => {
  loadDevices()
  loadIncompatibilityRules()
})
</script>

<style scoped>
.smart-weighing-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.weight-display {
  display: inline-flex;
  align-items: baseline;
  background: #f5f7fa;
  padding: 8px 16px;
  border-radius: 4px;
  margin-right: 12px;
}

.weight-value {
  font-size: 36px;
  font-weight: bold;
  color: #409eff;
}

.weight-unit {
  font-size: 18px;
  margin-left: 4px;
  color: #606266;
}

.upload-area {
  width: 100%;
}

:deep(.el-upload-dragger) {
  width: 100%;
  padding: 20px;
}
</style>
