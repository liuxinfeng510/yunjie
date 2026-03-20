<template>
  <div class="batch-manage">
    <el-card>
      <template #header>
        <span>药品批次管理</span>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="queryForm.drugName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="批号">
          <el-input v-model="queryForm.batchNo" placeholder="请输入批号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" value="normal" />
            <el-option label="近效期" value="near_expiry" />
            <el-option label="已过期" value="expired" />
            <el-option label="已锁定" value="locked" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 统计卡片 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="6">
          <el-statistic title="总批次数" :value="statistics.total" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="正常批次" :value="statistics.normal">
            <template #suffix><span style="color: #67c23a;">个</span></template>
          </el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="近效期批次" :value="statistics.nearExpiry">
            <template #suffix><span style="color: #e6a23c;">个</span></template>
          </el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="已过期批次" :value="statistics.expired">
            <template #suffix><span style="color: #f56c6c;">个</span></template>
          </el-statistic>
        </el-col>
      </el-row>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="drugName" label="商品名称" min-width="150" />
        <el-table-column prop="specification" label="规格" width="100" />
        <el-table-column prop="batchNo" label="批号" width="130" />
        <el-table-column prop="produceDate" label="生产日期" width="110" />
        <el-table-column prop="expireDate" label="有效期至" width="110" />
        <el-table-column prop="remainingDays" label="剩余天数" width="100">
          <template #default="{ row }">
            <span :style="{ color: row.remainingDays <= 90 ? '#f56c6c' : row.remainingDays <= 180 ? '#e6a23c' : '#67c23a' }">
              {{ row.remainingDays }}天
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="stockQuantity" label="库存数量" width="100" />
        <el-table-column prop="supplier" label="供应商" width="150" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button v-if="row.status !== 'locked'" type="warning" link @click="handleLock(row)">锁定</el-button>
            <el-button v-else type="success" link @click="handleUnlock(row)">解锁</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="批次详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="商品名称">{{ currentBatch.drugName }}</el-descriptions-item>
        <el-descriptions-item label="批号">{{ currentBatch.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="规格">{{ currentBatch.specification }}</el-descriptions-item>
        <el-descriptions-item label="生产企业">{{ currentBatch.manufacturer }}</el-descriptions-item>
        <el-descriptions-item label="生产日期">{{ currentBatch.produceDate }}</el-descriptions-item>
        <el-descriptions-item label="有效期至">{{ currentBatch.expireDate }}</el-descriptions-item>
        <el-descriptions-item label="入库数量">{{ currentBatch.inQuantity }}</el-descriptions-item>
        <el-descriptions-item label="当前库存">{{ currentBatch.quantity }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ currentBatch.supplier }}</el-descriptions-item>
        <el-descriptions-item label="采购单价">¥{{ currentBatch.purchasePrice }}</el-descriptions-item>
        <el-descriptions-item label="入库时间">{{ currentBatch.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusName(currentBatch.status) }}</el-descriptions-item>
      </el-descriptions>
      <el-divider>流转记录</el-divider>
      <el-timeline>
        <el-timeline-item v-for="log in currentBatch.logs" :key="log.id" :timestamp="log.time" placement="top">
          {{ log.action }}: {{ log.quantity }}{{ log.unit }} - {{ log.operator }}
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBatchPage, getBatchDetail, lockBatch, unlockBatch, getBatchStatistics } from '@/api/batch'

const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const currentBatch = ref({})

const queryForm = reactive({
  drugName: '',
  batchNo: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

const statistics = reactive({
  total: 0,
  normal: 0,
  nearExpiry: 0,
  expired: 0
})

const statusMap = { normal: '正常', near_expiry: '近效期', expired: '已过期', locked: '已锁定' }
const statusColorMap = { normal: 'success', near_expiry: 'warning', expired: 'danger', locked: 'info' }
const getStatusName = (status) => statusMap[status] || status
const getStatusColor = (status) => statusColorMap[status] || 'info'

const loadData = async () => {
  loading.value = true
  try {
    const res = await getBatchPage({
      current: pagination.current,
      size: pagination.size,
      ...queryForm
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载批次列表失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const res = await getBatchStatistics()
    Object.assign(statistics, res.data)
  } catch (error) {
    console.error('加载统计失败', error)
  }
}

const resetQuery = () => {
  queryForm.drugName = ''
  queryForm.batchNo = ''
  queryForm.status = ''
  pagination.current = 1
  loadData()
}

const handleDetail = (row) => {
  currentBatch.value = row
  detailVisible.value = true
}

const handleLock = async (row) => {
  try {
    await ElMessageBox.confirm('锁定后该批次药品将无法销售，确定要锁定吗？', '提示', { type: 'warning' })
    await lockBatch(row.id)
    ElMessage.success('锁定成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('锁定失败')
    }
  }
}

const handleUnlock = async (row) => {
  try {
    await unlockBatch(row.id)
    ElMessage.success('解锁成功')
    loadData()
  } catch (error) {
    ElMessage.error('解锁失败')
  }
}

onMounted(() => {
  loadData()
  loadStatistics()
})
</script>

<style scoped>
.batch-manage {
  padding: 20px;
}
.search-form {
  margin-bottom: 16px;
}
</style>
