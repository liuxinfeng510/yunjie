<template>
  <div class="refund-list-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable @change="handleSearch">
            <el-option label="待审核" value="待审核" />
            <el-option label="已退款" value="已退款" />
            <el-option label="已拒绝" value="已拒绝" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>查询</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>申请退货</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" stripe border style="width:100%">
        <el-table-column prop="refundNo" label="退款单号" width="180" />
        <el-table-column prop="saleOrderId" label="原订单ID" width="120" />
        <el-table-column label="退款金额" width="130" align="right">
          <template #default="{ row }">¥{{ row.refundAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="退款原因" show-overflow-tooltip />
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status] || 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="170" />
        <el-table-column prop="approvedAt" label="审核时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === '待审核'" link type="success" @click="handleApprove(row)">通过</el-button>
            <el-button v-if="row.status === '待审核'" link type="danger" @click="handleReject(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="退款单详情" width="600px">
      <el-descriptions v-if="currentRefund" :column="2" border>
        <el-descriptions-item label="退款单号">{{ currentRefund.refundNo }}</el-descriptions-item>
        <el-descriptions-item label="原订单ID">{{ currentRefund.saleOrderId }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">¥{{ currentRefund.refundAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusMap[currentRefund.status]">{{ currentRefund.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="退款原因" :span="2">{{ currentRefund.reason }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRefund.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ currentRefund.approvedAt || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增退货申请 -->
    <el-dialog v-model="addVisible" title="申请退货" width="500px" @close="handleAddClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="原订单ID" prop="saleOrderId">
          <el-input v-model="formData.saleOrderId" placeholder="请输入原销售订单ID" />
        </el-form-item>
        <el-form-item label="退款金额" prop="refundAmount">
          <el-input-number v-model="formData.refundAmount" :min="0.01" :precision="2" style="width:100%;" />
        </el-form-item>
        <el-form-item label="退款原因" prop="reason">
          <el-input v-model="formData.reason" type="textarea" :rows="3" placeholder="请输入退款原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getRefundPage,
  getRefund,
  createRefund,
  approveRefund,
  rejectRefund
} from '@/api/sale'

const statusMap = { '待审核': 'warning', '已退款': 'success', '已拒绝': 'danger' }

const searchForm = reactive({ status: '' })
const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ current: 1, size: 10, total: 0 })

const detailVisible = ref(false)
const currentRefund = ref(null)

const addVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const formData = reactive({ saleOrderId: '', refundAmount: null, reason: '' })
const formRules = {
  saleOrderId: [{ required: true, message: '请输入原订单ID', trigger: 'blur' }],
  refundAmount: [{ required: true, message: '请输入退款金额', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入退款原因', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getRefundPage({
      pageNum: pagination.current,
      pageSize: pagination.size,
      status: searchForm.status || undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch { ElMessage.error('获取数据失败') } finally { loading.value = false }
}

const handleSearch = () => { pagination.current = 1; fetchData() }
const handleReset = () => { searchForm.status = ''; handleSearch() }

const handleAdd = () => { addVisible.value = true }
const handleAddClose = () => {
  formData.saleOrderId = ''; formData.refundAmount = null; formData.reason = ''
  formRef.value?.clearValidate()
}

const handleView = (row) => { currentRefund.value = row; detailVisible.value = true }

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确认同意退款？退货将入库。', '提示', { type: 'warning' })
    const res = await approveRefund(row.id, { approvedBy: 1 })
    if (res.code === 200) { ElMessage.success('退款成功'); fetchData() }
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

const handleReject = async (row) => {
  try {
    await ElMessageBox.confirm('确认拒绝该退款申请？', '提示', { type: 'warning' })
    const res = await rejectRefund(row.id, { approvedBy: 1 })
    if (res.code === 200) { ElMessage.success('已拒绝'); fetchData() }
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const res = await createRefund(formData)
      if (res.code === 200) { ElMessage.success('退货申请已提交'); addVisible.value = false; fetchData() }
    } catch { ElMessage.error('提交失败') } finally { submitLoading.value = false }
  })
}

onMounted(() => { fetchData() })
</script>

<style scoped lang="scss">
.refund-list-container {
  padding: 20px;
  .search-card { margin-bottom: 20px; }
  .table-card {
    .table-header { margin-bottom: 16px; }
    .pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
  }
}
</style>
