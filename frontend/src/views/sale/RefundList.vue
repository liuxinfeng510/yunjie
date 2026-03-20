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
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>整单退货</el-button>
        <el-button type="warning" @click="handlePartialAdd"><el-icon><Select /></el-icon>单品退货</el-button>
      </div>

      <el-table ref="tableRef" v-loading="loading" :data="tableData" stripe border style="width:100%" highlight-current-row>
        <el-table-column prop="refundNo" label="退款单号" width="180" />
        <el-table-column prop="saleOrderId" label="原订单ID" width="120" />
        <el-table-column label="退货类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.refundType === 'partial' ? 'warning' : 'primary'" size="small">
              {{ row.refundType === 'partial' ? '单品退' : '整单退' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="退款金额" width="130" align="right">
          <template #default="{ row }">¥{{ row.refundAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="退款原因" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status] || 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="170" />
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
    <el-dialog v-model="detailVisible" title="退款单详情" width="700px">
      <el-descriptions v-if="currentRefund" :column="2" border>
        <el-descriptions-item label="退款单号">{{ currentRefund.refundNo }}</el-descriptions-item>
        <el-descriptions-item label="原订单ID">{{ currentRefund.saleOrderId }}</el-descriptions-item>
        <el-descriptions-item label="退货类型">
          <el-tag :type="currentRefund.refundType === 'partial' ? 'warning' : 'primary'" size="small">
            {{ currentRefund.refundType === 'partial' ? '单品退货' : '整单退货' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="退款金额">¥{{ currentRefund.refundAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusMap[currentRefund.status]">{{ currentRefund.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRefund.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="退款原因" :span="2">{{ currentRefund.reason }}</el-descriptions-item>
      </el-descriptions>
      <!-- 退货明细（单品退货时显示） -->
      <div v-if="currentRefund?.refundType === 'partial' && refundDetails.length > 0" style="margin-top: 16px;">
        <div style="font-weight: 600; margin-bottom: 8px;">退货明细</div>
        <el-table :data="refundDetails" size="small" border>
          <el-table-column prop="drugName" label="商品名称" />
          <el-table-column prop="specification" label="规格" width="100" />
          <el-table-column prop="quantity" label="数量" width="80" align="center" />
          <el-table-column prop="unitPrice" label="单价" width="100" align="right">
            <template #default="{ row }">¥{{ row.unitPrice?.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="refundAmount" label="退款金额" width="100" align="right">
            <template #default="{ row }">¥{{ row.refundAmount?.toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 整单退货申请 -->
    <el-dialog v-model="addVisible" title="整单退货申请" width="500px" @close="handleAddClose">
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

    <!-- 单品退货申请 -->
    <el-dialog v-model="partialAddVisible" title="单品退货申请" width="800px" @close="handlePartialAddClose">
      <el-form ref="partialFormRef" :model="partialFormData" :rules="partialFormRules" label-width="100px">
        <el-form-item label="原订单ID" prop="saleOrderId">
          <el-input v-model="partialFormData.saleOrderId" placeholder="请输入原销售订单ID" style="width: 200px;">
            <template #append>
              <el-button @click="loadOrderDetails" :loading="orderLoading">查询明细</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="退款原因" prop="reason">
          <el-input v-model="partialFormData.reason" type="textarea" :rows="2" placeholder="请输入退款原因" />
        </el-form-item>
      </el-form>
      
      <div v-if="orderDetails.length > 0" style="margin-top: 16px;">
        <div style="font-weight: 600; margin-bottom: 8px;">选择退货商品</div>
        <el-table :data="orderDetails" size="small" border @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" />
          <el-table-column prop="drugName" label="商品名称" />
          <el-table-column prop="specification" label="规格" width="100" />
          <el-table-column prop="batchNo" label="批号" width="100" />
          <el-table-column label="原数量" width="80" align="center">
            <template #default="{ row }">{{ row.quantity }}</template>
          </el-table-column>
          <el-table-column label="退货数量" width="120">
            <template #default="{ row }">
              <el-input-number 
                v-model="row.refundQty" 
                :min="0" 
                :max="Number(row.quantity)" 
                size="small"
                :disabled="!selectedItems.includes(row)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="unitPrice" label="单价" width="100" align="right">
            <template #default="{ row }">¥{{ row.unitPrice?.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="退款金额" width="100" align="right">
            <template #default="{ row }">
              ¥{{ ((row.refundQty || 0) * (row.unitPrice || 0)).toFixed(2) }}
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top: 12px; text-align: right; font-size: 16px;">
          退款总额：<span style="color: #ff6b00; font-weight: 600;">¥{{ partialTotal.toFixed(2) }}</span>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="partialAddVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handlePartialSubmit" :disabled="selectedItems.length === 0">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Select } from '@element-plus/icons-vue'
import {
  getRefundPage,
  getRefund,
  getRefundDetails,
  getOrderDetailsForRefund,
  createRefund,
  createPartialRefund,
  approveRefund,
  rejectRefund
} from '@/api/sale'
import { useTableKeyboardNav } from '@/composables/useTableKeyboardNav'

const statusMap = { '待审核': 'warning', '已退款': 'success', '已拒绝': 'danger' }

const searchForm = reactive({ status: '' })
const loading = ref(false)
const tableData = ref([])
const { tableRef, selectFirstRow } = useTableKeyboardNav(tableData)
const pagination = reactive({ current: 1, size: 10, total: 0 })

const detailVisible = ref(false)
const currentRefund = ref(null)
const refundDetails = ref([])

const addVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const formData = reactive({ saleOrderId: '', refundAmount: null, reason: '' })
const formRules = {
  saleOrderId: [{ required: true, message: '请输入原订单ID', trigger: 'blur' }],
  refundAmount: [{ required: true, message: '请输入退款金额', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入退款原因', trigger: 'blur' }]
}

// 单品退货相关
const partialAddVisible = ref(false)
const partialFormRef = ref(null)
const partialFormData = reactive({ saleOrderId: '', reason: '' })
const partialFormRules = {
  saleOrderId: [{ required: true, message: '请输入原订单ID', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入退款原因', trigger: 'blur' }]
}
const orderLoading = ref(false)
const orderDetails = ref([])
const selectedItems = ref([])

const partialTotal = computed(() => {
  return selectedItems.value.reduce((sum, item) => {
    return sum + (item.refundQty || 0) * (item.unitPrice || 0)
  }, 0)
})

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
      selectFirstRow()
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

const handlePartialAdd = () => { partialAddVisible.value = true }
const handlePartialAddClose = () => {
  partialFormData.saleOrderId = ''; partialFormData.reason = ''
  orderDetails.value = []
  selectedItems.value = []
  partialFormRef.value?.clearValidate()
}

const handleView = async (row) => {
  currentRefund.value = row
  detailVisible.value = true
  // 如果是单品退货，加载退货明细
  if (row.refundType === 'partial') {
    try {
      const res = await getRefundDetails(row.id)
      if (res.code === 200) {
        refundDetails.value = res.data || []
      }
    } catch { refundDetails.value = [] }
  } else {
    refundDetails.value = []
  }
}

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

// 加载订单明细
const loadOrderDetails = async () => {
  if (!partialFormData.saleOrderId) {
    ElMessage.warning('请输入原订单ID')
    return
  }
  orderLoading.value = true
  try {
    const res = await getOrderDetailsForRefund(partialFormData.saleOrderId)
    if (res.code === 200) {
      orderDetails.value = (res.data || []).map(item => ({
        ...item,
        refundQty: item.quantity
      }))
      if (orderDetails.value.length === 0) {
        ElMessage.warning('未找到订单明细')
      }
    }
  } catch { ElMessage.error('查询失败') } finally { orderLoading.value = false }
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedItems.value = selection
}

// 提交单品退货
const handlePartialSubmit = async () => {
  await partialFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (selectedItems.value.length === 0) {
      ElMessage.warning('请选择要退货的商品')
      return
    }
    
    // 检查退货数量
    for (const item of selectedItems.value) {
      if (!item.refundQty || item.refundQty <= 0) {
        ElMessage.warning(`请输入 ${item.drugName} 的退货数量`)
        return
      }
    }
    
    submitLoading.value = true
    try {
      const res = await createPartialRefund({
        saleOrderId: partialFormData.saleOrderId,
        reason: partialFormData.reason,
        items: selectedItems.value.map(item => ({
          saleOrderDetailId: item.id,
          drugId: item.drugId,
          drugName: item.drugName,
          specification: item.specification,
          batchId: item.batchId,
          batchNo: item.batchNo,
          quantity: item.refundQty,
          unit: item.unit,
          unitPrice: item.unitPrice
        }))
      })
      if (res.code === 200) {
        ElMessage.success('单品退货申请已提交')
        partialAddVisible.value = false
        fetchData()
      }
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
