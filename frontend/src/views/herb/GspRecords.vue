<template>
  <div class="gsp-records-container">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 装斗记录 -->
        <el-tab-pane label="装斗记录" name="fill">
          <div class="tab-content">
            <div class="toolbar">
              <el-button type="primary" @click="handleAddFillRecord">
                <el-icon><Plus /></el-icon>
                新增装斗记录
              </el-button>
            </div>

            <el-table
              v-loading="fillLoading"
              :data="fillRecords"
              stripe
              border
              style="width: 100%"
            >
              <el-table-column prop="herbName" label="药材名称" width="150" />
              <el-table-column prop="batchNo" label="批号" width="150" />
              <el-table-column label="装斗重量" width="120">
                <template #default="{ row }">
                  {{ row.fillWeight }}g
                </template>
              </el-table-column>
              <el-table-column prop="targetCell" label="目标斗格" width="120" />
              <el-table-column prop="operator" label="操作人" width="100" />
              <el-table-column prop="fillTime" label="装斗时间" width="160" />
              <el-table-column prop="preCheckResult" label="装前检查结果" width="120">
                <template #default="{ row }">
                  <el-tag :type="row.preCheckResult === '合格' ? 'success' : 'danger'">
                    {{ row.preCheckResult }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="remarks" label="备注" min-width="150" show-overflow-tooltip />
            </el-table>

            <div class="pagination">
              <el-pagination
                v-model:current-page="fillPagination.current"
                v-model:page-size="fillPagination.size"
                :page-sizes="[10, 20, 50]"
                :total="fillPagination.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="fetchFillRecords"
                @current-change="fetchFillRecords"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 清斗记录 -->
        <el-tab-pane label="清斗记录" name="clean">
          <div class="tab-content">
            <div class="toolbar">
              <el-button type="primary" @click="handleAddCleanRecord">
                <el-icon><Plus /></el-icon>
                新增清斗记录
              </el-button>
            </div>

            <el-table
              v-loading="cleanLoading"
              :data="cleanRecords"
              stripe
              border
              style="width: 100%"
            >
              <el-table-column prop="cellLocation" label="斗格位置" width="120" />
              <el-table-column prop="originalHerbName" label="原药材名称" width="150" />
              <el-table-column label="剩余重量" width="120">
                <template #default="{ row }">
                  {{ row.remainingWeight }}g
                </template>
              </el-table-column>
              <el-table-column prop="cleanReason" label="清斗原因" width="150" />
              <el-table-column prop="cleanMethod" label="清理方式" width="120" />
              <el-table-column prop="operator" label="操作人" width="100" />
              <el-table-column prop="cleanTime" label="清斗时间" width="160" />
              <el-table-column prop="remarks" label="备注" min-width="150" show-overflow-tooltip />
            </el-table>

            <div class="pagination">
              <el-pagination
                v-model:current-page="cleanPagination.current"
                v-model:page-size="cleanPagination.size"
                :page-sizes="[10, 20, 50]"
                :total="cleanPagination.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="fetchCleanRecords"
                @current-change="fetchCleanRecords"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 验收记录 -->
        <el-tab-pane label="验收记录" name="acceptance">
          <div class="tab-content">
            <div class="toolbar">
              <el-button type="primary" @click="handleAddAcceptance">
                <el-icon><Plus /></el-icon>
                新增验收记录
              </el-button>
            </div>

            <el-table
              v-loading="acceptanceLoading"
              :data="acceptanceRecords"
              stripe
              border
              style="width: 100%"
            >
              <el-table-column prop="herbName" label="药材名称" width="150" />
              <el-table-column prop="batchNo" label="批号" width="150" />
              <el-table-column prop="supplier" label="供应商" width="180" />
              <el-table-column label="验收数量" width="120">
                <template #default="{ row }">
                  {{ row.quantity }}{{ row.unit || 'g' }}
                </template>
              </el-table-column>
              <el-table-column prop="acceptor" label="验收人" width="100" />
              <el-table-column prop="acceptTime" label="验收时间" width="160" />
              <el-table-column prop="overallResult" label="综合判定" width="120">
                <template #default="{ row }">
                  <el-tag :type="getResultType(row.overallResult)">
                    {{ row.overallResult }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="remarks" label="备注" min-width="150" show-overflow-tooltip />
            </el-table>

            <div class="pagination">
              <el-pagination
                v-model:current-page="acceptancePagination.current"
                v-model:page-size="acceptancePagination.size"
                :page-sizes="[10, 20, 50]"
                :total="acceptancePagination.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="fetchAcceptances"
                @current-change="fetchAcceptances"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 养护记录 -->
        <el-tab-pane label="养护记录" name="maintenance">
          <div class="tab-content">
            <div class="toolbar">
              <el-button type="primary" @click="handleAddMaintenance">
                <el-icon><Plus /></el-icon>
                新增养护记录
              </el-button>
            </div>

            <el-table
              v-loading="maintenanceLoading"
              :data="maintenanceRecords"
              stripe
              border
              style="width: 100%"
            >
              <el-table-column prop="herbName" label="药材名称" width="150" />
              <el-table-column prop="maintenanceType" label="养护类型" width="120" />
              <el-table-column prop="operator" label="养护人" width="100" />
              <el-table-column prop="maintenanceTime" label="养护时间" width="160" />
              <el-table-column prop="overallResult" label="养护结果" width="120">
                <template #default="{ row }">
                  <el-tag :type="getResultType(row.overallResult)">
                    {{ row.overallResult }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="nextMaintenance" label="下次养护时间" width="160" />
              <el-table-column prop="remarks" label="备注" min-width="150" show-overflow-tooltip />
            </el-table>

            <div class="pagination">
              <el-pagination
                v-model:current-page="maintenancePagination.current"
                v-model:page-size="maintenancePagination.size"
                :page-sizes="[10, 20, 50]"
                :total="maintenancePagination.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="fetchMaintenances"
                @current-change="fetchMaintenances"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 装斗记录对话框 -->
    <el-dialog
      v-model="fillDialogVisible"
      title="新增装斗记录"
      width="600px"
      @close="handleFillDialogClose"
    >
      <el-form ref="fillFormRef" :model="fillForm" :rules="fillFormRules" label-width="120px">
        <el-form-item label="药材名称" prop="herbName">
          <el-input v-model="fillForm.herbName" placeholder="请输入药材名称" />
        </el-form-item>
        <el-form-item label="批号" prop="batchNo">
          <el-input v-model="fillForm.batchNo" placeholder="请输入批号" />
        </el-form-item>
        <el-form-item label="装斗重量(g)" prop="fillWeight">
          <el-input-number v-model="fillForm.fillWeight" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="目标斗格" prop="targetCell">
          <el-input v-model="fillForm.targetCell" placeholder="如：A-01" />
        </el-form-item>
        <el-form-item label="装前检查结果" prop="preCheckResult">
          <el-select v-model="fillForm.preCheckResult" placeholder="请选择">
            <el-option label="合格" value="合格" />
            <el-option label="不合格" value="不合格" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input v-model="fillForm.remarks" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="fillDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="fillSubmitLoading" @click="handleFillSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 清斗记录对话框 -->
    <el-dialog
      v-model="cleanDialogVisible"
      title="新增清斗记录"
      width="600px"
      @close="handleCleanDialogClose"
    >
      <el-form ref="cleanFormRef" :model="cleanForm" :rules="cleanFormRules" label-width="120px">
        <el-form-item label="斗格位置" prop="cellLocation">
          <el-input v-model="cleanForm.cellLocation" placeholder="如：A-01" />
        </el-form-item>
        <el-form-item label="原药材名称" prop="originalHerbName">
          <el-input v-model="cleanForm.originalHerbName" placeholder="请输入原药材名称" />
        </el-form-item>
        <el-form-item label="剩余重量(g)" prop="remainingWeight">
          <el-input-number v-model="cleanForm.remainingWeight" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="清斗原因" prop="cleanReason">
          <el-select v-model="cleanForm.cleanReason" placeholder="请选择">
            <el-option label="药材更换" value="药材更换" />
            <el-option label="药材变质" value="药材变质" />
            <el-option label="定期清理" value="定期清理" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="清理方式" prop="cleanMethod">
          <el-select v-model="cleanForm.cleanMethod" placeholder="请选择">
            <el-option label="全部清空" value="全部清空" />
            <el-option label="部分清理" value="部分清理" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input v-model="cleanForm.remarks" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="cleanDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="cleanSubmitLoading" @click="handleCleanSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 验收记录对话框 -->
    <el-dialog
      v-model="acceptanceDialogVisible"
      title="新增验收记录"
      width="600px"
      @close="handleAcceptanceDialogClose"
    >
      <el-form
        ref="acceptanceFormRef"
        :model="acceptanceForm"
        :rules="acceptanceFormRules"
        label-width="120px"
      >
        <el-form-item label="药材名称" prop="herbName">
          <el-input v-model="acceptanceForm.herbName" placeholder="请输入药材名称" />
        </el-form-item>
        <el-form-item label="批号" prop="batchNo">
          <el-input v-model="acceptanceForm.batchNo" placeholder="请输入批号" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplier">
          <el-input v-model="acceptanceForm.supplier" placeholder="请输入供应商" />
        </el-form-item>
        <el-form-item label="验收数量" prop="quantity">
          <el-input-number v-model="acceptanceForm.quantity" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="综合判定" prop="overallResult">
          <el-select v-model="acceptanceForm.overallResult" placeholder="请选择">
            <el-option label="合格" value="合格" />
            <el-option label="不合格" value="不合格" />
            <el-option label="待复检" value="待复检" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input v-model="acceptanceForm.remarks" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="acceptanceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="acceptanceSubmitLoading" @click="handleAcceptanceSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 养护记录对话框 -->
    <el-dialog
      v-model="maintenanceDialogVisible"
      title="新增养护记录"
      width="600px"
      @close="handleMaintenanceDialogClose"
    >
      <el-form
        ref="maintenanceFormRef"
        :model="maintenanceForm"
        :rules="maintenanceFormRules"
        label-width="120px"
      >
        <el-form-item label="药材名称" prop="herbName">
          <el-input v-model="maintenanceForm.herbName" placeholder="请输入药材名称" />
        </el-form-item>
        <el-form-item label="养护类型" prop="maintenanceType">
          <el-select v-model="maintenanceForm.maintenanceType" placeholder="请选择">
            <el-option label="日常养护" value="日常养护" />
            <el-option label="重点养护" value="重点养护" />
            <el-option label="专项养护" value="专项养护" />
          </el-select>
        </el-form-item>
        <el-form-item label="养护结果" prop="overallResult">
          <el-select v-model="maintenanceForm.overallResult" placeholder="请选择">
            <el-option label="正常" value="正常" />
            <el-option label="异常" value="异常" />
            <el-option label="需处理" value="需处理" />
          </el-select>
        </el-form-item>
        <el-form-item label="下次养护时间" prop="nextMaintenance">
          <el-date-picker
            v-model="maintenanceForm.nextMaintenance"
            type="datetime"
            placeholder="选择日期时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input v-model="maintenanceForm.remarks" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="maintenanceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="maintenanceSubmitLoading" @click="handleMaintenanceSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getFillRecords,
  createFillRecord,
  getCleanRecords,
  createCleanRecord,
  getAcceptances,
  createAcceptance,
  getMaintenances,
  createMaintenance
} from '@/api/herb'

// Tab 管理
const activeTab = ref('fill')

// ============ 装斗记录 ============
const fillLoading = ref(false)
const fillRecords = ref([])
const fillPagination = reactive({ current: 1, size: 10, total: 0 })
const fillDialogVisible = ref(false)
const fillFormRef = ref(null)
const fillSubmitLoading = ref(false)

const fillForm = reactive({
  herbName: '',
  batchNo: '',
  fillWeight: 0,
  targetCell: '',
  preCheckResult: '',
  remarks: ''
})

const fillFormRules = {
  herbName: [{ required: true, message: '请输入药材名称', trigger: 'blur' }],
  batchNo: [{ required: true, message: '请输入批号', trigger: 'blur' }],
  fillWeight: [{ required: true, message: '请输入装斗重量', trigger: 'blur' }],
  targetCell: [{ required: true, message: '请输入目标斗格', trigger: 'blur' }],
  preCheckResult: [{ required: true, message: '请选择装前检查结果', trigger: 'change' }]
}

// ============ 清斗记录 ============
const cleanLoading = ref(false)
const cleanRecords = ref([])
const cleanPagination = reactive({ current: 1, size: 10, total: 0 })
const cleanDialogVisible = ref(false)
const cleanFormRef = ref(null)
const cleanSubmitLoading = ref(false)

const cleanForm = reactive({
  cellLocation: '',
  originalHerbName: '',
  remainingWeight: 0,
  cleanReason: '',
  cleanMethod: '',
  remarks: ''
})

const cleanFormRules = {
  cellLocation: [{ required: true, message: '请输入斗格位置', trigger: 'blur' }],
  originalHerbName: [{ required: true, message: '请输入原药材名称', trigger: 'blur' }],
  remainingWeight: [{ required: true, message: '请输入剩余重量', trigger: 'blur' }],
  cleanReason: [{ required: true, message: '请选择清斗原因', trigger: 'change' }],
  cleanMethod: [{ required: true, message: '请选择清理方式', trigger: 'change' }]
}

// ============ 验收记录 ============
const acceptanceLoading = ref(false)
const acceptanceRecords = ref([])
const acceptancePagination = reactive({ current: 1, size: 10, total: 0 })
const acceptanceDialogVisible = ref(false)
const acceptanceFormRef = ref(null)
const acceptanceSubmitLoading = ref(false)

const acceptanceForm = reactive({
  herbName: '',
  batchNo: '',
  supplier: '',
  quantity: 0,
  overallResult: '',
  remarks: ''
})

const acceptanceFormRules = {
  herbName: [{ required: true, message: '请输入药材名称', trigger: 'blur' }],
  batchNo: [{ required: true, message: '请输入批号', trigger: 'blur' }],
  supplier: [{ required: true, message: '请输入供应商', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入验收数量', trigger: 'blur' }],
  overallResult: [{ required: true, message: '请选择综合判定', trigger: 'change' }]
}

// ============ 养护记录 ============
const maintenanceLoading = ref(false)
const maintenanceRecords = ref([])
const maintenancePagination = reactive({ current: 1, size: 10, total: 0 })
const maintenanceDialogVisible = ref(false)
const maintenanceFormRef = ref(null)
const maintenanceSubmitLoading = ref(false)

const maintenanceForm = reactive({
  herbName: '',
  maintenanceType: '',
  overallResult: '',
  nextMaintenance: '',
  remarks: ''
})

const maintenanceFormRules = {
  herbName: [{ required: true, message: '请输入药材名称', trigger: 'blur' }],
  maintenanceType: [{ required: true, message: '请选择养护类型', trigger: 'change' }],
  overallResult: [{ required: true, message: '请选择养护结果', trigger: 'change' }]
}

// ============ 通用方法 ============
const getResultType = (result) => {
  const typeMap = {
    合格: 'success',
    正常: 'success',
    不合格: 'danger',
    异常: 'danger',
    待复检: 'warning',
    需处理: 'warning'
  }
  return typeMap[result] || 'info'
}

// ============ 装斗记录方法 ============
const fetchFillRecords = async () => {
  fillLoading.value = true
  try {
    const params = {
      current: fillPagination.current,
      size: fillPagination.size
    }
    const res = await getFillRecords(params)
    if (res.code === 200) {
      fillRecords.value = res.data.records || []
      fillPagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('获取装斗记录失败')
  } finally {
    fillLoading.value = false
  }
}

const handleAddFillRecord = () => {
  fillDialogVisible.value = true
}

const handleFillSubmit = async () => {
  if (!fillFormRef.value) return
  await fillFormRef.value.validate(async (valid) => {
    if (!valid) return

    fillSubmitLoading.value = true
    try {
      const res = await createFillRecord(fillForm)
      if (res.code === 200) {
        ElMessage.success('新增成功')
        fillDialogVisible.value = false
        fetchFillRecords()
      }
    } catch (error) {
      ElMessage.error('新增失败')
    } finally {
      fillSubmitLoading.value = false
    }
  })
}

const handleFillDialogClose = () => {
  Object.assign(fillForm, {
    herbName: '',
    batchNo: '',
    fillWeight: 0,
    targetCell: '',
    preCheckResult: '',
    remarks: ''
  })
  fillFormRef.value?.clearValidate()
}

// ============ 清斗记录方法 ============
const fetchCleanRecords = async () => {
  cleanLoading.value = true
  try {
    const params = {
      current: cleanPagination.current,
      size: cleanPagination.size
    }
    const res = await getCleanRecords(params)
    if (res.code === 200) {
      cleanRecords.value = res.data.records || []
      cleanPagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('获取清斗记录失败')
  } finally {
    cleanLoading.value = false
  }
}

const handleAddCleanRecord = () => {
  cleanDialogVisible.value = true
}

const handleCleanSubmit = async () => {
  if (!cleanFormRef.value) return
  await cleanFormRef.value.validate(async (valid) => {
    if (!valid) return

    cleanSubmitLoading.value = true
    try {
      const res = await createCleanRecord(cleanForm)
      if (res.code === 200) {
        ElMessage.success('新增成功')
        cleanDialogVisible.value = false
        fetchCleanRecords()
      }
    } catch (error) {
      ElMessage.error('新增失败')
    } finally {
      cleanSubmitLoading.value = false
    }
  })
}

const handleCleanDialogClose = () => {
  Object.assign(cleanForm, {
    cellLocation: '',
    originalHerbName: '',
    remainingWeight: 0,
    cleanReason: '',
    cleanMethod: '',
    remarks: ''
  })
  cleanFormRef.value?.clearValidate()
}

// ============ 验收记录方法 ============
const fetchAcceptances = async () => {
  acceptanceLoading.value = true
  try {
    const params = {
      current: acceptancePagination.current,
      size: acceptancePagination.size
    }
    const res = await getAcceptances(params)
    if (res.code === 200) {
      acceptanceRecords.value = res.data.records || []
      acceptancePagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('获取验收记录失败')
  } finally {
    acceptanceLoading.value = false
  }
}

const handleAddAcceptance = () => {
  acceptanceDialogVisible.value = true
}

const handleAcceptanceSubmit = async () => {
  if (!acceptanceFormRef.value) return
  await acceptanceFormRef.value.validate(async (valid) => {
    if (!valid) return

    acceptanceSubmitLoading.value = true
    try {
      const res = await createAcceptance(acceptanceForm)
      if (res.code === 200) {
        ElMessage.success('新增成功')
        acceptanceDialogVisible.value = false
        fetchAcceptances()
      }
    } catch (error) {
      ElMessage.error('新增失败')
    } finally {
      acceptanceSubmitLoading.value = false
    }
  })
}

const handleAcceptanceDialogClose = () => {
  Object.assign(acceptanceForm, {
    herbName: '',
    batchNo: '',
    supplier: '',
    quantity: 0,
    overallResult: '',
    remarks: ''
  })
  acceptanceFormRef.value?.clearValidate()
}

// ============ 养护记录方法 ============
const fetchMaintenances = async () => {
  maintenanceLoading.value = true
  try {
    const params = {
      current: maintenancePagination.current,
      size: maintenancePagination.size
    }
    const res = await getMaintenances(params)
    if (res.code === 200) {
      maintenanceRecords.value = res.data.records || []
      maintenancePagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('获取养护记录失败')
  } finally {
    maintenanceLoading.value = false
  }
}

const handleAddMaintenance = () => {
  maintenanceDialogVisible.value = true
}

const handleMaintenanceSubmit = async () => {
  if (!maintenanceFormRef.value) return
  await maintenanceFormRef.value.validate(async (valid) => {
    if (!valid) return

    maintenanceSubmitLoading.value = true
    try {
      const res = await createMaintenance(maintenanceForm)
      if (res.code === 200) {
        ElMessage.success('新增成功')
        maintenanceDialogVisible.value = false
        fetchMaintenances()
      }
    } catch (error) {
      ElMessage.error('新增失败')
    } finally {
      maintenanceSubmitLoading.value = false
    }
  })
}

const handleMaintenanceDialogClose = () => {
  Object.assign(maintenanceForm, {
    herbName: '',
    maintenanceType: '',
    overallResult: '',
    nextMaintenance: '',
    remarks: ''
  })
  maintenanceFormRef.value?.clearValidate()
}

// ============ Tab 切换 ============
const handleTabChange = (tabName) => {
  switch (tabName) {
    case 'fill':
      fetchFillRecords()
      break
    case 'clean':
      fetchCleanRecords()
      break
    case 'acceptance':
      fetchAcceptances()
      break
    case 'maintenance':
      fetchMaintenances()
      break
  }
}

onMounted(() => {
  fetchFillRecords()
})
</script>

<style scoped lang="scss">
.gsp-records-container {
  padding: 20px;

  .tab-content {
    .toolbar {
      margin-bottom: 16px;
    }

    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
