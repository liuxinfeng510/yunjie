<template>
  <div class="equipment-manage">
    <el-tabs v-model="activeTab">
      <!-- 设备管理 Tab -->
      <el-tab-pane label="设备管理" name="equipment">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>养护设备管理</span>
              <el-button type="primary" @click="handleAddEquipment">
                <el-icon><Plus /></el-icon>添加设备
              </el-button>
            </div>
          </template>

          <el-form :inline="true" :model="eqQuery" class="search-form">
            <el-form-item label="设备类型">
              <el-select v-model="eqQuery.equipmentType" placeholder="请选择" clearable>
                <el-option label="空调" value="air_conditioner" />
                <el-option label="除湿机" value="dehumidifier" />
                <el-option label="温湿度计" value="hygrometer" />
                <el-option label="冷藏柜" value="refrigerator" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="eqQuery.status" placeholder="请选择" clearable>
                <el-option label="正常" value="active" />
                <el-option label="待检" value="pending_inspection" />
                <el-option label="停用" value="retired" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadEquipments">查询</el-button>
              <el-button @click="resetEqQuery">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="eqTableData" v-loading="eqLoading" stripe>
            <el-table-column prop="equipmentName" label="设备名称" min-width="130" />
            <el-table-column prop="equipmentType" label="设备类型" width="100">
              <template #default="{ row }">{{ eqTypeMap[row.equipmentType] || row.equipmentType }}</template>
            </el-table-column>
            <el-table-column prop="model" label="型号" width="120" />
            <el-table-column prop="manufacturer" label="生产厂家" width="130" />
            <el-table-column prop="location" label="存放位置" width="100" />
            <el-table-column prop="purchaseDate" label="购买日期" width="110" />
            <el-table-column prop="inspectionCycle" label="检查周期" width="90">
              <template #default="{ row }">{{ cycleMap[row.inspectionCycle] || row.inspectionCycle }}</template>
            </el-table-column>
            <el-table-column prop="lastInspectionDate" label="上次检查" width="110" />
            <el-table-column prop="nextInspectionDate" label="下次检查" width="110">
              <template #default="{ row }">
                <span :class="{ 'text-danger': isOverdue(row.nextInspectionDate) }">{{ row.nextInspectionDate }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="eqStatusColor[row.status]">{{ eqStatusMap[row.status] || row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEditEquipment(row)">编辑</el-button>
                <el-button type="success" link @click="handleInspect(row)" v-if="row.status !== 'retired'">检查</el-button>
                <el-button type="info" link @click="handleViewInspections(row)">记录</el-button>
                <el-button type="danger" link @click="handleRetire(row)" v-if="row.status !== 'retired'">停用</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="eqPagination.current"
            v-model:page-size="eqPagination.size"
            :total="eqPagination.total"
            :page-sizes="[20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadEquipments"
            @current-change="loadEquipments"
            style="margin-top: 16px; justify-content: flex-end;"
          />
        </el-card>
      </el-tab-pane>

      <!-- 检查记录 Tab -->
      <el-tab-pane label="检查记录" name="inspection">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>设备检查记录</span>
              <el-button @click="handlePrintInspections">
                <el-icon><Printer /></el-icon>打印
              </el-button>
            </div>
          </template>

          <el-form :inline="true" :model="insQuery" class="search-form">
            <el-form-item label="设备">
              <el-select v-model="insQuery.equipmentId" placeholder="请选择设备" clearable filterable>
                <el-option v-for="e in allEquipments" :key="e.id" :label="e.equipmentName" :value="e.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="日期范围">
              <el-date-picker v-model="insQuery.dateRange" type="daterange" start-placeholder="开始" end-placeholder="结束"
                value-format="YYYY-MM-DD" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadInspections">查询</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="insTableData" v-loading="insLoading" stripe>
            <el-table-column prop="equipmentName" label="设备名称" min-width="130" />
            <el-table-column prop="inspectionDate" label="检查日期" width="110" />
            <el-table-column prop="appearanceCheck" label="外观检查" width="90">
              <template #default="{ row }">
                <el-tag :type="row.appearanceCheck === '正常' ? 'success' : 'danger'" size="small">{{ row.appearanceCheck }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="functionCheck" label="功能检查" width="90">
              <template #default="{ row }">
                <el-tag :type="row.functionCheck === '正常' ? 'success' : 'danger'" size="small">{{ row.functionCheck }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="inspectionResult" label="检查结论" width="90">
              <template #default="{ row }">
                <el-tag :type="row.inspectionResult === '合格' ? 'success' : 'danger'">{{ row.inspectionResult }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="abnormalDesc" label="异常描述" min-width="150" show-overflow-tooltip />
            <el-table-column prop="treatment" label="处理措施" min-width="150" show-overflow-tooltip />
            <el-table-column prop="inspectorName" label="检查人" width="90" />
            <el-table-column prop="nextInspectionDate" label="下次检查" width="110" />
          </el-table>

          <el-pagination
            v-model:current-page="insPagination.current"
            v-model:page-size="insPagination.size"
            :total="insPagination.total"
            :page-sizes="[20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadInspections"
            @current-change="loadInspections"
            style="margin-top: 16px; justify-content: flex-end;"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 设备新增/编辑对话框 -->
    <el-dialog v-model="eqDialogVisible" :title="eqDialogTitle" width="600px">
      <el-form ref="eqFormRef" :model="eqForm" :rules="eqRules" label-width="100px">
        <el-form-item label="设备名称" prop="equipmentName">
          <el-input v-model="eqForm.equipmentName" placeholder="如：美的柜式空调" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备类型" prop="equipmentType">
              <el-select v-model="eqForm.equipmentType" placeholder="请选择" style="width:100%">
                <el-option label="空调" value="air_conditioner" />
                <el-option label="除湿机" value="dehumidifier" />
                <el-option label="温湿度计" value="hygrometer" />
                <el-option label="冷藏柜" value="refrigerator" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="型号">
              <el-input v-model="eqForm.model" placeholder="设备型号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="生产厂家">
              <el-input v-model="eqForm.manufacturer" placeholder="生产厂家" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="存放位置">
              <el-input v-model="eqForm.location" placeholder="如：阴凉库" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="购买日期">
              <el-date-picker v-model="eqForm.purchaseDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检查周期" prop="inspectionCycle">
              <el-select v-model="eqForm.inspectionCycle" placeholder="请选择" style="width:100%">
                <el-option label="每月" value="monthly" />
                <el-option label="每季" value="quarterly" />
                <el-option label="半年" value="semi_annual" />
                <el-option label="每年" value="annual" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="eqForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="eqDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitEquipment" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 检查记录对话框 -->
    <el-dialog v-model="insDialogVisible" title="设备检查" width="600px">
      <el-form ref="insFormRef" :model="insForm" :rules="insRules" label-width="100px">
        <el-form-item label="设备">
          <el-input :model-value="insForm.equipmentName" disabled />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="外观检查" prop="appearanceCheck">
              <el-select v-model="insForm.appearanceCheck" style="width:100%">
                <el-option label="正常" value="正常" />
                <el-option label="异常" value="异常" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="功能检查" prop="functionCheck">
              <el-select v-model="insForm.functionCheck" style="width:100%">
                <el-option label="正常" value="正常" />
                <el-option label="异常" value="异常" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="检查结论" prop="inspectionResult">
          <el-select v-model="insForm.inspectionResult" style="width:100%">
            <el-option label="合格" value="合格" />
            <el-option label="不合格" value="不合格" />
          </el-select>
        </el-form-item>
        <el-form-item label="异常描述" v-if="insForm.inspectionResult === '不合格'">
          <el-input v-model="insForm.abnormalDesc" type="textarea" :rows="2" placeholder="描述异常情况" />
        </el-form-item>
        <el-form-item label="处理措施" v-if="insForm.inspectionResult === '不合格'">
          <el-input v-model="insForm.treatment" type="textarea" :rows="2" placeholder="处理措施" />
        </el-form-item>
        <el-form-item label="检查人" prop="inspectorName">
          <el-input v-model="insForm.inspectorName" placeholder="检查人姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="insDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitInspection" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Printer } from '@element-plus/icons-vue'
import {
  getEquipmentPage, getEquipmentList, createEquipment, updateEquipment, retireEquipment,
  createInspection, getInspectionPage
} from '@/api/gsp'

const activeTab = ref('equipment')
const submitting = ref(false)

// ===== 设备管理 =====
const eqLoading = ref(false)
const eqTableData = ref([])
const allEquipments = ref([])
const eqQuery = reactive({ equipmentType: '', status: '' })
const eqPagination = reactive({ current: 1, size: 20, total: 0 })

const eqTypeMap = { air_conditioner: '空调', dehumidifier: '除湿机', hygrometer: '温湿度计', refrigerator: '冷藏柜', other: '其他' }
const cycleMap = { monthly: '每月', quarterly: '每季', semi_annual: '半年', annual: '每年' }
const eqStatusMap = { active: '正常', pending_inspection: '待检', retired: '停用' }
const eqStatusColor = { active: 'success', pending_inspection: 'warning', retired: 'info' }

const isOverdue = (dateStr) => {
  if (!dateStr) return false
  return new Date(dateStr) < new Date()
}

const loadEquipments = async () => {
  eqLoading.value = true
  try {
    const res = await getEquipmentPage({ pageNum: eqPagination.current, pageSize: eqPagination.size, ...eqQuery })
    eqTableData.value = res.data.records
    eqPagination.total = Number(res.data.total)
  } catch { ElMessage.error('加载设备列表失败') }
  finally { eqLoading.value = false }
}

const loadAllEquipments = async () => {
  try {
    const res = await getEquipmentList()
    allEquipments.value = res.data
  } catch {}
}

const resetEqQuery = () => { eqQuery.equipmentType = ''; eqQuery.status = ''; eqPagination.current = 1; loadEquipments() }

// 设备表单
const eqDialogVisible = ref(false)
const eqDialogTitle = ref('添加设备')
const eqFormRef = ref(null)
const eqEditingId = ref(null)
const eqForm = reactive({
  equipmentName: '', equipmentType: '', model: '', manufacturer: '', location: '',
  purchaseDate: '', inspectionCycle: 'quarterly', remark: ''
})
const eqRules = {
  equipmentName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  equipmentType: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
  inspectionCycle: [{ required: true, message: '请选择检查周期', trigger: 'change' }]
}

const handleAddEquipment = () => {
  eqEditingId.value = null
  eqDialogTitle.value = '添加设备'
  Object.assign(eqForm, { equipmentName: '', equipmentType: '', model: '', manufacturer: '', location: '', purchaseDate: '', inspectionCycle: 'quarterly', remark: '' })
  eqDialogVisible.value = true
}

const handleEditEquipment = (row) => {
  eqEditingId.value = row.id
  eqDialogTitle.value = '编辑设备'
  Object.assign(eqForm, {
    equipmentName: row.equipmentName, equipmentType: row.equipmentType, model: row.model,
    manufacturer: row.manufacturer, location: row.location, purchaseDate: row.purchaseDate,
    inspectionCycle: row.inspectionCycle, remark: row.remark
  })
  eqDialogVisible.value = true
}

const handleSubmitEquipment = async () => {
  const valid = await eqFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (eqEditingId.value) {
      await updateEquipment(eqEditingId.value, eqForm)
      ElMessage.success('更新成功')
    } else {
      await createEquipment(eqForm)
      ElMessage.success('添加成功')
    }
    eqDialogVisible.value = false
    loadEquipments()
    loadAllEquipments()
  } catch { ElMessage.error('操作失败') }
  finally { submitting.value = false }
}

const handleRetire = async (row) => {
  await ElMessageBox.confirm(`确定停用设备"${row.equipmentName}"？`, '提示', { type: 'warning' })
  await retireEquipment(row.id)
  ElMessage.success('已停用')
  loadEquipments()
}

// ===== 检查记录 =====
const insLoading = ref(false)
const insTableData = ref([])
const insQuery = reactive({ equipmentId: null, dateRange: null })
const insPagination = reactive({ current: 1, size: 20, total: 0 })

const loadInspections = async () => {
  insLoading.value = true
  try {
    const params = { pageNum: insPagination.current, pageSize: insPagination.size }
    if (insQuery.equipmentId) params.equipmentId = insQuery.equipmentId
    if (insQuery.dateRange && insQuery.dateRange.length === 2) {
      params.startDate = insQuery.dateRange[0]
      params.endDate = insQuery.dateRange[1]
    }
    const res = await getInspectionPage(params)
    insTableData.value = res.data.records
    insPagination.total = Number(res.data.total)
  } catch { ElMessage.error('加载检查记录失败') }
  finally { insLoading.value = false }
}

// 检查表单
const insDialogVisible = ref(false)
const insFormRef = ref(null)
const insForm = reactive({
  equipmentId: null, equipmentName: '', appearanceCheck: '正常', functionCheck: '正常',
  inspectionResult: '合格', abnormalDesc: '', treatment: '', inspectorName: ''
})
const insRules = {
  appearanceCheck: [{ required: true, message: '请选择', trigger: 'change' }],
  functionCheck: [{ required: true, message: '请选择', trigger: 'change' }],
  inspectionResult: [{ required: true, message: '请选择', trigger: 'change' }],
  inspectorName: [{ required: true, message: '请输入检查人', trigger: 'blur' }]
}

const handleInspect = (row) => {
  Object.assign(insForm, {
    equipmentId: row.id, equipmentName: row.equipmentName,
    appearanceCheck: '正常', functionCheck: '正常', inspectionResult: '合格',
    abnormalDesc: '', treatment: '', inspectorName: ''
  })
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  insForm.inspectorName = user.realName || ''
  insDialogVisible.value = true
}

const handleSubmitInspection = async () => {
  const valid = await insFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const { equipmentName, ...data } = insForm
    await createInspection(data)
    ElMessage.success('检查记录已提交')
    insDialogVisible.value = false
    loadEquipments()
    loadInspections()
  } catch { ElMessage.error('提交失败') }
  finally { submitting.value = false }
}

const handleViewInspections = (row) => {
  activeTab.value = 'inspection'
  insQuery.equipmentId = row.id
  loadInspections()
}

const handlePrintInspections = () => { window.print() }

onMounted(() => { loadEquipments(); loadAllEquipments(); loadInspections() })
</script>

<style scoped>
.equipment-manage { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
.text-danger { color: #f56c6c; font-weight: bold; }
@media print {
  .search-form, .el-pagination, .el-tabs__header, .card-header .el-button { display: none !important; }
}
</style>
