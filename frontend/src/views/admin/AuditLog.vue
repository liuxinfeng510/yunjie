<template>
  <div class="audit-log">
    <el-card>
      <template #header>
        <span>操作日志</span>
      </template>

      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="租户">
          <el-select v-model="query.tenantId" placeholder="全部租户" clearable style="width: 150px;">
            <el-option v-for="t in tenants" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="query.action" placeholder="全部" clearable style="width: 120px;">
            <el-option label="登录" value="LOGIN" />
            <el-option label="新增" value="CREATE" />
            <el-option label="修改" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="query.dateRange" type="daterange" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 240px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="logs" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="tenantName" label="租户" width="120" />
        <el-table-column prop="username" label="操作人" width="100" />
        <el-table-column prop="action" label="操作类型" width="80">
          <template #default="{ row }">
            <el-tag :type="getActionType(row.action)" size="small">{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="100" />
        <el-table-column prop="description" label="操作描述" min-width="200" />
        <el-table-column prop="ip" label="IP地址" width="130" />
        <el-table-column prop="createdAt" label="操作时间" width="160" />
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end;"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const loading = ref(false)
const logs = ref([])
const tenants = ref([])
const total = ref(0)
const query = ref({
  pageNum: 1,
  pageSize: 20,
  tenantId: '',
  action: '',
  dateRange: null
})

function getActionType(action) {
  const map = { LOGIN: 'info', CREATE: 'success', UPDATE: 'warning', DELETE: 'danger' }
  return map[action] || 'info'
}

async function loadTenants() {
  try {
    const res = await request.get('/tenant/list')
    if (res.code === 200) {
      tenants.value = res.data || []
    }
  } catch (e) {}
}

async function loadData() {
  loading.value = true
  try {
    const params = { ...query.value }
    if (params.dateRange && params.dateRange.length === 2) {
      params.startDate = params.dateRange[0]
      params.endDate = params.dateRange[1]
    }
    delete params.dateRange
    const res = await request.get('/audit-log/page', { params })
    if (res.code === 200) {
      logs.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadTenants()
  loadData()
})
</script>

<style scoped>
.search-form { margin-bottom: 16px; }
</style>
