<template>
  <div class="defective-drug">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>不良品管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>登记不良品
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="queryForm.drugName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="不良类型">
          <el-select v-model="queryForm.defectType" placeholder="请选择" clearable>
            <el-option label="破损" value="damaged" />
            <el-option label="变质" value="deteriorated" />
            <el-option label="过期" value="expired" />
            <el-option label="包装问题" value="packaging" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已处理" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="registerNo" label="登记编号" width="130" />
        <el-table-column prop="drugName" label="商品名称" min-width="150" />
        <el-table-column prop="specification" label="规格" width="100" />
        <el-table-column prop="batchNo" label="批号" width="120" />
        <el-table-column prop="defectType" label="不良类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getDefectTypeColor(row.defectType)">{{ getDefectTypeName(row.defectType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="discoveryDate" label="发现日期" width="110" />
        <el-table-column prop="discoverer" label="发现人" width="90" />
        <el-table-column prop="status" label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'pending'" type="warning" link @click="handleProcess(row)">处理</el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="药品" prop="drugId">
          <el-select v-model="form.drugId" placeholder="请选择药品" filterable style="width: 100%;">
            <el-option v-for="d in drugList" :key="d.id" :label="`${d.name} - ${d.specification}`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="批号" prop="batchNo">
              <el-input v-model="form.batchNo" placeholder="请输入批号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="form.quantity" :min="1" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="不良类型" prop="defectType">
          <el-select v-model="form.defectType" placeholder="请选择" style="width: 100%;">
            <el-option label="破损" value="damaged" />
            <el-option label="变质" value="deteriorated" />
            <el-option label="过期" value="expired" />
            <el-option label="包装问题" value="packaging" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="发现日期" prop="discoveryDate">
          <el-date-picker v-model="form.discoveryDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="问题描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请详细描述不良品问题" />
        </el-form-item>
        <el-form-item label="处理建议" prop="suggestion">
          <el-select v-model="form.suggestion" placeholder="请选择" style="width: 100%;">
            <el-option label="退货" value="return" />
            <el-option label="销毁" value="destroy" />
            <el-option label="降级使用" value="downgrade" />
            <el-option label="其他" value="other" />
          </el-select>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getDefectiveDrugPage, registerDefectiveDrug, processDefectiveDrug, completeDefectiveDrug } from '@/api/gsp'
import { getDrugList } from '@/api/drug'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const drugList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('登记不良品')
const formRef = ref(null)

const queryForm = reactive({ drugName: '', defectType: '', status: '' })
const pagination = reactive({ current: 1, size: 20, total: 0 })

const form = reactive({
  drugId: null, batchNo: '', quantity: 1, defectType: '', discoveryDate: '', description: '', suggestion: ''
})

const rules = {
  drugId: [{ required: true, message: '请选择药品', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  defectType: [{ required: true, message: '请选择不良类型', trigger: 'change' }],
  discoveryDate: [{ required: true, message: '请选择发现日期', trigger: 'change' }],
  description: [{ required: true, message: '请描述问题', trigger: 'blur' }]
}

const defectTypeMap = { damaged: '破损', deteriorated: '变质', expired: '过期', packaging: '包装问题', other: '其他' }
const defectTypeColorMap = { damaged: 'warning', deteriorated: 'danger', expired: 'danger', packaging: 'info', other: '' }
const statusMap = { pending: '待处理', processing: '处理中', completed: '已处理' }
const statusColorMap = { pending: 'warning', processing: 'primary', completed: 'success' }

const getDefectTypeName = (t) => defectTypeMap[t] || t
const getDefectTypeColor = (t) => defectTypeColorMap[t] || ''
const getStatusName = (s) => statusMap[s] || s
const getStatusColor = (s) => statusColorMap[s] || 'info'

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDefectiveDrugPage({ current: pagination.current, size: pagination.size, ...queryForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadDrugList = async () => {
  try {
    const res = await getDrugList()
    drugList.value = res.data
  } catch (error) {
    console.error('加载药品列表失败', error)
  }
}

const resetQuery = () => { queryForm.drugName = ''; queryForm.defectType = ''; queryForm.status = ''; pagination.current = 1; loadData() }

const handleAdd = () => {
  Object.assign(form, { drugId: null, batchNo: '', quantity: 1, defectType: '', discoveryDate: '', description: '', suggestion: '' })
  dialogTitle.value = '登记不良品'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await registerDefectiveDrug(form)
    ElMessage.success('登记成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDetail = (row) => { ElMessage.info('详情功能开发中') }
const handleProcess = async (row) => {
  await ElMessageBox.confirm('确定开始处理该不良品？', '提示', { type: 'warning' })
  await processDefectiveDrug(row.id)
  ElMessage.success('已开始处理')
  loadData()
}
const handleComplete = async (row) => {
  await ElMessageBox.confirm('确定该不良品已处理完成？', '提示', { type: 'warning' })
  await completeDefectiveDrug(row.id)
  ElMessage.success('处理完成')
  loadData()
}

onMounted(() => { loadData(); loadDrugList() })
</script>

<style scoped>
.defective-drug { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
