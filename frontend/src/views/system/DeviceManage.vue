<template>
  <div class="device-manage-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>设备管理</span>
          <el-tag :type="connectionStatus === 'connected' ? 'success' : 'info'">
            {{ connectionStatus === 'connected' ? 'WebSocket 已连接' : 'WebSocket 未连接' }}
          </el-tag>
        </div>
      </template>

      <!-- 操作区 -->
      <div class="toolbar">
        <el-button type="primary" @click="connectScale">连接模拟电子秤</el-button>
        <el-button type="success" @click="connectCamera">连接模拟摄像头</el-button>
        <el-button @click="refreshDevices">刷新设备列表</el-button>
      </div>

      <!-- 设备列表 -->
      <el-table :data="deviceList" style="width: 100%; margin-top: 16px" border>
        <el-table-column prop="deviceId" label="设备ID" min-width="200" />
        <el-table-column prop="type" label="设备类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 'scale' ? 'primary' : 'success'">
              {{ row.type === 'scale' ? '电子秤' : '摄像头' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ONLINE' ? 'success' : 'danger'">
              {{ row.status === 'ONLINE' ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="disconnect(row.deviceId)">断开</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 电子秤测试卡片 -->
    <el-card shadow="never" style="margin-top: 16px" v-if="currentScaleId">
      <template #header>
        <span>电子秤测试 - {{ currentScaleId }}</span>
      </template>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-statistic title="当前重量 (g)">
            <template #default>
              <span style="font-size: 36px; color: #409eff">{{ currentWeight }}</span>
            </template>
          </el-statistic>
        </el-col>
        <el-col :span="8">
          <el-button type="primary" size="large" @click="readWeight">读取重量</el-button>
          <el-button type="warning" size="large" @click="tareScale">去皮</el-button>
        </el-col>
        <el-col :span="8">
          <div>
            <span>模拟设置重量:</span>
            <el-input-number
              v-model="simulatedWeight"
              :min="0"
              :max="10000"
              :step="10"
              style="margin-left: 8px"
            />
            <el-button @click="setWeight" style="margin-left: 8px">设置</el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 摄像头测试卡片 -->
    <el-card shadow="never" style="margin-top: 16px" v-if="currentCameraId">
      <template #header>
        <span>摄像头测试 - {{ currentCameraId }}</span>
      </template>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-button type="primary" size="large" @click="captureImage">拍照</el-button>
        </el-col>
        <el-col :span="16">
          <div v-if="capturedImage" style="max-width: 300px">
            <img :src="'data:image/png;base64,' + capturedImage" style="max-width: 100%" />
          </div>
          <el-empty v-else description="暂无图片" />
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getDeviceList, connectSimulatedScale, connectSimulatedCamera, disconnectDevice } from '@/api/device'
import { useWebSocket } from '@/utils/websocket'

const { connected, connect: wsConnect, subscribe, send } = useWebSocket()

const connectionStatus = computed(() => connected.value ? 'connected' : 'disconnected')
const deviceList = ref([])
const currentScaleId = ref('')
const currentCameraId = ref('')
const currentWeight = ref('0.00')
const simulatedWeight = ref(100)
const capturedImage = ref('')

// 刷新设备列表
async function refreshDevices() {
  try {
    const res = await getDeviceList()
    const devices = []
    for (const [deviceId, info] of Object.entries(res.data || {})) {
      devices.push({ deviceId, ...info })
    }
    deviceList.value = devices

    // 更新当前选中的设备
    const scale = devices.find(d => d.type === 'scale' && d.status === 'ONLINE')
    const camera = devices.find(d => d.type === 'camera' && d.status === 'ONLINE')
    if (scale) currentScaleId.value = scale.deviceId
    if (camera) currentCameraId.value = camera.deviceId
  } catch (e) {
    console.error('获取设备列表失败', e)
  }
}

// 连接模拟电子秤
async function connectScale() {
  try {
    const res = await connectSimulatedScale()
    currentScaleId.value = res.data.deviceId
    ElMessage.success('模拟电子秤已连接: ' + res.data.deviceId)
    await refreshDevices()
  } catch (e) {
    ElMessage.error('连接失败')
  }
}

// 连接模拟摄像头
async function connectCamera() {
  try {
    const res = await connectSimulatedCamera()
    currentCameraId.value = res.data.deviceId
    ElMessage.success('模拟摄像头已连接: ' + res.data.deviceId)
    await refreshDevices()
  } catch (e) {
    ElMessage.error('连接失败')
  }
}

// 断开设备
async function disconnect(deviceId) {
  try {
    await disconnectDevice(deviceId)
    ElMessage.success('设备已断开')
    if (deviceId === currentScaleId.value) currentScaleId.value = ''
    if (deviceId === currentCameraId.value) currentCameraId.value = ''
    await refreshDevices()
  } catch (e) {
    ElMessage.error('断开失败')
  }
}

// 读取重量（通过WebSocket）
function readWeight() {
  if (!currentScaleId.value) {
    ElMessage.warning('请先连接电子秤')
    return
  }
  send(`/app/scale/${currentScaleId.value}/read`, {})
}

// 去皮
function tareScale() {
  if (!currentScaleId.value) {
    ElMessage.warning('请先连接电子秤')
    return
  }
  send(`/app/scale/${currentScaleId.value}/tare`, {})
  ElMessage.success('已去皮')
}

// 设置模拟重量
function setWeight() {
  // 这里仅前端演示，实际可通过API调用后端模拟设备
  currentWeight.value = simulatedWeight.value.toFixed(2)
  ElMessage.success('模拟重量已设置')
}

// 拍照
function captureImage() {
  if (!currentCameraId.value) {
    ElMessage.warning('请先连接摄像头')
    return
  }
  send(`/app/camera/${currentCameraId.value}/capture`, {})
}

onMounted(async () => {
  try {
    await wsConnect()
  } catch (e) {
    console.warn('WebSocket 连接失败，部分实时功能不可用')
  }

  // 订阅电子秤数据
  subscribe('/topic/scale/*', (data) => {
    if (data.weight !== undefined) {
      currentWeight.value = parseFloat(data.weight).toFixed(2)
    }
  })

  // 订阅摄像头数据
  subscribe('/topic/camera/*', (data) => {
    if (data.image) {
      capturedImage.value = data.image
    }
  })

  await refreshDevices()
})
</script>

<style scoped>
.device-manage-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar {
  display: flex;
  gap: 12px;
}
</style>
