<template>
  <div class="reconciliation-container">
    <div v-if="fromPos" class="back-bar" style="margin-bottom: 12px;">
      <el-button type="primary" plain size="small" @click="router.push('/sale/pos')">&larr; 返回收银台</el-button>
    </div>
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 发起对账 Tab -->
      <el-tab-pane label="发起对账" name="create">
        <el-card shadow="never">
          <el-form :inline="true" class="search-form">
            <el-form-item label="对账日期">
              <el-date-picker
                v-model="reconcileDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                :disabled-date="disableFutureDate"
              />
            </el-form-item>
            <el-form-item label="收银员">
              <el-select v-model="selectedCashierId" placeholder="全部收银员" clearable style="width: 160px;">
                <el-option label="全部收银员" :value="null" />
                <el-option v-for="u in cashierList" :key="u.id" :label="u.realName || u.username" :value="u.id" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadPreview" :loading="previewLoading">查询系统数据</el-button>
            </el-form-item>
          </el-form>

          <template v-if="previewData">
            <el-descriptions :column="4" border style="margin-bottom: 16px;">
              <el-descriptions-item label="对账日期">{{ previewData.reconcileDate }}</el-descriptions-item>
              <el-descriptions-item label="收银员">{{ selectedCashierName }}</el-descriptions-item>
              <el-descriptions-item label="订单总数">{{ previewData.orderCount }} 笔</el-descriptions-item>
              <el-descriptions-item label="系统总额">
                <span style="color: #409eff; font-weight: bold;">{{ formatMoney(previewData.systemTotal) }}</span>
              </el-descriptions-item>
            </el-descriptions>

            <!-- 差额提示 -->
            <el-alert
              v-if="submitted === false && previewData.orderCount > 0"
              :type="differenceAlertType"
              :title="differenceText"
              :closable="false"
              show-icon
              style="margin-bottom: 16px;"
            />

            <el-table :data="detailRows" border style="width: 100%; margin-bottom: 16px;">
              <el-table-column prop="payMethod" label="支付方式" width="150">
                <template #default="{ row }">{{ payMethodLabel(row.payMethod) }}</template>
              </el-table-column>
              <el-table-column prop="orderCount" label="订单数" width="100" align="center" />
              <el-table-column prop="systemAmount" label="系统金额" width="150" align="right">
                <template #default="{ row }">{{ formatMoney(row.systemAmount) }}</template>
              </el-table-column>
              <el-table-column label="实际金额" width="180" align="right">
                <template #default="{ row }">
                  <el-input-number
                    v-model="row.actualAmount"
                    :precision="2"
                    :step="0.01"
                    :min="0"
                    size="small"
                    controls-position="right"
                  />
                </template>
              </el-table-column>
              <el-table-column label="差额" width="120" align="right">
                <template #default="{ row }">
                  <span :style="{ color: getDiffColor(row.actualAmount - row.systemAmount) }">
                    {{ formatMoney(row.actualAmount - row.systemAmount) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="备注" min-width="150">
                <template #default="{ row }">
                  <el-input v-model="row.remark" placeholder="差异说明" size="small" />
                </template>
              </el-table-column>
            </el-table>

            <el-form label-width="80px" style="max-width: 600px;">
              <el-form-item label="整体备注">
                <el-input v-model="submitRemark" type="textarea" :rows="2" placeholder="可填写整体对账说明" />
              </el-form-item>
            </el-form>

            <div style="text-align: right; margin-top: 16px;">
              <el-button type="primary" @click="handleSubmit" :loading="submitLoading"
                         :disabled="previewData.orderCount === 0">
                提交对账
              </el-button>
            </div>
          </template>

          <el-empty v-else-if="!previewLoading" description="请选择日期后查询系统数据" />
        </el-card>
      </el-tab-pane>

      <!-- 对账记录 Tab -->
      <el-tab-pane label="对账记录" name="history">
        <el-card shadow="never">
          <el-form :inline="true" class="search-form">
            <el-form-item label="日期范围">
              <el-date-picker
                v-model="historyDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="historyStatus" placeholder="全部" clearable style="width: 120px;">
                <el-option label="账平" value="balanced" />
                <el-option label="长款" value="surplus" />
                <el-option label="短款" value="shortage" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadHistory">查询</el-button>
              <el-button @click="resetHistory">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="historyLoading" :data="historyData" border style="width: 100%;">
            <el-table-column prop="reconciliationNo" label="对账单号" width="200" />
            <el-table-column prop="reconcileDate" label="对账日期" width="120" />
            <el-table-column prop="cashierName" label="收银员" width="100">
              <template #default="{ row }">{{ row.cashierName || '全部' }}</template>
            </el-table-column>
            <el-table-column prop="orderCount" label="订单数" width="80" align="center" />
            <el-table-column prop="systemTotal" label="系统总额" width="120" align="right">
              <template #default="{ row }">{{ formatMoney(row.systemTotal) }}</template>
            </el-table-column>
            <el-table-column prop="actualTotal" label="实际总额" width="120" align="right">
              <template #default="{ row }">{{ formatMoney(row.actualTotal) }}</template>
            </el-table-column>
            <el-table-column prop="difference" label="差额" width="120" align="right">
              <template #default="{ row }">
                <span :style="{ color: getDiffColor(row.difference) }">{{ formatMoney(row.difference) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
            <el-table-column label="操作" width="80" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="showDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-if="historyTotal > 0"
            :current-page="historyPage"
            :page-size="historySize"
            :total="historyTotal"
            layout="total, prev, pager, next"
            style="margin-top: 16px; justify-content: flex-end;"
            @current-change="handleHistoryPageChange"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 对账详情弹窗 -->
    <el-dialog v-model="detailVisible" title="对账详情" width="700px">
      <template v-if="detailData">
        <el-descriptions :column="3" border style="margin-bottom: 16px;">
          <el-descriptions-item label="对账单号">{{ detailData.reconciliation.reconciliationNo }}</el-descriptions-item>
          <el-descriptions-item label="对账日期">{{ detailData.reconciliation.reconcileDate }}</el-descriptions-item>
          <el-descriptions-item label="收银员">{{ detailData.reconciliation.cashierName || '全部收银员' }}</el-descriptions-item>
          <el-descriptions-item label="订单总数">{{ detailData.reconciliation.orderCount }} 笔</el-descriptions-item>
          <el-descriptions-item label="系统总额">{{ formatMoney(detailData.reconciliation.systemTotal) }}</el-descriptions-item>
          <el-descriptions-item label="实际总额">{{ formatMoney(detailData.reconciliation.actualTotal) }}</el-descriptions-item>
          <el-descriptions-item label="差额">
            <span :style="{ color: getDiffColor(detailData.reconciliation.difference) }">
              {{ formatMoney(detailData.reconciliation.difference) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detailData.reconciliation.status)">
              {{ statusLabel(detailData.reconciliation.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="备注">{{ detailData.reconciliation.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-table :data="detailData.details" border style="width: 100%;">
          <el-table-column prop="payMethod" label="支付方式" width="120">
            <template #default="{ row }">{{ payMethodLabel(row.payMethod) }}</template>
          </el-table-column>
          <el-table-column prop="orderCount" label="订单数" width="80" align="center" />
          <el-table-column prop="systemAmount" label="系统金额" width="120" align="right">
            <template #default="{ row }">{{ formatMoney(row.systemAmount) }}</template>
          </el-table-column>
          <el-table-column prop="actualAmount" label="实际金额" width="120" align="right">
            <template #default="{ row }">{{ formatMoney(row.actualAmount) }}</template>
          </el-table-column>
          <el-table-column prop="difference" label="差额" width="120" align="right">
            <template #default="{ row }">
              <span :style="{ color: getDiffColor(row.difference) }">{{ formatMoney(row.difference) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import { getUserList } from '@/api/user'
import {
  getReconciliationPreview,
  submitReconciliation,
  getReconciliationPage,
  getReconciliationDetail
} from '@/api/sale'

const route = useRoute()
const router = useRouter()
const fromPos = computed(() => route.query.from === 'pos')

const authStore = useAuthStore()

// ========== 收银员列表 ==========
const cashierList = ref([])
const selectedCashierId = ref(null)

const selectedCashierName = computed(() => {
  if (selectedCashierId.value == null) return '全部收银员'
  const u = cashierList.value.find(c => c.id === selectedCashierId.value)
  return u ? (u.realName || u.username) : '未知'
})

async function loadCashierList() {
  try {
    const res = await getUserList()
    cashierList.value = res.data || []
  } catch (e) {
    console.error('加载收银员列表失败', e)
  }
}

// ========== 发起对账 ==========
const activeTab = ref('create')
const reconcileDate = ref(new Date().toISOString().slice(0, 10))
const previewLoading = ref(false)
const previewData = ref(null)
const detailRows = ref([])
const submitRemark = ref('')
const submitLoading = ref(false)
const submitted = ref(null)

const PAY_METHOD_LABELS = {
  '现金': '现金', 'CASH': '现金',
  '微信': '微信', 'WECHAT': '微信',
  '支付宝': '支付宝', 'ALIPAY': '支付宝',
  '医保': '医保', 'MEDICAL_INSURANCE': '医保'
}

function payMethodLabel(val) {
  return PAY_METHOD_LABELS[val] || val || '未知'
}

function disableFutureDate(date) {
  return date > new Date()
}

function formatMoney(val) {
  const n = Number(val) || 0
  return '\u00a5' + n.toFixed(2)
}

function getDiffColor(diff) {
  const n = Number(diff) || 0
  if (n > 0) return '#e6a23c'
  if (n < 0) return '#f56c6c'
  return '#67c23a'
}

const actualTotal = computed(() => {
  return detailRows.value.reduce((sum, r) => sum + (Number(r.actualAmount) || 0), 0)
})

const totalDifference = computed(() => {
  return actualTotal.value - (Number(previewData.value?.systemTotal) || 0)
})

const differenceColor = computed(() => getDiffColor(totalDifference.value))

const differenceAlertType = computed(() => {
  const d = totalDifference.value
  if (d === 0) return 'success'
  if (d > 0) return 'warning'
  return 'error'
})

const differenceText = computed(() => {
  const d = totalDifference.value
  if (d === 0) return '账目平衡，无差异'
  if (d > 0) return `长款 ${formatMoney(d)}（实际多于系统），请核实来源`
  return `短款 ${formatMoney(Math.abs(d))}（实际少于系统），请核实原因`
})

async function loadPreview() {
  if (!reconcileDate.value) {
    ElMessage.warning('请选择对账日期')
    return
  }
  previewLoading.value = true
  submitted.value = false
  try {
    const storeId = authStore.user?.storeId
    const params = { reconcileDate: reconcileDate.value }
    if (storeId) params.storeId = storeId
    if (selectedCashierId.value != null) params.cashierId = selectedCashierId.value
    const res = await getReconciliationPreview(params)
    const data = res.data
    previewData.value = data
    // 初始化明细行，实际金额默认等于系统金额
    detailRows.value = (data.details || []).map(d => ({
      payMethod: d.payMethod,
      orderCount: d.orderCount,
      systemAmount: Number(d.systemAmount) || 0,
      actualAmount: Number(d.systemAmount) || 0,
      remark: ''
    }))
    submitRemark.value = ''
  } catch (e) {
    ElMessage.error('加载对账数据失败')
  } finally {
    previewLoading.value = false
  }
}

async function handleSubmit() {
  if (detailRows.value.length === 0) {
    ElMessage.warning('当日无销售数据')
    return
  }

  const diff = totalDifference.value
  let confirmMsg = '确认提交对账单？'
  if (diff !== 0) {
    const label = diff > 0 ? '长款' : '短款'
    confirmMsg = `当前存在${label} ${formatMoney(Math.abs(diff))}，确认提交？`
  }

  try {
    await ElMessageBox.confirm(confirmMsg, '确认提交', { type: 'warning' })
  } catch {
    return
  }

  submitLoading.value = true
  try {
    const storeId = authStore.user?.storeId
    await submitReconciliation({
      storeId: storeId || undefined,
      cashierId: selectedCashierId.value,
      reconcileDate: reconcileDate.value,
      remark: submitRemark.value,
      details: detailRows.value.map(r => ({
        payMethod: r.payMethod,
        actualAmount: r.actualAmount,
        remark: r.remark
      }))
    })
    ElMessage.success('对账单提交成功')
    submitted.value = true
    previewData.value = null
    detailRows.value = []
    // 切换到历史记录并刷新
    activeTab.value = 'history'
    loadHistory()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '提交失败')
  } finally {
    submitLoading.value = false
  }
}

// ========== 对账记录 ==========
// 默认当月范围
const now = new Date()
const monthStart = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`
const monthEnd = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate()).padStart(2, '0')}`
const historyDateRange = ref([monthStart, monthEnd])
const historyStatus = ref('')
const historyLoading = ref(false)
const historyData = ref([])
const historyPage = ref(1)
const historySize = ref(20)
const historyTotal = ref(0)

function statusLabel(s) {
  const map = { balanced: '账平', surplus: '长款', shortage: '短款' }
  return map[s] || s
}

function statusTagType(s) {
  const map = { balanced: 'success', surplus: 'warning', shortage: 'danger' }
  return map[s] || 'info'
}

async function loadHistory() {
  historyLoading.value = true
  try {
    const params = {
      current: historyPage.value,
      size: historySize.value
    }
    if (historyStatus.value) params.status = historyStatus.value
    if (historyDateRange.value && historyDateRange.value.length === 2) {
      params.startDate = historyDateRange.value[0]
      params.endDate = historyDateRange.value[1]
    }
    const res = await getReconciliationPage(params)
    const page = res.data
    historyData.value = page.records || []
    historyTotal.value = Number(page.total) || 0
  } catch (e) {
    ElMessage.error('加载对账记录失败')
  } finally {
    historyLoading.value = false
  }
}

function resetHistory() {
  historyDateRange.value = [monthStart, monthEnd]
  historyStatus.value = ''
  historyPage.value = 1
  loadHistory()
}

function handleHistoryPageChange(page) {
  historyPage.value = page
  loadHistory()
}

// ========== 对账详情弹窗 ==========
const detailVisible = ref(false)
const detailData = ref(null)

async function showDetail(row) {
  try {
    const res = await getReconciliationDetail(row.id)
    detailData.value = res.data
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}

// 初始化
loadCashierList()
loadHistory()
</script>

<style scoped>
.reconciliation-container {
  padding: 0;
}
.search-form {
  margin-bottom: 16px;
}
</style>
