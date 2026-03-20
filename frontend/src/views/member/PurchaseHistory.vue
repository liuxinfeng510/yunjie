<template>
  <div class="purchase-history-container">
    <el-card shadow="never">
      <!-- 返回收银台 -->
      <div v-if="fromPos" class="back-bar">
        <el-button type="primary" plain size="small" @click="router.push({ path: '/sale/pos', query: { memberId: route.query.memberId } })">
          &larr; 返回收银台
        </el-button>
      </div>
      <!-- 搜索区 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="会员">
          <div class="member-search-wrapper">
            <el-input
              v-model="searchKeyword"
              placeholder="姓名/拼音/手机号"
              clearable
              @input="handleMemberSearch"
              @clear="handleClearMember"
              style="width: 200px"
            />
            <div v-if="searchResults.length > 0" class="search-results">
              <div
                v-for="item in searchResults"
                :key="item.id"
                class="search-result-item"
                @click="selectMember(item)"
              >
                <span class="member-name">{{ item.name }}</span>
                <span class="member-phone">{{ item.phone }}</span>
              </div>
            </div>
          </div>
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
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :disabled="!selectedMember">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 选中会员信息 -->
      <div v-if="selectedMember" class="selected-member-info">
        <el-tag size="large" closable @close="handleClearMember">
          {{ selectedMember.name }}（{{ selectedMember.phone }}）
        </el-tag>
        <span v-if="pagination.total > 0" class="summary-text">
          共 <b>{{ pagination.total }}</b> 笔订单，合计 <b style="color: #ff6b00">¥{{ totalPayAmount.toFixed(2) }}</b>
        </span>
      </div>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" style="width: 100%" border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="totalAmount" label="总金额" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.totalAmount?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="discountAmount" label="优惠" width="90" align="right">
          <template #default="{ row }">
            ¥{{ row.discountAmount?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="payAmount" label="实付金额" width="110" align="right">
          <template #default="{ row }">
            <span style="color: #ff6b00; font-weight: 600">¥{{ row.payAmount?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getPayMethodType(row.paymentMethod)" size="small">
              {{ getPayMethodLabel(row.paymentMethod) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cashierName" label="收银员" width="100" />
        <el-table-column prop="createTime" label="时间" min-width="160" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-if="pagination.total > 0"
        v-model:current-page="pagination.current"
        :page-size="50"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="handleSearch"
        style="margin-top: 16px; justify-content: flex-end"
      />

      <!-- 空状态 -->
      <el-empty v-if="!loading && tableData.length === 0 && selectedMember" description="暂无购药记录" />
      <el-empty v-if="!selectedMember" description="请先搜索并选择会员" />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="800px" destroy-on-close>
      <div v-if="currentOrder" class="order-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="会员">{{ currentOrder.memberName || '非会员' }}</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ currentOrder.totalAmount?.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="优惠金额">¥{{ currentOrder.discountAmount?.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">
            <span style="color: #ff6b00; font-weight: 600">¥{{ currentOrder.payAmount?.toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="支付方式">
            <el-tag :type="getPayMethodType(currentOrder.paymentMethod)">
              {{ getPayMethodLabel(currentOrder.paymentMethod) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="收银员">{{ currentOrder.cashierName }}</el-descriptions-item>
          <el-descriptions-item label="时间">{{ currentOrder.createTime }}</el-descriptions-item>
        </el-descriptions>

        <div class="order-items">
          <h3 style="margin: 20px 0 10px 0">购药明细</h3>
          <el-table :data="currentOrder.items" border>
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="drugName" label="商品名称" min-width="150" />
            <el-table-column prop="specification" label="规格" width="120" />
            <el-table-column prop="quantity" label="数量" width="80" align="right" />
            <el-table-column prop="unit" label="单位" width="60" align="center" />
            <el-table-column prop="unitPrice" label="单价" width="100" align="right">
              <template #default="{ row }">
                ¥{{ row.unitPrice?.toFixed(2) }}
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
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { searchMembers } from '@/api/member'
import { getSaleOrderPage, getSaleOrder } from '@/api/sale'

const route = useRoute()
const router = useRouter()
const fromPos = computed(() => route.query.from === 'pos')

// 会员搜索
const searchKeyword = ref('')
const searchResults = ref([])
const selectedMember = ref(null)
let searchTimer = null

// 从POS跳转时自动加载会员
onMounted(() => {
  if (route.query.memberId && route.query.memberName) {
    selectedMember.value = { id: route.query.memberId, name: route.query.memberName }
    searchKeyword.value = route.query.memberName
    handleSearch()
  }
})

const handleMemberSearch = (val) => {
  if (searchTimer) clearTimeout(searchTimer)
  if (!val || val.length < 1) {
    searchResults.value = []
    return
  }
  searchTimer = setTimeout(async () => {
    try {
      const res = await searchMembers(val)
      if (res.code === 200) {
        searchResults.value = res.data || []
      }
    } catch (e) {
      // ignore
    }
  }, 300)
}

const selectMember = (member) => {
  selectedMember.value = member
  searchKeyword.value = member.name
  searchResults.value = []
  handleSearch()
}

const handleClearMember = () => {
  selectedMember.value = null
  searchKeyword.value = ''
  searchResults.value = []
  tableData.value = []
  pagination.total = 0
}

// 日期范围 - 默认近三个月
const getDefaultDateRange = () => {
  const end = new Date()
  const start = new Date()
  start.setMonth(start.getMonth() - 3)
  const fmt = (d) => d.toISOString().slice(0, 10)
  return [fmt(start), fmt(end)]
}
const dateRange = ref(getDefaultDateRange())

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 汇总
const totalPayAmount = computed(() => {
  return tableData.value.reduce((sum, row) => sum + (row.payAmount || 0), 0)
})

// 分页
const pagination = ref({ current: 1, total: 0 })

// 支付方式
const payMethodMap = {
  CASH: { label: '现金', type: '' },
  WECHAT: { label: '微信', type: 'success' },
  ALIPAY: { label: '支付宝', type: 'warning' },
  MEDICAL_INSURANCE: { label: '医保', type: 'info' }
}
const getPayMethodLabel = (m) => payMethodMap[m]?.label || m || '-'
const getPayMethodType = (m) => payMethodMap[m]?.type || ''

// 查询
const handleSearch = async () => {
  if (!selectedMember.value) return
  try {
    loading.value = true
    const params = {
      memberName: selectedMember.value.name,
      current: pagination.value.current,
      size: 50
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await getSaleOrderPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.value.total = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error('查询失败：' + e.message)
  } finally {
    loading.value = false
  }
}

// 重置
const handleReset = () => {
  handleClearMember()
  dateRange.value = getDefaultDateRange()
}

// 订单详情
const detailVisible = ref(false)
const currentOrder = ref(null)

const showDetail = async (row) => {
  try {
    const res = await getSaleOrder(row.id)
    if (res.code === 200) {
      currentOrder.value = res.data
      detailVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取详情失败：' + e.message)
  }
}
</script>

<style scoped lang="scss">
.purchase-history-container {
  padding: 16px;

  .back-bar {
    margin-bottom: 12px;
  }

  .search-form {
    margin-bottom: 16px;
  }

  .member-search-wrapper {
    position: relative;

    .search-results {
      position: absolute;
      top: 100%;
      left: 0;
      right: 0;
      z-index: 999;
      background: #fff;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      max-height: 240px;
      overflow-y: auto;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

      .search-result-item {
        padding: 8px 12px;
        cursor: pointer;
        display: flex;
        justify-content: space-between;
        align-items: center;

        &:hover {
          background: #f5f7fa;
        }

        .member-name {
          font-weight: 500;
        }

        .member-phone {
          color: #909399;
          font-size: 12px;
        }
      }
    }
  }

  .selected-member-info {
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    gap: 16px;

    .summary-text {
      color: #606266;
      font-size: 14px;
    }
  }

  .order-detail {
    .order-items {
      margin-top: 20px;
    }
  }
}
</style>
