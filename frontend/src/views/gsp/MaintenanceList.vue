<template>
  <div class="maintenance-list-container">
    <el-card shadow="never">
      <!-- 操作区 -->
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">新增养护记录</el-button>
        <el-button @click="handlePrint"><el-icon><Printer /></el-icon>打印</el-button>
      </div>

      <!-- 筛选 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="药品名称">
          <el-input v-model="queryForm.drugName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="养护类型">
          <el-select v-model="queryForm.maintenanceType" placeholder="全部" clearable>
            <el-option label="日常养护" value="DAILY" />
            <el-option label="重点养护" value="KEY" />
            <el-option label="特殊养护" value="SPECIAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="重点品种">
          <el-select v-model="queryForm.isKeyDrug" placeholder="全部" clearable>
            <el-option label="是" :value="true" />
            <el-option label="否" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" style="width: 100%" border stripe>
        <el-table-column prop="drugName" label="品名" min-width="130" />
        <el-table-column prop="specification" label="规格" width="100" />
        <el-table-column prop="manufacturer" label="生产企业" width="130" show-overflow-tooltip />
        <el-table-column prop="batchNo" label="批号" width="110" />
        <el-table-column prop="isKeyDrug" label="重点" width="60" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isKeyDrug" type="warning" size="small">是</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maintenanceType" label="养护类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getMaintenanceTypeTag(row.maintenanceType)">
              {{ getMaintenanceTypeLabel(row.maintenanceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="appearanceCheck" label="外观" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.appearanceCheck === 'NORMAL' ? 'success' : 'danger'" size="small" v-if="row.appearanceCheck">
              {{ row.appearanceCheck === 'NORMAL' ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="packageCheck" label="包装" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.packageCheck === 'NORMAL' ? 'success' : 'danger'" size="small" v-if="row.packageCheck">
              {{ row.packageCheck === 'NORMAL' ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="overallResult" label="综合" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.overallResult === 'NORMAL' ? 'success' : 'danger'" size="small">
              {{ row.overallResult === 'NORMAL' ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="treatment" label="处理措施" min-width="130" show-overflow-tooltip />
        <el-table-column prop="operatorName" label="养护人" width="80" />
        <el-table-column prop="maintenanceTime" label="养护时间" width="160" />
        <el-table-column prop="nextMaintenance" label="下次养护" width="110" />
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="handleSearch"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增对话框 -->
    <el-dialog v-model="dialogVisible" title="新增养护记录" width="650px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="药品" prop="drugId">
          <el-select v-model="formData.drugId" placeholder="请选择药品" filterable style="width: 100%"
            @change="handleDrugChange">
            <el-option v-for="d in drugList" :key="d.id"
              :label="`${d.genericName || d.tradeName} - ${d.specification || ''}`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="批号">
              <el-input v-model="formData.batchNo" placeholder="批号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="重点品种">
              <el-switch v-model="formData.isKeyDrug" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="养护类型" prop="maintenanceType">
          <el-select v-model="formData.maintenanceType" placeholder="请选择" style="width: 100%">
            <el-option label="日常养护" value="DAILY" />
            <el-option label="重点养护" value="KEY" />
            <el-option label="特殊养护" value="SPECIAL" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="外观检查" prop="appearanceCheck">
              <el-radio-group v-model="formData.appearanceCheck">
                <el-radio value="NORMAL">正常</el-radio>
                <el-radio value="ABNORMAL">异常</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="包装检查" prop="packageCheck">
              <el-radio-group v-model="formData.packageCheck">
                <el-radio value="NORMAL">正常</el-radio>
                <el-radio value="ABNORMAL">异常</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="效期检查" prop="expireCheck">
              <el-radio-group v-model="formData.expireCheck">
                <el-radio value="NORMAL">正常</el-radio>
                <el-radio value="ABNORMAL">异常</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="储存条件" prop="storageCheck">
              <el-radio-group v-model="formData.storageCheck">
                <el-radio value="NORMAL">正常</el-radio>
                <el-radio value="ABNORMAL">异常</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="综合结果" prop="overallResult">
          <el-radio-group v-model="formData.overallResult">
            <el-radio value="NORMAL">正常</el-radio>
            <el-radio value="ABNORMAL">异常</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="formData.overallResult === 'ABNORMAL'" label="异常描述">
          <el-input v-model="formData.abnormalDesc" type="textarea" :rows="2" placeholder="描述异常情况" />
        </el-form-item>
        <el-form-item label="处理措施">
          <el-input v-model="formData.treatment" type="textarea" :rows="2" placeholder="如有异常，请输入处理措施" />
        </el-form-item>
        <el-form-item label="下次养护日期" prop="nextMaintenance">
          <el-date-picker v-model="formData.nextMaintenance" type="date" placeholder="选择日期"
            value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Printer } from '@element-plus/icons-vue'
import { getMaintenances, createMaintenance } from '@/api/gsp'
import { getDrugList } from '@/api/drug'

const tableData = ref([])
const loading = ref(false)
const submitting = ref(false)
const drugList = ref([])

const queryForm = reactive({ drugName: '', maintenanceType: '', isKeyDrug: null })
const pagination = reactive({ current: 1, size: 20, total: 0 })

const dialogVisible = ref(false)
const formRef = ref(null)
const formData = reactive({
  drugId: null, drugName: '', batchNo: '', isKeyDrug: false,
  maintenanceType: 'DAILY',
  appearanceCheck: 'NORMAL', packageCheck: 'NORMAL', expireCheck: 'NORMAL', storageCheck: 'NORMAL',
  overallResult: 'NORMAL', abnormalDesc: '', treatment: '', nextMaintenance: ''
})

const formRules = {
  drugId: [{ required: true, message: '请选择药品', trigger: 'change' }],
  maintenanceType: [{ required: true, message: '请选择养护类型', trigger: 'change' }],
  appearanceCheck: [{ required: true, message: '请选择', trigger: 'change' }],
  packageCheck: [{ required: true, message: '请选择', trigger: 'change' }],
  expireCheck: [{ required: true, message: '请选择', trigger: 'change' }],
  storageCheck: [{ required: true, message: '请选择', trigger: 'change' }],
  overallResult: [{ required: true, message: '请选择综合结果', trigger: 'change' }],
  nextMaintenance: [{ required: true, message: '请选择下次养护日期', trigger: 'change' }]
}

const maintenanceTypeMap = {
  DAILY: { label: '日常养护', type: '' },
  KEY: { label: '重点养护', type: 'warning' },
  SPECIAL: { label: '特殊养护', type: 'danger' }
}
const getMaintenanceTypeLabel = (type) => maintenanceTypeMap[type]?.label || type
const getMaintenanceTypeTag = (type) => maintenanceTypeMap[type]?.type || ''

const handleSearch = async () => {
  try {
    loading.value = true
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.size,
      drugName: queryForm.drugName || undefined,
      maintenanceType: queryForm.maintenanceType || undefined,
      isKeyDrug: queryForm.isKeyDrug != null ? queryForm.isKeyDrug : undefined
    }
    const res = await getMaintenances(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryForm.drugName = ''; queryForm.maintenanceType = ''; queryForm.isKeyDrug = null
  pagination.current = 1; handleSearch()
}

const loadDrugList = async () => {
  try {
    const res = await getDrugList()
    drugList.value = res.data
  } catch {}
}

const handleDrugChange = (drugId) => {
  const drug = drugList.value.find(d => d.id === drugId)
  if (drug) {
    formData.drugName = drug.genericName || drug.tradeName
  }
}

const handleAdd = () => {
  Object.assign(formData, {
    drugId: null, drugName: '', batchNo: '', isKeyDrug: false,
    maintenanceType: 'DAILY',
    appearanceCheck: 'NORMAL', packageCheck: 'NORMAL', expireCheck: 'NORMAL', storageCheck: 'NORMAL',
    overallResult: 'NORMAL', abnormalDesc: '', treatment: '', nextMaintenance: ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    const data = { ...formData, operatorId: user.id }
    const res = await createMaintenance(data)
    if (res.code === 200) {
      ElMessage.success('新增成功')
      dialogVisible.value = false
      handleSearch()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const handlePrint = () => { window.print() }

onMounted(() => { handleSearch(); loadDrugList() })
</script>

<style scoped lang="scss">
.maintenance-list-container {
  padding: 16px;
  .toolbar { display: flex; gap: 10px; margin-bottom: 16px; }
  .search-form { margin-bottom: 16px; }
}
@media print {
  .toolbar, .search-form, .el-pagination { display: none !important; }
}
</style>
