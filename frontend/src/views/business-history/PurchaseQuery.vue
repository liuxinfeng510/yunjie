<template>
  <div class="purchase-query-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="入库单号">
          <el-input
            v-model="searchForm.stockInNo"
            placeholder="请输入入库单号"
            clearable
            @clear="handleSearch"
            @keydown.enter.prevent="handleSearch"
            @keydown.up.prevent="handleArrowUp"
            @keydown.down.prevent="handleArrowDown"
          />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input
            v-model="searchForm.supplierName"
            placeholder="请输入供应商名称"
            clearable
            @clear="handleSearch"
            @keydown.enter.prevent="handleSearch"
            @keydown.up.prevent="handleArrowUp"
            @keydown.down.prevent="handleArrowDown"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable @change="handleSearch">
            <el-option label="待审核" value="待审核" />
            <el-option label="已审核" value="已审核" />
            <el-option label="已入库" value="已入库" />
            <el-option label="已驳回" value="已驳回" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
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
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 12px;">
      <div class="summary-bar">
        <span>共 <b>{{ total }}</b> 条记录</span>
        <span v-if="summary.totalAmount != null">总金额：<b style="color:#409eff">¥{{ summary.totalAmount?.toFixed(2) }}</b></span>
      </div>
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        stripe
        border
        highlight-current-row
        style="width: 100%"
      >
        <el-table-column prop="orderNo" label="入库单号" width="180" />
        <el-table-column prop="supplierName" label="供应商" min-width="160" />
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === '采购入库' ? 'primary' : 'warning'" size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.totalAmount?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="itemCount" label="品种数" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作员" width="100" />
        <el-table-column prop="createTime" label="入库时间" width="160" />
        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; justify-content: flex-end;"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="入库单详情" width="800px">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="入库单号">{{ currentRow.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ currentRow.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ currentRow.type }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ currentRow.status }}</el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ currentRow.totalAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="操作员">{{ currentRow.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRow.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentRow.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="detailItems" border stripe style="margin-top: 16px;" v-loading="detailLoading">
        <el-table-column prop="drugName" label="药品名称" min-width="150" />
        <el-table-column prop="specification" label="规格" width="120" />
        <el-table-column prop="manufacturer" label="生产企业" min-width="150" />
        <el-table-column prop="batchNo" label="批号" width="120" />
        <el-table-column prop="quantity" label="数量" width="80" align="center" />
        <el-table-column prop="purchasePrice" label="进价" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.purchasePrice?.toFixed(2) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getStockInPage, getStockIn } from '@/api/inventory'
import { useTableKeyboardNav } from '@/composables/useTableKeyboardNav'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const summary = reactive({ totalAmount: null })

const { tableRef, handleArrowUp, handleArrowDown, selectFirstRow } = useTableKeyboardNav(tableData)

const searchForm = reactive({
  stockInNo: '',
  supplierName: '',
  status: '',
  dateRange: null
})

const detailVisible = ref(false)
const detailLoading = ref(false)
const currentRow = ref(null)
const detailItems = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (searchForm.stockInNo) params.stockInNo = searchForm.stockInNo
    if (searchForm.supplierName) params.supplierName = searchForm.supplierName
    if (searchForm.status) params.status = searchForm.status
    if (searchForm.dateRange?.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const res = await getStockInPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
      // 计算总金额
      summary.totalAmount = tableData.value.reduce((sum, item) => sum + (item.totalAmount || 0), 0)
      selectFirstRow()
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  searchForm.stockInNo = ''
  searchForm.supplierName = ''
  searchForm.status = ''
  searchForm.dateRange = null
  handleSearch()
}

const getStatusType = (status) => {
  const map = { '待审核': 'warning', '已审核': 'primary', '已入库': 'success', '已驳回': 'danger' }
  return map[status] || 'info'
}

const showDetail = async (row) => {
  currentRow.value = row
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getStockIn(row.id)
    if (res.code === 200) {
      detailItems.value = res.data.items || []
    }
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.purchase-query-container {
  padding: 16px;
}
.summary-bar {
  display: flex;
  gap: 24px;
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
}
</style>
