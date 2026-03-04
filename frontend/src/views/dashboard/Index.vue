<template>
  <div>
    <el-row :gutter="16" style="margin-bottom:16px;">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="今日销售额" :value="stats.todaySales" prefix="¥" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="今日订单数" :value="stats.todayOrders" suffix="单" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="会员总数" :value="stats.totalMembers" suffix="人" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="库存预警" :value="stats.stockWarnings">
            <template #suffix>
              <span style="font-size:12px; color:#f56c6c;"> 项需处理</span>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>近7天销售趋势</template>
          <div ref="chartRef" style="height:300px;" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>待办事项</template>
          <div style="min-height:300px;">
            <div v-for="item in todoList" :key="item.label"
              style="display:flex; justify-content:space-between; padding:10px 0; border-bottom:1px solid #f0f0f0;">
              <span>{{ item.label }}</span>
              <el-badge :value="item.count" :type="item.type" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px;">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>近效期药品 TOP5</template>
          <el-table :data="expiringDrugs" size="small" style="width:100%;">
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="batchNo" label="批号" width="100" />
            <el-table-column prop="expireDate" label="有效期至" width="110" />
            <el-table-column prop="daysLeft" label="剩余天数" width="90">
              <template #default="{ row }">
                <el-tag :type="row.daysLeft < 30 ? 'danger' : 'warning'" size="small">{{ row.daysLeft }}天</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>畅销药品 TOP5</template>
          <el-table :data="topDrugs" size="small" style="width:100%;">
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="quantity" label="销量" width="80" />
            <el-table-column label="销售额" width="100">
              <template #default="{ row }">¥{{ row.amount }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getDashboardOverview, getSalesTrend, getDashboardTodos, getExpiringDrugs, getTopDrugs } from '@/api/dashboard'

const chartRef = ref(null)
let chartInstance = null

const stats = reactive({
  todaySales: 0,
  todayOrders: 0,
  totalMembers: 0,
  stockWarnings: 0
})

const todoList = ref([])
const expiringDrugs = ref([])
const topDrugs = ref([])

const loadOverview = async () => {
  try {
    const res = await getDashboardOverview()
    if (res.code === 200) {
      stats.todaySales = res.data.todaySales || 0
      stats.todayOrders = res.data.todayOrders || 0
      stats.totalMembers = res.data.totalMembers || 0
      stats.stockWarnings = res.data.stockWarnings || 0
    }
  } catch { /* 静默 */ }
}

const loadTrend = async () => {
  try {
    const res = await getSalesTrend()
    if (res.code === 200 && res.data) {
      await nextTick()
      renderChart(res.data)
    }
  } catch { /* 静默 */ }
}

const renderChart = (data) => {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const dates = data.map(d => d.date.substring(5)) // MM-DD
  const amounts = data.map(d => d.amount)
  const counts = data.map(d => d.orderCount)

  chartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: { data: ['销售额', '订单数'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates },
    yAxis: [
      { type: 'value', name: '金额(元)', position: 'left' },
      { type: 'value', name: '订单数', position: 'right' }
    ],
    series: [
      {
        name: '销售额',
        type: 'bar',
        data: amounts,
        itemStyle: { color: '#409eff' }
      },
      {
        name: '订单数',
        type: 'line',
        yAxisIndex: 1,
        data: counts,
        smooth: true,
        itemStyle: { color: '#67c23a' }
      }
    ]
  })
}

const loadTodos = async () => {
  try {
    const res = await getDashboardTodos()
    if (res.code === 200) {
      const d = res.data
      todoList.value = [
        { label: '近效期药品', count: d.expiringCount || 0, type: 'danger' },
        { label: '低库存预警', count: d.lowStockCount || 0, type: 'warning' },
        { label: '待审核入库', count: d.pendingStockIn || 0, type: 'primary' },
        { label: '待养护任务', count: d.maintenanceCount || 0, type: 'info' }
      ]
    }
  } catch {
    todoList.value = [
      { label: '近效期药品', count: 0, type: 'danger' },
      { label: '低库存预警', count: 0, type: 'warning' },
      { label: '待审核入库', count: 0, type: 'primary' },
      { label: '待养护任务', count: 0, type: 'info' }
    ]
  }
}

const loadExpiringDrugs = async () => {
  try {
    const res = await getExpiringDrugs()
    if (res.code === 200) { expiringDrugs.value = res.data || [] }
  } catch { /* 静默 */ }
}

const loadTopDrugs = async () => {
  try {
    const res = await getTopDrugs()
    if (res.code === 200) { topDrugs.value = res.data || [] }
  } catch { /* 静默 */ }
}

onMounted(() => {
  loadOverview()
  loadTrend()
  loadTodos()
  loadExpiringDrugs()
  loadTopDrugs()

  window.addEventListener('resize', () => {
    chartInstance?.resize()
  })
})
</script>
