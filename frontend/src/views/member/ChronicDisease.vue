<template>
  <div class="chronic-disease-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <el-statistic title="在管人数" :value="statistics.activeRecords" />
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card diabetes">
          <el-statistic title="糖尿病" :value="statistics.diabetesCount" />
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card hypertension">
          <el-statistic title="高血压" :value="statistics.hypertensionCount" />
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card hyperlipidemia">
          <el-statistic title="高血脂" :value="statistics.hyperlipidemiaCount" />
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card heart">
          <el-statistic title="冠心病" :value="statistics.coronaryHeartCount" />
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card warning">
          <el-statistic title="待复查" :value="statistics.pendingFollowUps" value-style="color: #E6A23C" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 左侧：慢病记录列表 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>慢病管理记录</span>
              <div class="header-actions">
                <el-select v-model="queryParams.diseaseType" placeholder="疾病类型" clearable 
                           style="width: 120px; margin-right: 10px" @change="loadRecords">
                  <el-option label="糖尿病" value="diabetes" />
                  <el-option label="高血压" value="hypertension" />
                  <el-option label="高血脂" value="hyperlipidemia" />
                  <el-option label="冠心病" value="coronary_heart" />
                  <el-option label="哮喘" value="asthma" />
                </el-select>
                <el-select v-model="queryParams.managementStatus" placeholder="状态" clearable 
                           style="width: 100px" @change="loadRecords">
                  <el-option label="在管" value="active" />
                  <el-option label="稳定" value="stable" />
                  <el-option label="好转" value="improved" />
                </el-select>
              </div>
            </div>
          </template>

          <el-table :data="recordList" stripe v-loading="loading">
            <el-table-column prop="memberName" label="会员姓名" width="100" />
            <el-table-column prop="diseaseName" label="疾病名称" />
            <el-table-column prop="severityLevel" label="程度" width="70">
              <template #default="{ row }">
                <el-tag :type="getSeverityType(row.severityLevel)" size="small">
                  {{ getSeverityText(row.severityLevel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="diagnosisDate" label="诊断日期" width="100" />
            <el-table-column prop="lastCheckDate" label="最近检查" width="100" />
            <el-table-column prop="nextCheckDate" label="下次复查" width="100">
              <template #default="{ row }">
                <span :class="{ 'warning-date': isNearDate(row.nextCheckDate) }">
                  {{ row.nextCheckDate }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="managementStatus" label="状态" width="70">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.managementStatus)" size="small">
                  {{ getStatusText(row.managementStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="showCheckDialog(row)">
                  记录检查
                </el-button>
                <el-button type="success" link size="small" @click="showReminderDialog(row)">
                  设置提醒
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="queryParams.current"
            v-model:page-size="queryParams.size"
            :total="total"
            layout="total, sizes, prev, pager, next"
            @change="loadRecords"
            style="margin-top: 15px"
          />
        </el-card>
      </el-col>

      <!-- 右侧：待复查提醒 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>待复查提醒 (7天内)</span>
              <el-button type="primary" size="small" @click="loadFollowUps">刷新</el-button>
            </div>
          </template>

          <div class="followup-list">
            <div v-for="item in pendingFollowUps" :key="item.recordId" class="followup-item">
              <div class="followup-info">
                <div class="member-name">{{ item.memberName }}</div>
                <div class="disease-name">{{ item.diseaseName }}</div>
                <div class="phone">{{ item.phone }}</div>
              </div>
              <div class="followup-date">
                <div class="days" :class="{ urgent: item.daysRemaining <= 2 }">
                  {{ item.daysRemaining }}天后
                </div>
                <div class="date">{{ item.nextCheckDate }}</div>
              </div>
            </div>
            <el-empty v-if="!pendingFollowUps.length" description="暂无待复查记录" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 记录检查结果对话框 -->
    <el-dialog v-model="checkDialogVisible" title="记录检查结果" width="500px">
      <el-form :model="checkForm" label-width="100px">
        <el-form-item label="会员">
          <span>{{ currentRecord?.memberName }}</span>
        </el-form-item>
        <el-form-item label="疾病">
          <span>{{ currentRecord?.diseaseName }}</span>
        </el-form-item>
        <el-form-item label="检查结果" required>
          <el-input v-model="checkForm.checkResult" type="textarea" :rows="3" 
                    placeholder="血压/血糖/血脂等指标结果" />
        </el-form-item>
        <el-form-item label="下次复查" required>
          <el-date-picker v-model="checkForm.nextCheckDate" type="date" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="医嘱建议">
          <el-input v-model="checkForm.doctorAdvice" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCheck">保存</el-button>
      </template>
    </el-dialog>

    <!-- 设置用药提醒对话框 -->
    <el-dialog v-model="reminderDialogVisible" title="设置用药提醒" width="500px">
      <el-form :model="reminderForm" label-width="100px">
        <el-form-item label="药品名称" required>
          <el-input v-model="reminderForm.drugName" placeholder="请输入药品名称" />
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="reminderForm.specification" />
        </el-form-item>
        <el-form-item label="用量" required>
          <el-input v-model="reminderForm.dosage" placeholder="如：1片/次" />
        </el-form-item>
        <el-form-item label="用法">
          <el-select v-model="reminderForm.usageMethod">
            <el-option label="口服" value="oral" />
            <el-option label="注射" value="injection" />
            <el-option label="外用" value="external" />
          </el-select>
        </el-form-item>
        <el-form-item label="提醒频率">
          <el-select v-model="reminderForm.frequency">
            <el-option label="每天一次" value="daily" />
            <el-option label="每天两次" value="twice_daily" />
            <el-option label="每天三次" value="three_times" />
            <el-option label="每周一次" value="weekly" />
          </el-select>
        </el-form-item>
        <el-form-item label="提醒时间">
          <el-checkbox-group v-model="selectedTimes">
            <el-checkbox label="08:00">早8点</el-checkbox>
            <el-checkbox label="12:00">中午12点</el-checkbox>
            <el-checkbox label="18:00">晚6点</el-checkbox>
            <el-checkbox label="21:00">晚9点</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="reminderForm.startDate" type="date" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="reminderForm.endDate" type="date" placeholder="长期用药可不填" />
        </el-form-item>
        <el-form-item label="通知方式">
          <el-radio-group v-model="reminderForm.notifyMethod">
            <el-radio label="sms">短信</el-radio>
            <el-radio label="wechat">微信</el-radio>
            <el-radio label="all">全部</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="注意事项">
          <el-input v-model="reminderForm.precautions" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reminderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReminder">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  pageDiseaseRecords, getDiseaseStatistics, getPendingFollowUps,
  recordCheckResult, createDiseaseReminder 
} from '@/api/health'

const loading = ref(false)
const statistics = ref({})
const recordList = ref([])
const total = ref(0)
const pendingFollowUps = ref([])
const queryParams = ref({
  current: 1,
  size: 10,
  diseaseType: '',
  managementStatus: ''
})

const checkDialogVisible = ref(false)
const currentRecord = ref(null)
const checkForm = ref({})

const reminderDialogVisible = ref(false)
const reminderForm = ref({})
const selectedTimes = ref(['08:00'])

onMounted(() => {
  loadStatistics()
  loadRecords()
  loadFollowUps()
})

const loadStatistics = async () => {
  try {
    const res = await getDiseaseStatistics()
    statistics.value = res.data || {}
  } catch (error) {
    console.error('加载统计失败', error)
  }
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await pageDiseaseRecords(queryParams.value)
    recordList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('加载记录失败', error)
  } finally {
    loading.value = false
  }
}

const loadFollowUps = async () => {
  try {
    const res = await getPendingFollowUps(7)
    pendingFollowUps.value = res.data || []
  } catch (error) {
    console.error('加载待复查失败', error)
  }
}

const getSeverityType = (level) => {
  const types = { mild: 'success', moderate: 'warning', severe: 'danger' }
  return types[level] || 'info'
}

const getSeverityText = (level) => {
  const texts = { mild: '轻度', moderate: '中度', severe: '重度' }
  return texts[level] || '--'
}

const getStatusType = (status) => {
  const types = { active: '', stable: 'success', improved: 'success', worsened: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { active: '在管', stable: '稳定', improved: '好转', worsened: '恶化' }
  return texts[status] || '--'
}

const isNearDate = (dateStr) => {
  if (!dateStr) return false
  const date = new Date(dateStr)
  const today = new Date()
  const diff = (date - today) / (1000 * 60 * 60 * 24)
  return diff <= 7 && diff >= 0
}

const showCheckDialog = (row) => {
  currentRecord.value = row
  checkForm.value = {}
  checkDialogVisible.value = true
}

const submitCheck = async () => {
  if (!checkForm.value.checkResult || !checkForm.value.nextCheckDate) {
    ElMessage.warning('请填写必填项')
    return
  }
  try {
    await recordCheckResult(currentRecord.value.id, checkForm.value)
    ElMessage.success('保存成功')
    checkDialogVisible.value = false
    loadRecords()
    loadFollowUps()
    loadStatistics()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const showReminderDialog = (row) => {
  currentRecord.value = row
  reminderForm.value = {
    usageMethod: 'oral',
    frequency: 'daily',
    notifyMethod: 'sms',
    startDate: new Date()
  }
  selectedTimes.value = ['08:00']
  reminderDialogVisible.value = true
}

const submitReminder = async () => {
  if (!reminderForm.value.drugName || !reminderForm.value.dosage) {
    ElMessage.warning('请填写必填项')
    return
  }
  try {
    reminderForm.value.reminderTimes = JSON.stringify(selectedTimes.value)
    await createDiseaseReminder(currentRecord.value.id, reminderForm.value)
    ElMessage.success('提醒设置成功')
    reminderDialogVisible.value = false
  } catch (error) {
    ElMessage.error('设置失败')
  }
}
</script>

<style scoped lang="scss">
.chronic-disease-container {
  padding: 20px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  &.diabetes :deep(.el-statistic__number) { color: #E6A23C; }
  &.hypertension :deep(.el-statistic__number) { color: #F56C6C; }
  &.hyperlipidemia :deep(.el-statistic__number) { color: #909399; }
  &.heart :deep(.el-statistic__number) { color: #F56C6C; }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
}

.warning-date {
  color: #E6A23C;
  font-weight: bold;
}

.followup-list {
  max-height: 500px;
  overflow-y: auto;
}

.followup-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #eee;
  
  &:hover {
    background: #f5f7fa;
  }

  .followup-info {
    .member-name {
      font-weight: bold;
      margin-bottom: 4px;
    }
    .disease-name {
      font-size: 13px;
      color: #666;
    }
    .phone {
      font-size: 12px;
      color: #999;
    }
  }

  .followup-date {
    text-align: right;
    .days {
      font-size: 16px;
      font-weight: bold;
      color: #409EFF;
      &.urgent {
        color: #F56C6C;
      }
    }
    .date {
      font-size: 12px;
      color: #999;
    }
  }
}
</style>
