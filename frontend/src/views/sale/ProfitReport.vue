<template>
  <div class="profit-report">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            :shortcuts="dateShortcuts"
            style="width: 280px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 汇总卡片 -->
    <div class="summary-cards">
      <div class="summary-item">
        <div class="summary-label">销售总额</div>
        <div class="summary-value blue">{{ formatMoney(summary.totalSales) }}</div>
        <div class="summary-sub">{{ summary.orderCount }} 笔订单</div>
      </div>
      <div class="summary-item">
        <div class="summary-label">总成本</div>
        <div class="summary-value orange">{{ formatMoney(summary.totalCost) }}</div>
      </div>
      <div class="summary-item">
        <div class="summary-label">总利润</div>
        <div class="summary-value" :class="summary.totalProfit >= 0 ? 'green' : 'red'">
          {{ formatMoney(summary.totalProfit) }}
        </div>
      </div>
      <div class="summary-item">
        <div class="summary-label">利润率</div>
        <div class="summary-value" :class="summary.profitRate >= 0 ? 'green' : 'red'">
          {{ summary.profitRate }}%
        </div>
      </div>
    </div>

    <!-- 左右布局：表格 + 图表 -->
    <div class="main-content">
      <!-- 左侧表格 -->
      <el-card shadow="never" class="table-card">
        <div class="view-switch">
          <el-radio-group v-model="viewMode" @change="handleViewChange">
            <el-radio-button value="day">按日汇总</el-radio-button>
            <el-radio-button value="order">按订单明细</el-radio-button>
          </el-radio-group>
        </div>

        <!-- 按日汇总表格 -->
        <el-table v-if="viewMode === 'day'" :data="tableData" v-loading="loading" border stripe>
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column prop="orderCount" label="订单数" width="80" align="center" />
          <el-table-column label="销售额" width="110" align="right">
            <template #default="{ row }">{{ formatMoney(row.salesAmount) }}</template>
          </el-table-column>
          <el-table-column label="成本" width="110" align="right">
            <template #default="{ row }">{{ formatMoney(row.costAmount) }}</template>
          </el-table-column>
          <el-table-column label="利润" width="110" align="right">
            <template #default="{ row }">
              <span :style="{ color: row.profitAmount >= 0 ? '#67c23a' : '#f56c6c' }">
                {{ formatMoney(row.profitAmount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="利润率" width="90" align="center">
            <template #default="{ row }">
              <span :style="{ color: row.profitRate >= 0 ? '#67c23a' : '#f56c6c' }">
                {{ row.profitRate }}%
              </span>
            </template>
          </el-table-column>
        </el-table>

        <!-- 按订单明细表格 -->
        <el-table v-else :data="tableData" v-loading="loading" border stripe>
          <el-table-column prop="orderNo" label="订单号" min-width="180" />
          <el-table-column prop="createdAt" label="时间" width="160" />
          <el-table-column prop="cashierName" label="收银员" width="90" />
          <el-table-column label="销售额" width="110" align="right">
            <template #default="{ row }">{{ formatMoney(row.salesAmount) }}</template>
          </el-table-column>
          <el-table-column label="成本" width="100" align="right">
            <template #default="{ row }">{{ formatMoney(row.costAmount) }}</template>
          </el-table-column>
          <el-table-column label="利润" width="100" align="right">
            <template #default="{ row }">
              <span :style="{ color: row.profitAmount >= 0 ? '#67c23a' : '#f56c6c' }">
                {{ formatMoney(row.profitAmount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="利润率" width="80" align="center">
            <template #default="{ row }">
              <span :style="{ color: row.profitRate >= 0 ? '#67c23a' : '#f56c6c' }">
                {{ row.profitRate }}%
              </span>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrap" v-if="total > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next"
            @size-change="loadData"
            @current-change="loadData"
          />
        </div>
      </el-card>

      <!-- 右侧图表 -->
      <div class="chart-panel">
        <el-card shadow="never" class="chart-card">
          <template #header><span class="chart-title">销售额 / 成本 / 利润趋势</span></template>
          <div ref="barChartRef" class="chart-container"></div>
        </el-card>
        <el-card shadow="never" class="chart-card">
          <template #header><span class="chart-title">利润构成</span></template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { getProfitReport } from '@/api/sale'
import { useAuthStore } from '@/store/auth'
import * as echarts from 'echarts'

const authStore = useAuthStore()

const loading = ref(false)
const viewMode = ref('day')
const currentPage = ref(1)
const pageSize = ref(31)
const total = ref(0)
const tableData = ref([])
const allDayData = ref([])
const summary = reactive({
  totalSales: 0,
  totalCost: 0,
  totalProfit: 0,
  profitRate: 0,
  orderCount: 0
})

// 图表 DOM 引用
const barChartRef = ref(null)
const pieChartRef = ref(null)
let barChart = null
let pieChart = null

// 默认当月
const now = new Date()
const firstDay = new Date(now.getFullYear(), now.getMonth(), 1)
const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0)
const formatDate = (d) => {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const searchForm = reactive({
  dateRange: [formatDate(firstDay), formatDate(lastDay)]
})

const dateShortcuts = [
  { text: '今天', value: () => { const d = new Date(); return [d, d] } },
  { text: '近7天', value: () => { const e = new Date(); const s = new Date(); s.setDate(s.getDate() - 6); return [s, e] } },
  { text: '本月', value: () => [firstDay, lastDay] },
  { text: '上月', value: () => {
    const s = new Date(now.getFullYear(), now.getMonth() - 1, 1)
    const e = new Date(now.getFullYear(), now.getMonth(), 0)
    return [s, e]
  }}
]

const formatMoney = (val) => {
  const num = Number(val) || 0
  return '¥' + num.toFixed(2)
}

// 初始化图表
const initCharts = () => {
  if (barChartRef.value) {
    barChart = echarts.init(barChartRef.value)
  }
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
  }
}

// 更新柱状图
const updateBarChart = () => {
  if (!barChart) return
  const data = allDayData.value
  if (!data.length) {
    barChart.clear()
    return
  }
  const dates = data.map(d => d.date ? d.date.substring(5) : '')
  const sales = data.map(d => Number(d.salesAmount) || 0)
  const costs = data.map(d => Number(d.costAmount) || 0)
  const profits = data.map(d => Number(d.profitAmount) || 0)

  barChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        let tip = params[0].axisValue + '<br/>'
        params.forEach(p => {
          tip += `${p.marker} ${p.seriesName}: ¥${Number(p.value).toFixed(2)}<br/>`
        })
        return tip
      }
    },
    legend: { data: ['销售额', '成本', '利润'], bottom: 0, textStyle: { fontSize: 12 } },
    grid: { top: 10, right: 15, bottom: 36, left: 50, containLabel: false },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { fontSize: 11, rotate: dates.length > 15 ? 45 : 0 }
    },
    yAxis: {
      type: 'value',
      axisLabel: { fontSize: 11, formatter: (v) => v >= 1000 ? (v / 1000).toFixed(1) + 'k' : v }
    },
    series: [
      { name: '销售额', type: 'bar', data: sales, itemStyle: { color: '#409eff' }, barMaxWidth: 20 },
      { name: '成本', type: 'bar', data: costs, itemStyle: { color: '#e6a23c' }, barMaxWidth: 20 },
      { name: '利润', type: 'line', data: profits, itemStyle: { color: '#67c23a' }, lineStyle: { width: 2 }, symbol: 'circle', symbolSize: 6 }
    ]
  })
}

