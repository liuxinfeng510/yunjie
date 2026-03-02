<template>
  <div class="stock-in-list-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="入库单号">
          <el-input
            v-model="searchForm.stockInNo"
            placeholder="请输入入库单号"
            clearable
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            @clear="handleSearch"
          >
            <el-option label="待审核" value="待审核" />
            <el-option label="已审核" value="已审核" />
            <el-option label="已入库" value="已入库" />
            <el-option label="已驳回" value="已驳回" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增入库单
        </el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="stockInNo" label="入库单号" width="180" />
        <el-table-column label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="supplierName" label="供应商" width="200" />
        <el-table-column label="金额" width="150" align="right">
          <template #default="{ row }">
            ¥{{ row.totalAmount?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建人" width="100" />
        <el-table-column prop="createdTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">
              查看
            </el-button>
            <el-button
              v-if="row.status === '待审核'"
              link
              type="success"
              @click="handleApprove(row)"
            >
              审核
            </el-button>
            <el-button
              v-if="row.status === '已审核'"
              link
              type="warning"
              @click="handleStockIn(row)"
            >
              入库
            </el-button>
            <el-button
              v-if="row.status === '待审核'"
              link
              type="danger"
              @click="handleReject(row)"
            >
              驳回
            </el-button>
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
    <el-dialog
      v-model="detailDialogVisible"
      title="入库单详情"
      width="1000px"
      @close="handleDetailDialogClose"
    >
      <div v-if="currentStockIn" class="stock-in-detail">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="入库单号">
            {{ currentStockIn.stockInNo }}
          </el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="getTypeTagType(currentStockIn.type)">
              {{ currentStockIn.type }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentStockIn.status)">
              {{ currentStockIn.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="供应商">
            {{ currentStockIn.supplierName }}
          </el-descriptions-item>
          <el-descriptions-item label="联系人">
            {{ currentStockIn.contactPerson || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ currentStockIn.contactPhone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建人">
            {{ currentStockIn.creatorName }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ currentStockIn.createdTime }}
          </el-descriptions-item>
          <el-descriptions-item label="总金额">
            ¥{{ currentStockIn.totalAmount?.toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentStockIn.approverName" label="审核人">
            {{ currentStockIn.approverName }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentStockIn.approveTime" label="审核时间">
            {{ currentStockIn.approveTime }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentStockIn.stockInTime" label="入库时间">
            {{ currentStockIn.stockInTime }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentStockIn.remarks" label="备注" :span="3">
            {{ currentStockIn.remarks }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">
          <span style="font-weight: 600; font-size: 16px">入库明细</span>
        </el-divider>

        <el-table
          :data="currentStockIn.items"
          stripe
          border
          style="width: 100%"
          show-summary
          :summary-method="getSummaries"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="drugName" label="药品名称" width="200" />
          <el-table-column prop="specification" label="规格" width="150" />
          <el-table-column prop="batchNo" label="批号" width="150" />
          <el-table-column label="数量" width="120" align="right">
            <template #default="{ row }">
              {{ row.quantity }}
            </template>
          </el-table-column>
          <el-table-column prop="unit" label="单位" width="80" align="center" />
          <el-table-column label="单价" width="120" align="right">
            <template #default="{ row }">
              ¥{{ row.unitPrice?.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="金额" width="120" align="right">
            <template #default="{ row }">
              ¥{{ (row.quantity * row.unitPrice).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="productionDate" label="生产日期" width="120" />
          <el-table-column prop="expiryDate" label="有效期至" width="120" />
        </el-table>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增入库单对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="新增入库单"
      width="800px"
      @close="handleAddDialogClose"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="入库类型" prop="type">
              <el-select v-model="formData.type" placeholder="请选择类型">
                <el-option label="采购入库" value="采购入库" />
                <el-option label="退货入库" value="退货入库" />
                <el-option label="调拨入库" value="调拨入库" />
                <el-option label="其他入库" value="其他入库" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierName">
              <el-input v-model="formData.supplierName" placeholder="请输入供应商" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input v-model="formData.contactPerson" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="formData.remarks"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getStockInPage,
  getStockIn,
  createStockIn,
  approveStockIn,
  completeStockIn
} from '@/api/inventory'

// 搜索表单
const searchForm = reactive({
  stockInNo: '',
  status: '',
  dateRange: null
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 详情对话框
const detailDialogVisible = ref(false)
const currentStockIn = ref(null)

// 新增对话框
const addDialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)

const formData = reactive({
  type: '',
  supplierName: '',
  contactPerson: '',
  contactPhone: '',
  remarks: ''
})

const formRules = {
  type: [{ required: true, message: '请选择入库类型', trigger: 'change' }],
  supplierName: [{ required: true, message: '请输入供应商', trigger: 'blur' }]
}

// 获取类型标签类型
const getTypeTagType = (type) => {
  const typeMap = {
    采购入库: 'primary',
    退货入库: 'warning',
    调拨入库: 'info',
    其他入库: 'default'
  }
  return typeMap[type] || 'default'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    待审核: 'warning',
    已审核: 'primary',
    已入库: 'success',
    已驳回: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取列表数据
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      stockInNo: searchForm.stockInNo,
      status: searchForm.status
    }
    
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }

    const res = await getStockInPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

// 重置
const handleReset = () => {
  searchForm.stockInNo = ''
  searchForm.status = ''
  searchForm.dateRange = null
  handleSearch()
}

// 新增
const handleAdd = () => {
  addDialogVisible.value = true
}

// 查看详情
const handleViewDetail = async (row) => {
  try {
    const res = await getStockIn(row.id)
    if (res.code === 200) {
      currentStockIn.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

// 审核
const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确认审核通过该入库单吗？', '提示', {
      type: 'warning'
    })
    const res = await approveStockIn(row.id)
    if (res.code === 200) {
      ElMessage.success('审核成功')
      fetchData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审核失败')
    }
  }
}

// 入库
const handleStockIn = async (row) => {
  try {
    await ElMessageBox.confirm('确认完成入库吗？', '提示', {
      type: 'warning'
    })
    const res = await completeStockIn(row.id)
    if (res.code === 200) {
      ElMessage.success('入库成功')
      fetchData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('入库失败')
    }
  }
}

// 驳回
const handleReject = async (row) => {
  try {
    await ElMessageBox.prompt('请输入驳回原因', '驳回', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入驳回原因'
    })
    ElMessage.success('已驳回')
    fetchData()
  } catch (error) {
    // 用户取消
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      const res = await createStockIn(formData)
      if (res.code === 200) {
        ElMessage.success('新增成功')
        addDialogVisible.value = false
        fetchData()
      }
    } catch (error) {
      ElMessage.error('新增失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 合计行计算
const getSummaries = (param) => {
  const { columns, data } = param
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (index === 4) {
      // 数量列
      const values = data.map(item => Number(item.quantity))
      if (!values.every(value => isNaN(value))) {
        sums[index] = values.reduce((prev, curr) => {
          const value = Number(curr)
          if (!isNaN(value)) {
            return prev + curr
          } else {
            return prev
          }
        }, 0)
      } else {
        sums[index] = '-'
      }
    } else if (index === 7) {
      // 金额列
      const values = data.map(item => Number(item.quantity) * Number(item.unitPrice))
      if (!values.every(value => isNaN(value))) {
        sums[index] = '¥' + values.reduce((prev, curr) => {
          const value = Number(curr)
          if (!isNaN(value)) {
            return prev + curr
          } else {
            return prev
          }
        }, 0).toFixed(2)
      } else {
        sums[index] = '-'
      }
    } else {
      sums[index] = ''
    }
  })
  return sums
}

// 关闭详情对话框
const handleDetailDialogClose = () => {
  currentStockIn.value = null
}

// 关闭新增对话框
const handleAddDialogClose = () => {
  Object.assign(formData, {
    type: '',
    supplierName: '',
    contactPerson: '',
    contactPhone: '',
    remarks: ''
  })
  formRef.value?.clearValidate()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.stock-in-list-container {
  padding: 20px;

  .search-card {
    margin-bottom: 20px;
  }

  .table-card {
    .table-header {
      margin-bottom: 16px;
    }

    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .stock-in-detail {
    .el-divider {
      margin: 20px 0;
    }
  }
}
</style>
