<template>
  <div class="sales-query-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="订单号">
          <el-input
            v-model="searchForm.orderNo"
            placeholder="请输入订单号"
            clearable
            @clear="handleSearch"
            @keydown.enter.prevent="handleSearch"
            @keydown.up.prevent="handleArrowUp"
            @keydown.down.prevent="handleArrowDown"
          />
        </el-form-item>
        <el-form-item label="会员姓名">
          <el-input
            v-model="searchForm.memberName"
            placeholder="请输入会员姓名"
            clearable
            @clear="handleSearch"
            @keydown.enter.prevent="handleSearch"
            @keydown.up.prevent="handleArrowUp"
            @keydown.down.prevent="handleArrowDown"
          />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="searchForm.paymentMethod" placeholder="全部" clearable @change="handleSearch">
            <el-option label="现金" value="CASH" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="医保" value="MEDICAL_INSURANCE" />
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
        <span v-if="summary.totalAmount != null">销售总额：<b style="color:#ff6b00">¥{{ summary.totalAmount?.toFixed(2) }}</b></span>
        <span v-if="summary.payAmount != null">实收总额：<b style="color:#409eff">¥{{ summary.payAmount?.toFixed(2) }}</b></span>
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
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="memberName" label="会员" width="100">
          <template #default="{ row }">
            {{ row.memberName || '非会员' }}
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.totalAmount?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="discountAmount" label="优惠金额" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.discountAmount?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="payAmount" label="实付金额" width="100" align="right">
          <template #default="{ row }">
            <span style="color: #ff6b00; font-weight: 600">¥{{ row.payAmount?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getPaymentType(row.paymentMethod)" size="small">
              {{ getPaymentLabel(row.paymentMethod) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cashierName" label="收银员" width="100" />
        <el-table-column prop="createTime" label="销售时间" width="160" />
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
    <el-dialog v-model="detailVisible" title="销售单详情" width="800px">
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="订单号">{{ currentRow.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="会员">{{ currentRow.memberName || '非会员' }}</el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ currentRow.totalAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="优惠金额">¥{{ currentRow.discountAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="实付金额">¥{{ currentRow.payAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ getPaymentLabel(currentRow.paymentMethod) }}</el-descriptions-item>
        <el-descriptions-item label="收银员">{{ currentRow.cashierName }}</el-descriptions-item>
        <el-descriptions-item label="销售时间">{{ currentRow.createTime }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="detailItems" border stripe style="margin-top: 16px;" v-loading="detailLoading">
        <el-table-column prop="drugName" label="药品名称" min-width="150" />
        <el-table-column prop="specification" label="规格" width="120" />
        <el-table-column prop="quantity" label="数量" width="80" align="center" />
        <el-table-column prop="unitPrice" label="单价" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.unitPrice?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="subtotal" label="小计" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.subtotal?.toFixed(2) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getSaleOrderPage, getSaleOrder } from '@/api/sale'
import { useTableKeyboardNav } from '@/composables/useTableKeyboardNav'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const summary = reactive({ totalAmount: null, payAmount: null })

const { tableRef, handleArrowUp, handleArrowDown, selectFirstRow } = useTableKeyboardNav(tableData)

const searchForm = reactive({
  orderNo: '',
  memberName: '',
  paymentMethod: '',
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
    if (searchForm.orderNo) params.orderNo = searchForm.orderNo
    if (searchForm.memberName) params.memberName = searchForm.memberName
    if (searchForm.paymentMethod) params.paymentMethod = searchForm.paymentMethod
    if (searchForm.dateRange?.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const res = await getSaleOrderPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
      summary.totalAmount = tableData.value.reduce((sum, item) => sum + (item.totalAmount || 0), 0)
      summary.payAmount = tableData.value.reduce((sum, item) => sum + (item.payAmount || 0), 0)
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
  searchForm.orderNo = ''
  searchForm.memberName = ''
  searchForm.paymentMethod = ''
  searchForm.dateRange = null
  handleSearch()
}

const paymentMap = {
  CASH: { label: '现金', type: 'info' },
  WECHAT: { label: '微信', type: 'success' },
  ALIPAY: { label: '支付宝', type: 'primary' },
  MEDICAL_INSURANCE: { label: '医保', type: 'warning' }
}
const getPaymentLabel = (method) => paymentMap[method]?.label || method || '-'
const getPaymentType = (method) => paymentMap[method]?.type || 'info'

const showDetail = async (row) => {
  currentRow.value = row
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getSaleOrder(row.id)
    if (res.code === 200) {
      detailItems.value = res.data.items || res.data.details || []
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
.sales-query-container {
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
