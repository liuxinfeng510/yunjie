<template>
  <div class="expiry-warning-container">
    <!-- 预警概览卡片 -->
    <el-row :gutter="16" style="margin-bottom: 20px">
      <el-col :span="8">
        <el-card shadow="hover" class="warning-card expired">
          <el-statistic title="已过期" :value="summary.expiredCount">
            <template #suffix>
              <span style="font-size: 14px">批</span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="warning-card urgent">
          <el-statistic title="30天内到期" :value="summary.urgentCount">
            <template #suffix>
              <span style="font-size: 14px">批</span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="warning-card near">
          <el-statistic title="90天内到期" :value="summary.nearExpiryCount">
            <template #suffix>
              <span style="font-size: 14px">批</span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 预警列表 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>效期预警明细</span>
          <el-button type="primary" @click="loadData">刷新</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 已过期 -->
        <el-tab-pane label="已过期" name="expired">
          <el-table :data="summary.expiredBatches" size="small" border v-loading="loading">
            <el-table-column prop="drugName" label="药品名称" min-width="150" />
            <el-table-column prop="specification" label="规格" width="120" />
            <el-table-column prop="batchNo" label="批号" width="120" />
            <el-table-column prop="expireDate" label="失效日期" width="120" />
            <el-table-column prop="daysRemaining" label="过期天数" width="100">
              <template #default="{ row }">
                <el-tag type="danger">{{ Math.abs(row.daysRemaining) }}天</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="stockQuantity" label="库存数量" width="100" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="danger" size="small" @click="handleDispose(row)">报损处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 紧急（30天内） -->
        <el-tab-pane label="紧急(30天内)" name="urgent">
          <el-table :data="summary.urgentBatches" size="small" border v-loading="loading">
            <el-table-column prop="drugName" label="药品名称" min-width="150" />
            <el-table-column prop="specification" label="规格" width="120" />
            <el-table-column prop="batchNo" label="批号" width="120" />
            <el-table-column prop="expireDate" label="失效日期" width="120" />
            <el-table-column prop="daysRemaining" label="剩余天数" width="100">
              <template #default="{ row }">
                <el-tag type="warning">{{ row.daysRemaining }}天</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="stockQuantity" label="库存数量" width="100" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="warning" size="small" @click="handlePromote(row)">促销处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 近效期（90天内） -->
        <el-tab-pane label="近效期(90天内)" name="near">
          <el-table :data="summary.nearExpiryBatches" size="small" border v-loading="loading">
            <el-table-column prop="drugName" label="药品名称" min-width="150" />
            <el-table-column prop="specification" label="规格" width="120" />
            <el-table-column prop="batchNo" label="批号" width="120" />
            <el-table-column prop="expireDate" label="失效日期" width="120" />
            <el-table-column prop="daysRemaining" label="剩余天数" width="100">
              <template #default="{ row }">
                <el-tag type="info">{{ row.daysRemaining }}天</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="stockQuantity" label="库存数量" width="100" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 追溯码查询 -->
    <el-card shadow="never" style="margin-top: 20px">
      <template #header>追溯码查询</template>
      <el-form inline>
        <el-form-item label="追溯码">
          <el-input v-model="traceCodeQuery" placeholder="输入追溯码" style="width: 300px" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="queryTrace" :loading="traceLoading">查询</el-button>
        </el-form-item>
      </el-form>

      <el-descriptions v-if="traceResult" :column="2" border style="margin-top: 16px">
        <el-descriptions-item label="追溯码">{{ traceResult.traceCode }}</el-descriptions-item>
        <el-descriptions-item label="批次号">{{ traceResult.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="生产日期">{{ traceResult.produceDate }}</el-descriptions-item>
        <el-descriptions-item label="失效日期">{{ traceResult.expireDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="traceResult.status === 'active' ? 'success' : 'info'">
            {{ traceResult.status === 'active' ? '在库' : traceResult.status === 'sold' ? '已售' : traceResult.status }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getExpiryWarningSummary, queryTraceCode } from '@/api/batch'

const loading = ref(false)
const activeTab = ref('expired')

const summary = ref({
  expiredCount: 0,
  urgentCount: 0,
  nearExpiryCount: 0,
  expiredBatches: [],
  urgentBatches: [],
  nearExpiryBatches: []
})

const traceCodeQuery = ref('')
const traceLoading = ref(false)
const traceResult = ref(null)

async function loadData() {
  loading.value = true
  try {
    // 假设门店ID为1
    const res = await getExpiryWarningSummary(1)
    summary.value = res.data || summary.value
  } catch (e) {
    ElMessage.error('加载预警数据失败')
  } finally {
    loading.value = false
  }
}

async function queryTrace() {
  if (!traceCodeQuery.value.trim()) {
    ElMessage.warning('请输入追溯码')
    return
  }
  traceLoading.value = true
  try {
    const res = await queryTraceCode(traceCodeQuery.value)
    traceResult.value = res.data
  } catch (e) {
    traceResult.value = null
    ElMessage.error('追溯码不存在或查询失败')
  } finally {
    traceLoading.value = false
  }
}

function handleDispose(row) {
  ElMessageBox.confirm(`确定要对批次 ${row.batchNo} 进行报损处理吗？`, '报损确认', {
    type: 'warning'
  }).then(() => {
    ElMessage.success('报损处理成功（功能待实现）')
  }).catch(() => {})
}

function handlePromote(row) {
  ElMessageBox.confirm(`确定要对批次 ${row.batchNo} 设置促销吗？`, '促销确认', {
    type: 'info'
  }).then(() => {
    ElMessage.success('促销设置成功（功能待实现）')
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.expiry-warning-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.warning-card {
  text-align: center;
}

.warning-card.expired :deep(.el-statistic__number) {
  color: #f56c6c;
}

.warning-card.urgent :deep(.el-statistic__number) {
  color: #e6a23c;
}

.warning-card.near :deep(.el-statistic__number) {
  color: #909399;
}
</style>
