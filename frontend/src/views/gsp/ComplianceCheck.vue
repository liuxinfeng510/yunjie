<template>
  <div class="compliance-check-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="本月检查次数" :value="statistics.totalChecks" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card success">
          <el-statistic title="合格项目" :value="statistics.totalPassedItems" value-style="color: #67C23A" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="平均合格率" :value="statistics.averagePassRate" suffix="%" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card warning">
          <el-statistic title="待整改项" :value="statistics.pendingCorrections" value-style="color: #E6A23C" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 左侧：执行检查 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>执行GSP检查</span>
          </template>

          <el-form :model="checkForm" label-width="100px">
            <el-form-item label="门店">
              <el-select v-model="checkForm.storeId" placeholder="选择门店">
                <el-option v-for="store in storeList" :key="store.id" 
                           :label="store.storeName" :value="store.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="检查类型">
              <el-radio-group v-model="checkForm.checkType">
                <el-radio label="daily">日检</el-radio>
                <el-radio label="monthly">月检</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="executeCheck" :loading="executing">
                开始检查
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 最新检查结果 -->
          <div v-if="lastCheckResult" class="last-result">
            <el-divider>最新检查结果</el-divider>
            <el-descriptions :column="1" size="small" border>
              <el-descriptions-item label="检查批次">{{ lastCheckResult.checkBatchNo }}</el-descriptions-item>
              <el-descriptions-item label="检查日期">{{ lastCheckResult.checkDate }}</el-descriptions-item>
              <el-descriptions-item label="检查项">{{ lastCheckResult.totalItems }}项</el-descriptions-item>
              <el-descriptions-item label="合格率">
                <el-progress :percentage="lastCheckResult.passRate" 
                             :status="getProgressStatus(lastCheckResult.passRate)" />
              </el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getCorrectionStatusType(lastCheckResult.correctionStatus)">
                  {{ getCorrectionStatusText(lastCheckResult.correctionStatus) }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
            <div v-if="lastCheckResult.issues" class="issues">
              <div class="issue-title">存在问题：</div>
              <div class="issue-content">{{ lastCheckResult.issues }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：检查记录 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>检查记录</span>
              <div class="header-actions">
                <el-select v-model="queryParams.checkType" placeholder="检查类型" clearable
                           style="width: 100px; margin-right: 10px" @change="loadChecks">
                  <el-option label="日检" value="daily" />
                  <el-option label="月检" value="monthly" />
                </el-select>
                <el-date-picker v-model="dateRange" type="daterange" start-placeholder="开始日期"
                                end-placeholder="结束日期" value-format="YYYY-MM-DD" 
                                @change="loadChecks" style="width: 240px" />
              </div>
            </div>
          </template>

          <el-table :data="checkList" stripe v-loading="loading">
            <el-table-column prop="checkBatchNo" label="批次号" width="140" />
            <el-table-column prop="checkDate" label="检查日期" width="100" />
            <el-table-column prop="checkType" label="类型" width="70">
              <template #default="{ row }">
                {{ row.checkType === 'daily' ? '日检' : '月检' }}
              </template>
            </el-table-column>
            <el-table-column label="合格率" width="150">
              <template #default="{ row }">
                <el-progress :percentage="row.passRate" :stroke-width="10"
                             :status="getProgressStatus(row.passRate)" />
              </template>
            </el-table-column>
            <el-table-column prop="passedItems" label="合格/总数" width="90">
              <template #default="{ row }">
                {{ row.passedItems }}/{{ row.totalItems }}
              </template>
            </el-table-column>
            <el-table-column prop="correctionStatus" label="整改状态" width="90">
              <template #default="{ row }">
                <el-tag :type="getCorrectionStatusType(row.correctionStatus)" size="small">
                  {{ getCorrectionStatusText(row.correctionStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="showDetail(row)">
                  详情
                </el-button>
                <el-button v-if="row.correctionStatus === 'pending'" 
                           type="warning" link size="small" @click="showCorrectionDialog(row)">
                  整改
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="queryParams.current"
            v-model:page-size="queryParams.size"
            :total="total"
            layout="total, prev, pager, next"
            @change="loadChecks"
            style="margin-top: 15px"
          />
        </el-card>
      </el-col>
    </el-row>

    <!-- 整改对话框 -->
    <el-dialog v-model="correctionDialogVisible" title="整改处理" width="500px">
      <el-form :model="correctionForm" label-width="100px">
        <el-form-item label="问题描述">
          <div class="issue-text">{{ currentCheck?.issues }}</div>
        </el-form-item>
        <el-form-item label="整改措施" required>
          <el-input v-model="correctionForm.measures" type="textarea" :rows="3" 
                    placeholder="请输入整改措施" />
        </el-form-item>
        <el-form-item label="整改状态">
          <el-radio-group v-model="correctionForm.status">
            <el-radio label="in_progress">整改中</el-radio>
            <el-radio label="completed">已完成</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="correctionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCorrection">提交</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="检查详情" width="600px">
      <el-descriptions :column="2" border v-if="currentCheck">
        <el-descriptions-item label="检查批次">{{ currentCheck.checkBatchNo }}</el-descriptions-item>
        <el-descriptions-item label="检查日期">{{ currentCheck.checkDate }}</el-descriptions-item>
        <el-descriptions-item label="检查类型">{{ currentCheck.checkType === 'daily' ? '日检' : '月检' }}</el-descriptions-item>
        <el-descriptions-item label="合格率">{{ currentCheck.passRate }}%</el-descriptions-item>
        <el-descriptions-item label="合格项">{{ currentCheck.passedItems }}</el-descriptions-item>
        <el-descriptions-item label="不合格项">{{ currentCheck.failedItems }}</el-descriptions-item>
        <el-descriptions-item label="检查人" :span="2">{{ currentCheck.checkerName || '系统自动' }}</el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">{{ currentCheck.issues || '无' }}</el-descriptions-item>
        <el-descriptions-item label="整改措施" :span="2">{{ currentCheck.correctionMeasures || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { executeGspCheck, pageGspChecks, getGspStatistics, updateCorrectionStatus } from '@/api/gspAutomation'
import { getStoreList } from '@/api/system'

const loading = ref(false)
const executing = ref(false)
const storeList = ref([])
const checkList = ref([])
const total = ref(0)
const statistics = ref({})
const lastCheckResult = ref(null)

const checkForm = ref({
  storeId: null,
  checkType: 'daily'
})

const queryParams = ref({
  current: 1,
  size: 10,
  checkType: ''
})

const dateRange = ref([])
const correctionDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentCheck = ref(null)
const correctionForm = ref({
  measures: '',
  status: 'in_progress'
})

onMounted(() => {
  loadStores()
  loadChecks()
  loadStatistics()
})

const loadStores = async () => {
  try {
    const res = await getStoreList()
    storeList.value = res.data || []
    if (storeList.value.length > 0) {
      checkForm.value.storeId = storeList.value[0].id
    }
  } catch (error) {
    console.error('加载门店失败', error)
  }
}

const loadChecks = async () => {
  loading.value = true
  try {
    const params = { ...queryParams.value }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await pageGspChecks(params)
    checkList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('加载检查记录失败', error)
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const today = new Date()
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1)
    const res = await getGspStatistics({
      startDate: firstDay.toISOString().split('T')[0],
      endDate: today.toISOString().split('T')[0]
    })
    statistics.value = res.data || {}
  } catch (error) {
    console.error('加载统计失败', error)
  }
}

const executeCheck = async () => {
  if (!checkForm.value.storeId) {
    ElMessage.warning('请选择门店')
    return
  }
  executing.value = true
  try {
    const res = await executeGspCheck(checkForm.value)
    lastCheckResult.value = res.data
    ElMessage.success('检查完成')
    loadChecks()
    loadStatistics()
  } catch (error) {
    ElMessage.error('检查执行失败')
  } finally {
    executing.value = false
  }
}

const getProgressStatus = (rate) => {
  if (rate >= 90) return 'success'
  if (rate >= 70) return 'warning'
  return 'exception'
}

const getCorrectionStatusType = (status) => {
  const types = { pending: 'danger', in_progress: 'warning', completed: 'success' }
  return types[status] || 'info'
}

const getCorrectionStatusText = (status) => {
  const texts = { pending: '待整改', in_progress: '整改中', completed: '已完成' }
  return texts[status] || '--'
}

const showDetail = (row) => {
  currentCheck.value = row
  detailDialogVisible.value = true
}

const showCorrectionDialog = (row) => {
  currentCheck.value = row
  correctionForm.value = { measures: '', status: 'in_progress' }
  correctionDialogVisible.value = true
}

const submitCorrection = async () => {
  if (!correctionForm.value.measures) {
    ElMessage.warning('请输入整改措施')
    return
  }
  try {
    await updateCorrectionStatus(currentCheck.value.id, correctionForm.value)
    ElMessage.success('整改提交成功')
    correctionDialogVisible.value = false
    loadChecks()
    loadStatistics()
  } catch (error) {
    ElMessage.error('提交失败')
  }
}
</script>

<style scoped lang="scss">
.compliance-check-container {
  padding: 20px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.last-result {
  margin-top: 20px;
}

.issues {
  margin-top: 15px;
  padding: 10px;
  background: #fef0f0;
  border-radius: 4px;
  
  .issue-title {
    font-weight: bold;
    color: #F56C6C;
    margin-bottom: 5px;
  }
  .issue-content {
    color: #666;
    font-size: 13px;
  }
}

.issue-text {
  color: #F56C6C;
  background: #fef0f0;
  padding: 10px;
  border-radius: 4px;
}
</style>
