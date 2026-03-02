<template>
  <div>
    <el-row :gutter="16" style="margin-bottom:16px;">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="今日销售额" :value="stats.todaySales" prefix="¥">
            <template #suffix><span style="font-size:12px; color:#67c23a;"> +12.3%</span></template>
          </el-statistic>
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
          <el-statistic title="库存预警" :value="stats.stockWarnings" suffix="项">
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
          <div style="height:300px; display:flex; align-items:center; justify-content:center; color:#999;">
            图表区域 (接入ECharts)
          </div>
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
            <el-table-column prop="name" label="药品名称" />
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
            <el-table-column prop="name" label="药品名称" />
            <el-table-column prop="quantity" label="销量" width="80" />
            <el-table-column prop="amount" label="销售额" width="100">
              <template #default="{ row }">¥{{ row.amount }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const stats = reactive({
  todaySales: 12580,
  todayOrders: 156,
  totalMembers: 3280,
  stockWarnings: 23
})

const todoList = [
  { label: '近效期药品', count: 15, type: 'danger' },
  { label: '低库存预警', count: 8, type: 'warning' },
  { label: '待验收入库', count: 3, type: 'primary' },
  { label: '养护任务', count: 5, type: 'info' },
  { label: '会员生日提醒', count: 12, type: 'success' }
]

const expiringDrugs = [
  { name: '阿莫西林胶囊', batchNo: 'B20250801', expireDate: '2026-04-15', daysLeft: 44 },
  { name: '布洛芬缓释胶囊', batchNo: 'B20250612', expireDate: '2026-04-20', daysLeft: 49 },
  { name: '复方甘草片', batchNo: 'B20250305', expireDate: '2026-05-01', daysLeft: 60 },
  { name: '板蓝根颗粒', batchNo: 'B20250410', expireDate: '2026-05-15', daysLeft: 74 },
  { name: '维生素C片', batchNo: 'B20250520', expireDate: '2026-06-01', daysLeft: 91 }
]

const topDrugs = [
  { name: '感冒灵颗粒', quantity: 286, amount: '4,290' },
  { name: '阿莫西林胶囊', quantity: 198, amount: '3,564' },
  { name: '布洛芬缓释胶囊', quantity: 175, amount: '2,625' },
  { name: '氯雷他定片', quantity: 152, amount: '1,824' },
  { name: '板蓝根颗粒', quantity: 143, amount: '1,716' }
]
</script>
