<template>
  <div class="statistics">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in overviewStats" :key="stat.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: stat.color }">
              <el-icon :size="28"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>租户增长趋势</template>
          <div ref="tenantChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>经营模式分布</template>
          <div ref="modeChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>租户活跃度排行 (Top 10)</template>
          <el-table :data="activeRanking" stripe>
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="name" label="租户名称" />
            <el-table-column prop="storeCount" label="门店数" width="100" />
            <el-table-column prop="userCount" label="用户数" width="100" />
            <el-table-column prop="orderCount" label="订单数" width="100" />
            <el-table-column prop="lastActiveAt" label="最近活跃" width="160" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, shallowRef } from 'vue'
import * as echarts from 'echarts'

const tenantChartRef = ref()
const modeChartRef = ref()
const tenantChart = shallowRef()
const modeChart = shallowRef()

const overviewStats = ref([
  { label: '租户总数', value: 12, icon: 'OfficeBuilding', color: '#409eff' },
  { label: '本月新增', value: 3, icon: 'Plus', color: '#67c23a' },
  { label: '门店总数', value: 45, icon: 'Shop', color: '#e6a23c' },
  { label: '用户总数', value: 128, icon: 'User', color: '#f56c6c' }
])

const activeRanking = ref([
  { name: '仁和大药房', storeCount: 8, userCount: 24, orderCount: 1250, lastActiveAt: '2026-03-02 10:30' },
  { name: '益丰药店', storeCount: 5, userCount: 15, orderCount: 980, lastActiveAt: '2026-03-02 09:45' },
  { name: '老百姓药房', storeCount: 12, userCount: 36, orderCount: 2100, lastActiveAt: '2026-03-01 18:20' }
])

onMounted(() => {
  initTenantChart()
  initModeChart()
})

function initTenantChart() {
  tenantChart.value = echarts.init(tenantChartRef.value)
  tenantChart.value.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月', '6月'] },
    yAxis: { type: 'value' },
    series: [{
      name: '新增租户',
      type: 'line',
      smooth: true,
      data: [2, 3, 1, 4, 2, 3],
      areaStyle: { opacity: 0.3 }
    }]
  })
}

function initModeChart() {
  modeChart.value = echarts.init(modeChartRef.value)
  modeChart.value.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 10 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: [
        { name: '单店模式', value: 8 },
        { name: '连锁模式', value: 4 }
      ]
    }]
  })
}
</script>

<style scoped>
.stat-card { height: 120px; }
.stat-content { display: flex; align-items: center; height: 80px; }
.stat-icon { width: 60px; height: 60px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #fff; margin-right: 16px; }
.stat-info { flex: 1; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
</style>
