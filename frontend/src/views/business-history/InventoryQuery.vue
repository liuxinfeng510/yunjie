<template>
  <div class="inventory-query-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="药品名称">
          <el-input
            v-model="searchForm.drugName"
            placeholder="名称/拼音/简码"
            clearable
            @clear="handleSearch"
            @keydown.enter.prevent="handleSearch"
            @keydown.up.prevent="handleArrowUp"
            @keydown.down.prevent="handleArrowDown"
          />
        </el-form-item>
        <el-form-item label="库存状态">
          <el-select v-model="searchForm.stockStatus" placeholder="全部" clearable @change="handleSearch">
            <el-option label="正常" value="normal" />
            <el-option label="库存不足" value="low" />
            <el-option label="零库存" value="zero" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 12px;">
      <div class="summary-bar">
        <span>共 <b>{{ total }}</b> 个品种</span>
        <span v-if="summary.totalValue != null">库存总值：<b style="color:#409eff">¥{{ summary.totalValue?.toFixed(2) }}</b></span>
      </div>
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        stripe
        border
        highlight-current-row
        style="width: 100%"
      >
        <el-table-column prop="drugName" label="药品名称" min-width="180" />
        <el-table-column prop="genericName" label="通用名" min-width="150" />
        <el-table-column prop="specification" label="规格" width="120" />
        <el-table-column prop="manufacturer" label="生产企业" min-width="160" />
        <el-table-column prop="unit" label="单位" width="60" align="center" />
        <el-table-column prop="quantity" label="库存数量" width="100" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.quantity <= (row.minStock || 0) ? '#f56c6c' : '#303133', fontWeight: 600 }">
              {{ row.quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="retailPrice" label="零售价" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.retailPrice?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="库存金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ ((row.quantity || 0) * (row.retailPrice || 0)).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="货位" width="100" />
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; justify-content: flex-end;"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getInventoryPage } from '@/api/inventory'
import { useTableKeyboardNav } from '@/composables/useTableKeyboardNav'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const summary = reactive({ totalValue: null })

const { tableRef, handleArrowUp, handleArrowDown, selectFirstRow } = useTableKeyboardNav(tableData)

const searchForm = reactive({
  drugName: '',
  stockStatus: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (searchForm.drugName) params.drugName = searchForm.drugName
    if (searchForm.stockStatus) params.stockStatus = searchForm.stockStatus
    const res = await getInventoryPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
      summary.totalValue = tableData.value.reduce((sum, item) => sum + (item.quantity || 0) * (item.retailPrice || 0), 0)
      selectFirstRow()
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const handleReset = () => {
  searchForm.drugName = ''
  searchForm.stockStatus = ''
  handleSearch()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.inventory-query-container {
  padding: 16px;
}
.summary-bar {
  display: flex;
  gap: 24px;
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
}
</style>
