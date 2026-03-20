<template>
  <div class="temperature-log-container">
    <el-card shadow="never">
      <!-- 统计卡片 -->
      <el-row :gutter="16" class="stats-row">
        <el-col :span="8">
          <el-card shadow="hover">
            <el-statistic title="监测点数量" :value="stats.locationCount" suffix="个">
              <template #prefix>
                <el-icon color="#409EFF"><Location /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <el-statistic title="今日记录数" :value="stats.todayCount" suffix="条">
              <template #prefix>
                <el-icon color="#67C23A"><Document /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <el-statistic title="异常次数" :value="stats.abnormalCount" suffix="次">
              <template #prefix>
                <el-icon color="#F56C6C"><Warning /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
      </el-row>

      <!-- 搜索区 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="监测位置">
          <el-select v-model="searchForm.location" placeholder="请选择" clearable>
            <el-option label="仓库A区" value="WAREHOUSE_A" />
            <el-option label="仓库B区" value="WAREHOUSE_B" />
            <el-option label="冷藏室" value="COLD_STORAGE" />
            <el-option label="营业大厅" value="SALES_HALL" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="仅显示异常">
          <el-switch v-model="searchForm.abnormalOnly" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        border
        highlight-current-row
      >
        <el-table-column prop="location" label="位置" width="120" align="center">
          <template #default="{ row }">
            {{ getLocationLabel(row.location) }}
          </template>
        </el-table-column>
        <el-table-column prop="temperature" label="温度" width="100" align="right">
          <template #default="{ row }">
            <span :style="{ color: row.tempAbnormal ? '#f56c6c' : '#333' }">
              {{ row.temperature }}℃
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="humidity" label="湿度" width="100" align="right">
          <template #default="{ row }">
            <span :style="{ color: row.humidityAbnormal ? '#f56c6c' : '#333' }">
              {{ row.humidity }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="recordTime" label="记录时间" width="160" />
        <el-table-column prop="isAbnormal" label="是否异常" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isAbnormal ? 'danger' : 'success'">
              {{ row.isAbnormal ? '异常' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alarmStatus" label="报警状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.alarmStatus === 'ALARMED'" type="danger">
              <el-icon><Bell /></el-icon> 已报警
            </el-tag>
            <el-tag v-else type="info">未报警</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handleStatus" label="处理状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.handleStatus === 'HANDLED'" type="success">已处理</el-tag>
            <el-tag v-else-if="row.handleStatus === 'HANDLING'" type="warning">处理中</el-tag>
            <el-tag v-else type="info">未处理</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handleMeasure" label="处理措施" min-width="150" show-overflow-tooltip />
        <el-table-column prop="handlePerson" label="处理人" width="100" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Location, Document, Warning, Bell } from '@element-plus/icons-vue'
import { getTempHumidityLogs } from '@/api/gsp'

// 统计数据
const stats = reactive({
  locationCount: 4,
  todayCount: 0,
  abnormalCount: 0
})

// 搜索表单
const searchForm = reactive({
  location: '',
  abnormalOnly: false
})

const dateRange = ref([])

// 表格数据
const tableData = ref([])
const loading = ref(false)
const { tableRef, selectFirstRow } = useTableKeyboardNav(tableData)

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 位置映射
const locationMap = {
  WAREHOUSE_A: '仓库A区',
  WAREHOUSE_B: '仓库B区',
  COLD_STORAGE: '冷藏室',
  SALES_HALL: '营业大厅'
}

const getLocationLabel = (location) => {
  return locationMap[location] || location
}

// 查询列表
const handleSearch = async () => {
  try {
    loading.value = true
    const params = {
      ...searchForm,
      current: pagination.current,
      size: pagination.size
    }

    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }

    const res = await getTempHumidityLogs(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
      selectFirstRow()

      // 更新统计数据
      stats.todayCount = res.data.todayCount || 0
      stats.abnormalCount = res.data.abnormalCount || 0
    }
  } catch (error) {
    ElMessage.error('查询失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 重置搜索
const handleReset = () => {
  searchForm.location = ''
  searchForm.abnormalOnly = false
  dateRange.value = []
  pagination.current = 1
  handleSearch()
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped lang="scss">
.temperature-log-container {
  padding: 16px;

  .stats-row {
    margin-bottom: 16px;

    :deep(.el-card__body) {
      padding: 16px;
    }
  }

  .search-form {
    margin-top: 16px;
    margin-bottom: 16px;
  }
}
</style>