// 更新饼图
const updatePieChart = () => {
  if (!pieChart) return
  const cost = Number(summary.totalCost) || 0
  const profit = Number(summary.totalProfit) || 0
  if (cost === 0 && profit === 0) {
    pieChart.clear()
    return
  }
  pieChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: (p) => `${p.name}: ¥${Number(p.value).toFixed(2)} (${p.percent}%)`
    },
    legend: { bottom: 0, textStyle: { fontSize: 12 } },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: {
        show: true,
        formatter: (p) => `${p.name}\n¥${Number(p.value).toFixed(2)}`,
        fontSize: 12
      },
      data: [
        { value: cost, name: '成本', itemStyle: { color: '#e6a23c' } },
        { value: Math.max(profit, 0), name: '利润', itemStyle: { color: '#67c23a' } }
      ]
    }]
  })
}

// 窗口缩放时重绘
const handleResize = () => {
  barChart?.resize()
  pieChart?.resize()
}

const loadData = async () => {
  if (!searchForm.dateRange || searchForm.dateRange.length < 2) return
  loading.value = true
  try {
    // 同时请求日汇总数据（用于图表）和当前视图数据
    const params = {
      startDate: searchForm.dateRange[0],
      endDate: searchForm.dateRange[1],
      groupBy: viewMode.value,
      current: currentPage.value,
      size: pageSize.value,
      storeId: authStore.user?.storeId || undefined
    }
    const res = await getProfitReport(params)
    if (res.code === 200 && res.data) {
      const data = res.data
      Object.assign(summary, data.summary || {})
      tableData.value = data.page?.records || []
      total.value = data.page?.total || 0
    }

    // 图表始终用日汇总数据
    if (viewMode.value === 'day') {
      allDayData.value = tableData.value
    } else {
      // 如果当前是订单视图，额外请求日汇总数据给图表用
      const dayRes = await getProfitReport({
        startDate: searchForm.dateRange[0],
        endDate: searchForm.dateRange[1],
        groupBy: 'day',
        current: 1,
        size: 366,
        storeId: authStore.user?.storeId || undefined
      })
      if (dayRes.code === 200 && dayRes.data) {
        allDayData.value = dayRes.data.page?.records || []
      }
    }

    await nextTick()
    updateBarChart()
    updatePieChart()
  } catch (e) {
    console.error('加载利润数据失败', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  searchForm.dateRange = [formatDate(firstDay), formatDate(lastDay)]
  currentPage.value = 1
  viewMode.value = 'day'
  pageSize.value = 31
  loadData()
}

const handleViewChange = () => {
  currentPage.value = 1
  pageSize.value = viewMode.value === 'day' ? 31 : 20
  loadData()
}

onMounted(() => {
  nextTick(() => {
    initCharts()
    loadData()
  })
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  barChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped lang="scss">
.profit-report {
  padding: 20px;
}
.search-card {
  margin-bottom: 16px;
  .el-form {
    display: flex;
    align-items: center;
  }
}
.summary-cards {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  .summary-item {
    flex: 1;
    background: #fff;
    border-radius: 8px;
    padding: 16px 20px;
    border: 1px solid #ebeef5;
    .summary-label {
      font-size: 13px;
      color: #909399;
      margin-bottom: 8px;
    }
    .summary-value {
      font-size: 24px;
      font-weight: 700;
      &.blue { color: #409eff; }
      &.orange { color: #e6a23c; }
      &.green { color: #67c23a; }
      &.red { color: #f56c6c; }
    }
    .summary-sub {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }
  }
}
.main-content {
  display: flex;
  gap: 16px;
}
.table-card {
  flex: 1;
  min-width: 0;
}
.chart-panel {
  width: 400px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.chart-card {
  .chart-container {
    width: 100%;
    height: 260px;
  }
}
.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.view-switch {
  margin-bottom: 16px;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
