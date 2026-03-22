<template>
  <div class="near-expiry-sale">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>近效期药品催销表</span>
          <div>
            <el-button type="primary" @click="handleGenerate">
              <el-icon><Refresh /></el-icon>生成本月催销表
            </el-button>
            <el-button @click="handlePrint">
              <el-icon><Printer /></el-icon>打印
            </el-button>
          </div>
        </div>
      </template>

      <!-- 筛选 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="月份">
          <el-date-picker v-model="queryForm.month" type="month" placeholder="选择月份"
            value-format="YYYY-MM" style="width: 150px;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 130px;">
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData" @contextmenu.prevent="showAutoGenSetting">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 自动生成提示 -->
      <el-alert v-if="autoGenDay" :title="`自动生成：每月${autoGenDay}号自动生成催销表`"
        type="info" :closable="false" show-icon style="margin-bottom: 12px;" />

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe id="print-table">
        <el-table-column prop="drugName" label="药品名称" min-width="150" />
        <el-table-column prop="specification" label="规格" width="100" />
        <el-table-column prop="manufacturer" label="生产企业" width="150" show-overflow-tooltip />
        <el-table-column prop="batchNo" label="批号" width="120" />
        <el-table-column prop="expireDate" label="有效期至" width="110">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.remainingDays <= 90 }">{{ row.expireDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remainingDays" label="剩余天数" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.remainingDays <= 90 ? 'danger' : row.remainingDays <= 180 ? 'warning' : 'success'" size="small">
              {{ row.remainingDays }}天
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="stockQuantity" label="库存" width="80" align="center" />
        <el-table-column prop="unit" label="单位" width="60" />
        <el-table-column prop="saleMeasure" label="催销措施" min-width="120">
          <template #default="{ row }">
            <span v-if="row.saleMeasure">{{ measureMap[row.saleMeasure] || row.saleMeasure }}</span>
            <el-tag v-else type="info" size="small">未设置</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusColor[row.status]">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'pending'" type="primary" link @click="handleSetMeasure(row)">设置措施</el-button>
            <el-button v-if="row.status === 'processing'" type="success" link @click="handleComplete(row)">完成</el-button>
            <el-button type="info" link @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 自动生成设置对话框 -->
    <el-dialog v-model="autoGenDialogVisible" title="自动生成设置" width="400px">
      <el-form label-width="140px">
        <el-form-item label="每月自动生成日期">
          <el-select v-model="autoGenDayEdit" placeholder="选择日期" style="width: 100%;">
            <el-option label="关闭自动生成" :value="0" />
            <el-option v-for="d in 28" :key="d" :label="`每月${d}号`" :value="d" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-text type="info" size="small">系统将在设定日期自动生成当月近效期催销表</el-text>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="autoGenDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAutoGenSetting" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 设置催销措施对话框 -->
    <el-dialog v-model="measureDialogVisible" title="设置催销措施" width="500px">
      <el-form ref="measureFormRef" :model="measureForm" :rules="measureRules" label-width="100px">
        <el-form-item label="药品">
          <el-input :model-value="`${currentRow.drugName} (${currentRow.batchNo})`" disabled />
        </el-form-item>
        <el-form-item label="催销措施" prop="saleMeasure">
          <el-select v-model="measureForm.saleMeasure" placeholder="请选择" style="width: 100%;">
            <el-option label="降价促销" value="discount" />
            <el-option label="捆绑销售" value="bundle" />
            <el-option label="退货给供应商" value="return" />
            <el-option label="加快销售" value="accelerate" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="措施说明" prop="measureDetail">
          <el-input v-model="measureForm.measureDetail" type="textarea" :rows="3" placeholder="详细说明催销措施" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="measureDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitMeasure" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 完成对话框 -->
    <el-dialog v-model="completeDialogVisible" title="完成处理" width="500px">
      <el-form ref="completeFormRef" :model="completeForm" label-width="100px">
        <el-form-item label="药品">
          <el-input :model-value="`${currentRow.drugName} (${currentRow.batchNo})`" disabled />
        </el-form-item>
        <el-form-item label="催销措施">
          <el-input :model-value="measureMap[currentRow.saleMeasure] || currentRow.saleMeasure" disabled />
        </el-form-item>
        <el-form-item label="结果说明">
          <el-input v-model="completeForm.resultRemark" type="textarea" :rows="3" placeholder="处理结果说明（如：已退货、已售完等）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitComplete" :loading="submitting">确认完成</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="催销详情" width="500px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="药品名称">{{ currentRow.drugName }}</el-descriptions-item>
        <el-descriptions-item label="规格">{{ currentRow.specification }}</el-descriptions-item>
        <el-descriptions-item label="生产企业" :span="2">{{ currentRow.manufacturer }}</el-descriptions-item>
        <el-descriptions-item label="批号">{{ currentRow.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="有效期至">{{ currentRow.expireDate }}</el-descriptions-item>
        <el-descriptions-item label="剩余天数">{{ currentRow.remainingDays }}天</el-descriptions-item>
        <el-descriptions-item label="库存">{{ currentRow.stockQuantity }} {{ currentRow.unit }}</el-descriptions-item>
        <el-descriptions-item label="催销措施">{{ measureMap[currentRow.saleMeasure] || currentRow.saleMeasure || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusMap[currentRow.status] }}</el-descriptions-item>
        <el-descriptions-item label="措施说明" :span="2">{{ currentRow.measureDetail || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结果说明" :span="2">{{ currentRow.resultRemark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Printer } from '@element-plus/icons-vue'
import {
  generateNearExpirySale, getNearExpirySalePage,
  updateNearExpirySaleMeasure, completeNearExpirySale
} from '@/api/gsp'
import { getConfigValue, setConfigValue } from '@/api/system'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])

const now = new Date()
const currentMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`

const queryForm = reactive({ month: currentMonth, status: '' })
const pagination = reactive({ current: 1, size: 20, total: 0 })

const measureMap = { discount: '降价促销', bundle: '捆绑销售', return: '退货给供应商', accelerate: '加快销售', other: '其他' }
const statusMap = { pending: '待处理', processing: '处理中', completed: '已完成' }
const statusColor = { pending: 'warning', processing: 'primary', completed: 'success' }

const currentRow = reactive({
  drugName: '', specification: '', manufacturer: '', batchNo: '', expireDate: '',
  remainingDays: 0, stockQuantity: 0, unit: '', saleMeasure: '', measureDetail: '',
  status: '', resultRemark: ''
})

// ===== 自动生成设置 =====
const autoGenDay = ref(0)
const autoGenDayEdit = ref(0)
const autoGenDialogVisible = ref(false)

const loadAutoGenSetting = async () => {
  try {
    const res = await getConfigValue('near_expiry_auto_gen_day')
    autoGenDay.value = parseInt(res.data) || 0
  } catch {}
}

const showAutoGenSetting = () => {
  autoGenDayEdit.value = autoGenDay.value
  autoGenDialogVisible.value = true
}

const saveAutoGenSetting = async () => {
  submitting.value = true
  try {
    await setConfigValue({ group: 'gsp', key: 'near_expiry_auto_gen_day', value: String(autoGenDayEdit.value), description: '近效期催销表每月自动生成日期' })
    autoGenDay.value = autoGenDayEdit.value
    autoGenDialogVisible.value = false
    ElMessage.success(autoGenDayEdit.value > 0 ? `已设置每月${autoGenDayEdit.value}号自动生成` : '已关闭自动生成')
  } catch { ElMessage.error('保存失败') }
  finally { submitting.value = false }
}

// ===== 数据加载 =====
const loadData = async () => {
  loading.value = true
  try {
    const res = await getNearExpirySalePage({
      pageNum: pagination.current, pageSize: pagination.size,
      month: queryForm.month, status: queryForm.status || undefined
    })
    tableData.value = res.data.records
    pagination.total = Number(res.data.total)
  } catch { ElMessage.error('加载数据失败') }
  finally { loading.value = false }
}

// 首次加载：当月无数据时自动生成
const initLoad = async () => {
  loading.value = true
  try {
    const res = await getNearExpirySalePage({
      pageNum: 1, pageSize: pagination.size, month: currentMonth
    })
    tableData.value = res.data.records
    pagination.total = Number(res.data.total)

    // 当月无数据，自动生成
    if (tableData.value.length === 0) {
      try {
        const genRes = await generateNearExpirySale(currentMonth)
        const count = genRes.data || 0
        if (count > 0) {
          ElMessage.success(`已自动生成 ${count} 条催销记录`)
          const res2 = await getNearExpirySalePage({ pageNum: 1, pageSize: pagination.size, month: currentMonth })
          tableData.value = res2.data.records
          pagination.total = Number(res2.data.total)
        } else {
          ElMessage.info('当前无6个月内到期药品')
        }
      } catch {}
    }
  } catch { ElMessage.error('加载数据失败') }
  finally { loading.value = false }
}

const resetQuery = () => {
  queryForm.month = currentMonth; queryForm.status = ''
  pagination.current = 1; loadData()
}

const handleGenerate = async () => {
  await ElMessageBox.confirm(`确定生成 ${queryForm.month || currentMonth} 的近效期催销表？将自动检索6个月内到期的药品。`, '生成催销表', { type: 'info' })
  loading.value = true
  try {
    const res = await generateNearExpirySale(queryForm.month || currentMonth)
    ElMessage.success(`已生成 ${res.data} 条催销记录`)
    loadData()
  } catch (e) { ElMessage.error(e.response?.data?.message || '生成失败') }
  finally { loading.value = false }
}

// 设置催销措施
const measureDialogVisible = ref(false)
const measureFormRef = ref(null)
const measureForm = reactive({ saleMeasure: '', measureDetail: '' })
const measureRules = {
  saleMeasure: [{ required: true, message: '请选择催销措施', trigger: 'change' }]
}

const handleSetMeasure = (row) => {
  Object.assign(currentRow, row)
  measureForm.saleMeasure = ''
  measureForm.measureDetail = ''
  measureDialogVisible.value = true
}

const handleSubmitMeasure = async () => {
  const valid = await measureFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await updateNearExpirySaleMeasure(currentRow.id, measureForm)
    ElMessage.success('催销措施已设置')
    measureDialogVisible.value = false
    loadData()
  } catch { ElMessage.error('操作失败') }
  finally { submitting.value = false }
}

// 完成处理
const completeDialogVisible = ref(false)
const completeFormRef = ref(null)
const completeForm = reactive({ resultRemark: '' })

const handleComplete = (row) => {
  Object.assign(currentRow, row)
  completeForm.resultRemark = ''
  completeDialogVisible.value = true
}

const handleSubmitComplete = async () => {
  submitting.value = true
  try {
    await completeNearExpirySale(currentRow.id, completeForm)
    ElMessage.success('已完成处理')
    completeDialogVisible.value = false
    loadData()
  } catch { ElMessage.error('操作失败') }
  finally { submitting.value = false }
}

// 详情
const detailDialogVisible = ref(false)
const handleDetail = (row) => {
  Object.assign(currentRow, row)
  detailDialogVisible.value = true
}

const handlePrint = () => {
  const printWindow = window.open('', '_blank')
  const rows = tableData.value.map(row => `
    <tr>
      <td>${row.drugName || ''}</td>
      <td>${row.specification || ''}</td>
      <td>${row.manufacturer || ''}</td>
      <td>${row.batchNo || ''}</td>
      <td>${row.expireDate || ''}</td>
      <td style="text-align:center">${row.remainingDays ?? ''}天</td>
      <td style="text-align:center">${row.stockQuantity ?? ''}</td>
      <td>${row.unit || ''}</td>
      <td>${measureMap[row.saleMeasure] || row.saleMeasure || ''}</td>
      <td style="text-align:center">${statusMap[row.status] || ''}</td>
    </tr>`).join('')
  printWindow.document.write(`<!DOCTYPE html><html><head><title>近效期药品催销表</title>
<style>
  body { font-family: SimSun, serif; margin: 20px; }
  h2 { text-align: center; margin-bottom: 5px; }
  .sub { text-align: center; color: #666; margin-bottom: 15px; font-size: 14px; }
  table { width: 100%; border-collapse: collapse; font-size: 12px; }
  th, td { border: 1px solid #333; padding: 5px 8px; }
  th { background: #f0f0f0; font-weight: bold; }
  @media print { @page { size: landscape; margin: 10mm; } }
</style></head><body>
<h2>近效期药品催销表</h2>
<div class="sub">月份：${queryForm.month || currentMonth}</div>
<table>
  <thead><tr>
    <th>药品名称</th><th>规格</th><th>生产企业</th><th>批号</th>
    <th>有效期至</th><th>剩余天数</th><th>库存</th><th>单位</th>
    <th>催销措施</th><th>状态</th>
  </tr></thead>
  <tbody>${rows || '<tr><td colspan="10" style="text-align:center">暂无数据</td></tr>'}</tbody>
</table>
<div class="sub" style="margin-top:20px">打印日期：${new Date().toLocaleDateString('zh-CN')}</div>
</body></html>`)
  printWindow.document.close()
  printWindow.onload = () => { printWindow.print() }
}

onMounted(() => { initLoad(); loadAutoGenSetting() })
</script>

<style scoped>
.near-expiry-sale { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
.text-danger { color: #f56c6c; font-weight: bold; }
@media print {
  .search-form, .el-pagination, .card-header .el-button, .el-alert { display: none !important; }
}
</style>
