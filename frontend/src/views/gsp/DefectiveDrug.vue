<template>
  <div class="defective-drug">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>不合格药品台帐</span>
          <div>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>登记不良品
            </el-button>
            <el-button @click="handlePrint">
              <el-icon><Printer /></el-icon>打印台帐
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="queryForm.drugName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="已锁定" value="locked" />
            <el-option label="处理中" value="processing" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker v-model="queryForm.dateRange" type="daterange" start-placeholder="开始"
            end-placeholder="结束" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="registerNo" label="台帐编号" width="160" />
        <el-table-column prop="drugName" label="品名" min-width="130" />
        <el-table-column prop="specification" label="规格" width="100" />
        <el-table-column prop="manufacturer" label="生产企业" width="140" show-overflow-tooltip />
        <el-table-column prop="batchNo" label="批号" width="110" />
        <el-table-column prop="expireDate" label="有效期至" width="100" />
        <el-table-column prop="quantity" label="数量" width="70" align="center" />
        <el-table-column prop="unit" label="单位" width="50" />
        <el-table-column prop="defectReason" label="不合格原因" width="100">
          <template #default="{ row }">
            <el-tag :type="getDefectTypeColor(row.defectReason)" size="small">{{ getDefectTypeName(row.defectReason) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="discoveryDate" label="发现日期" width="100" />
        <el-table-column prop="discovererName" label="发现人" width="80" />
        <el-table-column prop="isolationLocation" label="隔离存放" width="100" />
        <el-table-column prop="disposalMethod" label="处置方式" width="90">
          <template #default="{ row }">{{ disposalMap[row.disposalMethod] || row.disposalMethod || '-' }}</template>
        </el-table-column>
        <el-table-column prop="disposalDate" label="处置日期" width="100" />
        <el-table-column prop="disposalHandlerName" label="处置人" width="80" />
        <el-table-column prop="status" label="状态" width="80" fixed="right">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'locked'" type="warning" link @click="handleDispose(row)">处置</el-button>
            <el-button v-if="row.status === 'processing'" type="success" link @click="handleComplete(row)">完成</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 登记对话框 -->
    <el-dialog v-model="dialogVisible" title="登记不合格药品" width="650px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="药品" prop="drugId">
          <el-select v-model="form.drugId" placeholder="请选择药品" filterable style="width: 100%;">
            <el-option v-for="d in drugList" :key="d.id" :label="`${d.genericName || d.tradeName} - ${d.specification || ''}`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="批号">
              <el-input v-model="form.batchNo" placeholder="批号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="form.quantity" :min="1" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="不合格原因" prop="defectReason">
          <el-select v-model="form.defectReason" placeholder="请选择" style="width: 100%;">
            <el-option label="过期" value="expired" />
            <el-option label="破损" value="damaged" />
            <el-option label="质量问题" value="quality" />
            <el-option label="召回" value="recall" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述" prop="defectDescription">
          <el-input v-model="form.defectDescription" type="textarea" :rows="2" placeholder="请描述不合格情况" />
        </el-form-item>
        <el-form-item label="隔离位置">
          <el-input v-model="form.isolationLocation" placeholder="不合格药品存放位置" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 处置对话框 -->
    <el-dialog v-model="disposeDialogVisible" title="处置不合格药品" width="500px">
      <el-form ref="disposeFormRef" :model="disposeForm" :rules="disposeRules" label-width="100px">
        <el-form-item label="药品">
          <el-input :model-value="`${currentRow.drugName} (${currentRow.registerNo})`" disabled />
        </el-form-item>
        <el-form-item label="处置方式" prop="disposalMethod">
          <el-select v-model="disposeForm.disposalMethod" style="width: 100%;">
            <el-option label="退货" value="return" />
            <el-option label="销毁" value="destroy" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="处置人" prop="disposalHandlerName">
          <el-input v-model="disposeForm.disposalHandlerName" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="disposeForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="disposeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitDispose" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="不合格药品详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="台帐编号">{{ currentRow.registerNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusColor(currentRow.status)">{{ getStatusName(currentRow.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="品名">{{ currentRow.drugName }}</el-descriptions-item>
        <el-descriptions-item label="规格">{{ currentRow.specification }}</el-descriptions-item>
        <el-descriptions-item label="生产企业" :span="2">{{ currentRow.manufacturer }}</el-descriptions-item>
        <el-descriptions-item label="批号">{{ currentRow.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="有效期至">{{ currentRow.expireDate }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ currentRow.quantity }} {{ currentRow.unit }}</el-descriptions-item>
        <el-descriptions-item label="不合格原因">{{ getDefectTypeName(currentRow.defectReason) }}</el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">{{ currentRow.defectDescription || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发现日期">{{ currentRow.discoveryDate }}</el-descriptions-item>
        <el-descriptions-item label="发现人">{{ currentRow.discovererName }}</el-descriptions-item>
        <el-descriptions-item label="隔离存放">{{ currentRow.isolationLocation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处置方式">{{ disposalMap[currentRow.disposalMethod] || currentRow.disposalMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处置日期">{{ currentRow.disposalDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处置人">{{ currentRow.disposalHandlerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Printer } from '@element-plus/icons-vue'
import { getDefectiveDrugPage, registerDefectiveDrug, disposeDefectiveDrug, completeDefectiveDrug } from '@/api/gsp'
import { getDrugList } from '@/api/drug'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const drugList = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)

const queryForm = reactive({ drugName: '', status: '', dateRange: null })
const pagination = reactive({ current: 1, size: 20, total: 0 })

const defectTypeMap = { expired: '过期', damaged: '破损', quality: '质量问题', recall: '召回', other: '其他' }
const defectTypeColorMap = { expired: 'danger', damaged: 'warning', quality: 'danger', recall: 'danger', other: 'info' }
const statusMap = { locked: '已锁定', processing: '处理中', completed: '已完成' }
const statusColorMap = { locked: 'danger', processing: 'warning', completed: 'success' }
const disposalMap = { return: '退货', destroy: '销毁', other: '其他' }

const getDefectTypeName = (t) => defectTypeMap[t] || t || '-'
const getDefectTypeColor = (t) => defectTypeColorMap[t] || 'info'
const getStatusName = (s) => statusMap[s] || s || '-'
const getStatusColor = (s) => statusColorMap[s] || 'info'

const currentRow = reactive({})

const form = reactive({
  drugId: null, batchNo: '', quantity: 1, defectReason: '', defectDescription: '', isolationLocation: ''
})
const rules = {
  drugId: [{ required: true, message: '请选择药品', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  defectReason: [{ required: true, message: '请选择不合格原因', trigger: 'change' }],
  defectDescription: [{ required: true, message: '请描述问题', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { current: pagination.current, size: pagination.size, drugName: queryForm.drugName || undefined, status: queryForm.status || undefined }
    if (queryForm.dateRange && queryForm.dateRange.length === 2) {
      params.startDate = queryForm.dateRange[0]
      params.endDate = queryForm.dateRange[1]
    }
    const res = await getDefectiveDrugPage(params)
    tableData.value = res.data.records
    pagination.total = Number(res.data.total)
  } catch { ElMessage.error('加载数据失败') }
  finally { loading.value = false }
}

const loadDrugList = async () => {
  try {
    const res = await getDrugList()
    drugList.value = res.data
  } catch {}
}

const resetQuery = () => {
  queryForm.drugName = ''; queryForm.status = ''; queryForm.dateRange = null
  pagination.current = 1; loadData()
}

const handleAdd = () => {
  Object.assign(form, { drugId: null, batchNo: '', quantity: 1, defectReason: '', defectDescription: '', isolationLocation: '' })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    // 预填发现人
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    const data = { ...form, discovererId: user.id, discovererName: user.realName }
    await registerDefectiveDrug(data)
    ElMessage.success('登记成功，台帐编号已自动生成')
    dialogVisible.value = false
    loadData()
  } catch { ElMessage.error('操作失败') }
  finally { submitting.value = false }
}

// 处置
const disposeDialogVisible = ref(false)
const disposeFormRef = ref(null)
const disposeForm = reactive({ disposalMethod: '', disposalHandlerName: '', remark: '' })
const disposeRules = {
  disposalMethod: [{ required: true, message: '请选择处置方式', trigger: 'change' }],
  disposalHandlerName: [{ required: true, message: '请输入处置人', trigger: 'blur' }]
}

const handleDispose = (row) => {
  Object.assign(currentRow, row)
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  disposeForm.disposalMethod = ''
  disposeForm.disposalHandlerName = user.realName || ''
  disposeForm.remark = ''
  disposeDialogVisible.value = true
}

const handleSubmitDispose = async () => {
  const valid = await disposeFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await disposeDefectiveDrug(currentRow.id, disposeForm)
    ElMessage.success('处置成功')
    disposeDialogVisible.value = false
    loadData()
  } catch { ElMessage.error('操作失败') }
  finally { submitting.value = false }
}

const handleComplete = async (row) => {
  await ElMessageBox.confirm('确定该不合格药品已处置完成？', '提示', { type: 'warning' })
  try {
    await completeDefectiveDrug(row.id)
    ElMessage.success('已完成处置')
    loadData()
  } catch { ElMessage.error('操作失败') }
}

const detailDialogVisible = ref(false)
const handleDetail = (row) => {
  Object.assign(currentRow, row)
  detailDialogVisible.value = true
}

const handlePrint = () => { window.print() }

onMounted(() => { loadData(); loadDrugList() })
</script>

<style scoped>
.defective-drug { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
@media print {
  .search-form, .el-pagination, .card-header .el-button { display: none !important; }
}
</style>
