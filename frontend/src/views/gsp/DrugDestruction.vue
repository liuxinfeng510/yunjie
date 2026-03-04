<template>
  <div class="drug-destruction">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>药品销毁管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>申请销毁
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="申请编号">
          <el-input v-model="queryForm.applicationNo" placeholder="请输入申请编号" clearable />
        </el-form-item>
        <el-form-item label="审批状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="待审批" value="pending" />
            <el-option label="已批准" value="approved" />
            <el-option label="待执行" value="waiting" />
            <el-option label="已执行" value="executed" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="applicationNo" label="申请编号" width="140" />
        <el-table-column prop="drugName" label="商品名称" min-width="150" />
        <el-table-column prop="batchNo" label="批号" width="120" />
        <el-table-column prop="quantity" label="销毁数量" width="100" />
        <el-table-column prop="reason" label="销毁原因" width="100">
          <template #default="{ row }">{{ getReasonName(row.reason) }}</template>
        </el-table-column>
        <el-table-column prop="applicant" label="申请人" width="90" />
        <el-table-column prop="applicationDate" label="申请日期" width="110" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'pending'" type="success" link @click="handleApprove(row)">批准</el-button>
            <el-button v-if="row.status === 'pending'" type="danger" link @click="handleReject(row)">拒绝</el-button>
            <el-button v-if="row.status === 'approved'" type="warning" link @click="handleExecute(row)">执行销毁</el-button>
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

    <!-- 申请对话框 -->
    <el-dialog v-model="dialogVisible" title="申请药品销毁" width="650px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="药品" prop="drugId">
          <el-select v-model="form.drugId" placeholder="请选择药品" filterable style="width: 100%;">
            <el-option v-for="d in drugList" :key="d.id" :label="`${d.name} - ${d.specification}`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="批号" prop="batchNo"><el-input v-model="form.batchNo" placeholder="请输入批号" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销毁数量" prop="quantity"><el-input-number v-model="form.quantity" :min="1" style="width: 100%;" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="销毁原因" prop="reason">
          <el-select v-model="form.reason" placeholder="请选择" style="width: 100%;">
            <el-option label="过期" value="expired" />
            <el-option label="变质" value="deteriorated" />
            <el-option label="破损" value="damaged" />
            <el-option label="召回" value="recalled" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="销毁方式" prop="method">
          <el-select v-model="form.method" placeholder="请选择" style="width: 100%;">
            <el-option label="焚烧" value="incineration" />
            <el-option label="化学处理" value="chemical" />
            <el-option label="填埋" value="landfill" />
            <el-option label="委托处理" value="outsource" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请详细说明销毁原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getDestructionPage, createDestructionApplication, approveDestruction, rejectDestruction, executeDestruction } from '@/api/gsp'
import { getDrugList } from '@/api/drug'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const drugList = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)

const queryForm = reactive({ applicationNo: '', status: '' })
const pagination = reactive({ current: 1, size: 20, total: 0 })
const form = reactive({ drugId: null, batchNo: '', quantity: 1, reason: '', method: '', description: '' })

const rules = {
  drugId: [{ required: true, message: '请选择药品', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  reason: [{ required: true, message: '请选择销毁原因', trigger: 'change' }],
  method: [{ required: true, message: '请选择销毁方式', trigger: 'change' }]
}

const reasonMap = { expired: '过期', deteriorated: '变质', damaged: '破损', recalled: '召回', other: '其他' }
const statusMap = { pending: '待审批', approved: '已批准', waiting: '待执行', executed: '已执行', rejected: '已拒绝' }
const statusColorMap = { pending: 'warning', approved: 'success', waiting: 'primary', executed: 'info', rejected: 'danger' }

const getReasonName = (r) => reasonMap[r] || r
const getStatusName = (s) => statusMap[s] || s
const getStatusColor = (s) => statusColorMap[s] || 'info'

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDestructionPage({ current: pagination.current, size: pagination.size, ...queryForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) { ElMessage.error('加载数据失败') }
  finally { loading.value = false }
}

const loadDrugList = async () => { try { const res = await getDrugList(); drugList.value = res.data } catch (e) { console.error(e) } }
const resetQuery = () => { queryForm.applicationNo = ''; queryForm.status = ''; pagination.current = 1; loadData() }

const handleAdd = () => {
  Object.assign(form, { drugId: null, batchNo: '', quantity: 1, reason: '', method: '', description: '' })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try { await createDestructionApplication(form); ElMessage.success('申请已提交'); dialogVisible.value = false; loadData() }
  catch (error) { ElMessage.error('操作失败') }
  finally { submitting.value = false }
}

const handleDetail = (row) => ElMessage.info('详情功能开发中')
const handleApprove = async (row) => { await ElMessageBox.confirm('确定批准该销毁申请？', '提示'); await approveDestruction(row.id); ElMessage.success('已批准'); loadData() }
const handleReject = async (row) => { await ElMessageBox.confirm('确定拒绝该销毁申请？', '提示'); await rejectDestruction(row.id); ElMessage.success('已拒绝'); loadData() }
const handleExecute = async (row) => { await ElMessageBox.confirm('确定执行销毁？销毁后无法撤销', '警告', { type: 'warning' }); await executeDestruction(row.id); ElMessage.success('销毁完成'); loadData() }

onMounted(() => { loadData(); loadDrugList() })
</script>

<style scoped>
.drug-destruction { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
