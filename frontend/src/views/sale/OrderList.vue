<template>
  <div class="order-list-container">
    <el-card shadow="never">
      <!-- 搜索区 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="会员姓名">
          <el-input v-model="searchForm.memberName" placeholder="请输入会员姓名" clearable />
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="searchForm.paymentMethod" placeholder="请选择" clearable>
            <el-option label="现金" value="CASH" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="医保" value="MEDICAL_INSURANCE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        border
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
            <el-tag :type="getPaymentMethodType(row.paymentMethod)">
              {{ getPaymentMethodLabel(row.paymentMethod) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cashierName" label="收银员" width="100" />
        <el-table-column prop="createTime" label="时间" width="160" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
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

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="订单详情"
      width="800px"
      destroy-on-close
    >
      <div v-if="currentOrder" class="order-detail">
        <!-- 订单信息 -->
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="会员">{{ currentOrder.memberName || '非会员' }}</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ currentOrder.totalAmount?.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="优惠金额">¥{{ currentOrder.discountAmount?.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">
            <span style="color: #ff6b00; font-weight: 600">¥{{ currentOrder.payAmount?.toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="支付方式">
            <el-tag :type="getPaymentMethodType(currentOrder.paymentMethod)">
              {{ getPaymentMethodLabel(currentOrder.paymentMethod) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="收银员">{{ currentOrder.cashierName }}</el-descriptions-item>
          <el-descriptions-item label="时间">{{ currentOrder.createTime }}</el-descriptions-item>
        </el-descriptions>

        <!-- 订单明细 -->
        <div class="order-items">
          <h3 style="margin: 20px 0 10px 0">订单明细</h3>
          <el-table :data="currentOrder.items" border>
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="drugName" label="药品名称" min-width="150" />
            <el-table-column prop="specification" label="规格" width="120" />
            <el-table-column prop="quantity" label="数量" width="80" align="right" />
            <el-table-column prop="unitPrice" label="单价" width="100" align="right">
              <template #default="{ row }">
                ¥{{ row.unitPrice?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="discount" label="折扣" width="100" align="right">
              <template #default="{ row }">
                ¥{{ row.discount?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" width="100" align="right">
              <template #default="{ row }">
                ¥{{ row.amount?.toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="handlePrint">打印小票</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSaleOrderPage, getSaleOrder } from '@/api/sale'

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  memberName: '',
  paymentMethod: ''
})

const dateRange = ref([])

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 详情对话框
const detailVisible = ref(false)
const currentOrder = ref(null)

// 支付方式映射
const paymentMethodMap = {
  CASH: { label: '现金', type: '' },
  WECHAT: { label: '微信', type: 'success' },
  ALIPAY: { label: '支付宝', type: 'warning' },
  MEDICAL_INSURANCE: { label: '医保', type: 'info' }
}

const getPaymentMethodLabel = (method) => {
  return paymentMethodMap[method]?.label || method
}

const getPaymentMethodType = (method) => {
  return paymentMethodMap[method]?.type || ''
}

// 查询订单列表
const handleSearch = async () => {
  try {
    loading.value = true
    const params = {
      ...searchForm,
      current: pagination.current,
      size: pagination.size
    }

    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    const res = await getSaleOrderPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('查询失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 重置搜索
const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.memberName = ''
  searchForm.paymentMethod = ''
  dateRange.value = []
  pagination.current = 1
  handleSearch()
}

// 查看详情
const showDetail = async (row) => {
  try {
    const res = await getSaleOrder(row.id)
    if (res.code === 200) {
      currentOrder.value = res.data
      detailVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取详情失败：' + error.message)
  }
}

// 打印小票
const handlePrint = () => {
  ElMessage.success('打印功能开发中...')
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped lang="scss">
.order-list-container {
  padding: 16px;

  .search-form {
    margin-bottom: 16px;
  }

  .order-detail {
    .order-items {
      margin-top: 20px;
    }
  }
}
</style>
