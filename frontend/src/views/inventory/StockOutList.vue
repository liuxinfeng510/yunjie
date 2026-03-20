<template>
  <div class="stock-out-list-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="出库类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable @change="handleSearch">
            <el-option label="销售出库" value="销售出库" />
            <el-option label="调拨出库" value="调拨出库" />
            <el-option label="报损出库" value="报损出库" />
            <el-option label="其他出库" value="其他出库" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable @change="handleSearch">
            <el-option label="待审核" value="待审核" />
            <el-option label="已审核" value="已审核" />
            <el-option label="已出库" value="已出库" />
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
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增出库单</el-button>
      </div>

      <el-table ref="tableRef" v-loading="loading" :data="tableData" stripe border style="width: 100%" highlight-current-row>
        <el-table-column prop="orderNo" label="出库单号" width="180" />
        <el-table-column label="出库类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagMap[row.type] || 'info'">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="总金额" width="150" align="right">
          <template #default="{ row }">¥{{ row.totalAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagMap[row.status] || 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === '待审核'" link type="success" @click="handleApprove(row)">审核</el-button>
            <el-button v-if="row.status === '已审核'" link type="warning" @click="handleComplete(row)">出库</el-button>
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="出库单详情" width="900px">
      <div v-if="currentDetail">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="出库单号">{{ currentDetail.stockOut.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="出库类型">
            <el-tag>{{ currentDetail.stockOut.type }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagMap[currentDetail.stockOut.status]">{{ currentDetail.stockOut.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ currentDetail.stockOut.totalAmount?.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentDetail.stockOut.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left"><span style="font-weight:600;">出库明细</span></el-divider>

        <el-table :data="currentDetail.details" stripe border>
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="drugId" label="药品ID" width="100" />
          <el-table-column prop="batchNo" label="批号" width="150" />
          <el-table-column prop="quantity" label="数量" width="100" align="right" />
          <el-table-column prop="unit" label="单位" width="80" align="center" />
          <el-table-column label="成本价" width="120" align="right">
            <template #default="{ row }">¥{{ row.costPrice?.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="金额" width="120" align="right">
            <template #default="{ row }">¥{{ row.amount?.toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增出库单对话框 -->
    <el-dialog v-model="addVisible" title="新增出库单" width="800px" @close="handleAddClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出库类型" prop="type">
              <el-select v-model="formData.type" placeholder="请选择">
                <el-option label="调拨出库" value="调拨出库" />
                <el-option label="报损出库" value="报损出库" />
                <el-option label="其他出库" value="其他出库" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="formData.type === '调拨出库'" label="目标门店">
              <el-input v-model="formData.targetStoreId" placeholder="目标门店ID" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>

        <el-divider content-position="left">出库明细</el-divider>
        <el-button type="primary" size="small" style="margin-bottom:10px;" @click="addDetailRow">
          <el-icon><Plus /></el-icon>添加药品
        </el-button>
        <el-table :data="formData.details" border size="small">
          <el-table-column type="index" label="#" width="50" />
          <el-table-column label="药品ID" width="120">
            <template #default="{ row }">
              <el-input v-model="row.drugId" size="small" placeholder="药品ID" />
            </template>
          </el-table-column>
          <el-table-column label="批次ID" width="120">
            <template #default="{ row }">
              <el-input v-model="row.batchId" size="small" placeholder="批次ID" />
            </template>
          </el-table-column>
          <el-table-column label="批号" width="140">
            <template #default="{ row }">
              <el-input v-model="row.batchNo" size="small" placeholder="批号" />
            </template>
          </el-table-column>
          <el-table-column label="数量" width="100">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" size="small" :min="1" />
            </template>
          </el-table-column>
          <el-table-column label="单位" width="80">
            <template #default="{ row }">
              <el-input v-model="row.unit" size="small" placeholder="单位" />
            </template>
          </el-table-column>
          <el-table-column label="成本价" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.costPrice" size="small" :min="0" :precision="2" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="60">
            <template #default="{ $index }">
              <el-button link type="danger" size="small" @click="formData.details.splice($index, 1)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getStockOutPage,
  getStockOut,
  createStockOut,
  approveStockOut,
  completeStockOut
} from '@/api/inventory'
import { useTableKeyboardNav } from '@/composables/useTableKeyboardNav'

const typeTagMap = { '销售出库': 'primary', '调拨出库': 'info', '报损出库': 'danger', '其他出库': '' }
const statusTagMap = { '待审核': 'warning', '已审核': 'primary', '已出库': 'success' }

const searchForm = reactive({ type: '', status: '' })
const loading = ref(false)
const tableData = ref([])
const { tableRef, selectFirstRow } = useTableKeyboardNav(tableData)
const pagination = reactive({ current: 1, size: 10, total: 0 })

const detailVisible = ref(false)
const currentDetail = ref(null)

const addVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const formData = reactive({
  type: '',
  targetStoreId: null,
  remark: '',
  details: []
})
const formRules = {
  type: [{ required: true, message: '请选择出库类型', trigger: 'change' }]
}

const addDetailRow = () => {
  formData.details.push({ drugId: '', batchId: '', batchNo: '', quantity: 1, unit: '', costPrice: 0 })
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getStockOutPage({
      pageNum: pagination.current,
      pageSize: pagination.size,
      type: searchForm.type || undefined,
      status: searchForm.status || undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
      selectFirstRow()
    }
  } catch {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.current = 1; fetchData() }
const handleReset = () => { searchForm.type = ''; searchForm.status = ''; handleSearch() }

const handleAdd = () => { addVisible.value = true }
const handleAddClose = () => {
  formData.type = ''; formData.targetStoreId = null; formData.remark = ''; formData.details = []
  formRef.value?.clearValidate()
}

const handleView = async (row) => {
  try {
    const res = await getStockOut(row.id)
    if (res.code === 200) { currentDetail.value = res.data; detailVisible.value = true }
  } catch { ElMessage.error('获取详情失败') }
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确认审核通过该出库单吗？', '提示', { type: 'warning' })
    const res = await approveStockOut(row.id)
    if (res.code === 200) { ElMessage.success('审核成功'); fetchData() }
  } catch (e) { if (e !== 'cancel') ElMessage.error('审核失败') }
}

const handleComplete = async (row) => {
  try {
    await ElMessageBox.confirm('确认完成出库吗？库存将被扣减。', '提示', { type: 'warning' })
    const res = await completeStockOut(row.id)
    if (res.code === 200) { ElMessage.success('出库成功'); fetchData() }
  } catch (e) { if (e !== 'cancel') ElMessage.error('出库失败') }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (formData.details.length === 0) { ElMessage.warning('请添加出库明细'); return }
    submitLoading.value = true
    try {
      const details = formData.details.map(d => ({
        ...d,
        amount: (d.quantity * d.costPrice).toFixed(2)
      }))
      const res = await createStockOut({
        stockOut: { type: formData.type, targetStoreId: formData.targetStoreId, remark: formData.remark },
        details
      })
      if (res.code === 200) { ElMessage.success('创建成功'); addVisible.value = false; fetchData() }
    } catch { ElMessage.error('创建失败') } finally { submitLoading.value = false }
  })
}

onMounted(() => { fetchData() })
</script>

<style scoped lang="scss">
.stock-out-list-container {
  padding: 20px;
  .search-card { margin-bottom: 20px; }
  .table-card {
    .table-header { margin-bottom: 16px; }
    .pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
  }
}
</style>
