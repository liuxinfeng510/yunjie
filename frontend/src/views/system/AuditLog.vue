<template>
  <div class="audit-log">
    <el-card>
      <template #header>
        <span>审计日志</span>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="操作人">
          <el-input v-model="queryForm.operatorName" placeholder="请输入操作人" clearable @keydown.enter.prevent="loadData" @keydown.up.prevent="handleArrowUp" @keydown.down.prevent="handleArrowDown" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="queryForm.operationType" placeholder="请选择" clearable>
            <el-option label="登录" value="LOGIN" />
            <el-option label="登出" value="LOGOUT" />
            <el-option label="新增" value="CREATE" />
            <el-option label="修改" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="导入" value="IMPORT" />
            <el-option label="导出" value="EXPORT" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作模块">
          <el-select v-model="queryForm.module" placeholder="请选择" clearable>
            <el-option label="药品管理" value="drug" />
            <el-option label="库存管理" value="inventory" />
            <el-option label="销售管理" value="sale" />
            <el-option label="会员管理" value="member" />
            <el-option label="GSP合规" value="gsp" />
            <el-option label="系统管理" value="system" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="operationType" label="操作类型" width="80">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.operationType)">{{ getTypeName(row.operationType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="操作模块" width="100">
          <template #default="{ row }">{{ getModuleName(row.module) }}</template>
        </el-table-column>
        <el-table-column prop="description" label="操作描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="130" />
        <el-table-column prop="requestUrl" label="请求路径" width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'danger'">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="操作时间" width="170" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[20, 50, 100, 200]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作人">{{ currentLog.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ currentLog.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ getTypeName(currentLog.operationType) }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ getModuleName(currentLog.module) }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ currentLog.status === 'success' ? '成功' : '失败' }}</el-descriptions-item>
        <el-descriptions-item label="请求路径" :span="2">{{ currentLog.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ currentLog.description }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre class="json-pre">{{ formatJson(currentLog.requestParams) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="响应结果" :span="2" v-if="currentLog.responseData">
          <pre class="json-pre">{{ formatJson(currentLog.responseData) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAuditLogPage } from '@/api/system'
import { useTableKeyboardNav } from '@/composables/useTableKeyboardNav'

const loading = ref(false)
const tableData = ref([])
const { tableRef, handleArrowUp, handleArrowDown, selectFirstRow } = useTableKeyboardNav(tableData)
const detailVisible = ref(false)
const currentLog = ref({})

const queryForm = reactive({
  operatorName: '',
  operationType: '',
  module: '',
  dateRange: []
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

const typeMap = {
  LOGIN: '登录',
  LOGOUT: '登出',
  CREATE: '新增',
  UPDATE: '修改',
  DELETE: '删除',
  IMPORT: '导入',
  EXPORT: '导出'
}

const typeColorMap = {
  LOGIN: 'success',
  LOGOUT: 'info',
  CREATE: 'primary',
  UPDATE: 'warning',
  DELETE: 'danger',
  IMPORT: 'success',
  EXPORT: 'info'
}

const moduleMap = {
  drug: '药品管理',
  inventory: '库存管理',
  sale: '销售管理',
  member: '会员管理',
  gsp: 'GSP合规',
  system: '系统管理'
}

const getTypeName = (type) => typeMap[type] || type
const getTypeColor = (type) => typeColorMap[type] || 'info'
const getModuleName = (module) => moduleMap[module] || module

const formatJson = (jsonStr) => {
  try {
    if (!jsonStr) return ''
    const obj = typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr
    return JSON.stringify(obj, null, 2)
  } catch {
    return jsonStr
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      operatorName: queryForm.operatorName,
      operationType: queryForm.operationType,
      module: queryForm.module
    }
    if (queryForm.dateRange && queryForm.dateRange.length === 2) {
      params.startDate = queryForm.dateRange[0]
      params.endDate = queryForm.dateRange[1]
    }
    const res = await getAuditLogPage(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
    selectFirstRow()
  } catch (error) {
    ElMessage.error('加载审计日志失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryForm.operatorName = ''
  queryForm.operationType = ''
  queryForm.module = ''
  queryForm.dateRange = []
  pagination.current = 1
  loadData()
}

const handleDetail = (row) => {
  currentLog.value = row
  detailVisible.value = true
}

const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.audit-log {
  padding: 20px;
}
.search-form {
  margin-bottom: 16px;
}
.json-pre {
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  max-height: 200px;
  overflow: auto;
  font-size: 12px;
  margin: 0;
}
</style>
