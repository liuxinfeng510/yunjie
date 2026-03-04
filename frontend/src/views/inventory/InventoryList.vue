<template>
  <div class="inventory-list-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="商品名称">
          <el-input
            v-model="searchForm.drugName"
            placeholder="请输入商品名称"
            clearable
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="所属门店">
          <el-select
            v-model="searchForm.storeId"
            placeholder="请选择门店"
            clearable
            @clear="handleSearch"
          >
            <el-option
              v-for="store in stores"
              :key="store.id"
              :label="store.name"
              :value="store.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="低库存预警">
          <el-switch v-model="searchForm.lowStock" @change="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <div>
          <el-tag type="warning" size="large">
            低库存预警: {{ warningCount }} 种药品
          </el-tag>
        </div>
        <div>
          <el-button type="primary" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
        style="width: 100%"
        :row-class-name="getRowClassName"
      >
        <el-table-column prop="drugName" label="商品名称" width="200" />
        <el-table-column prop="specification" label="规格" width="150" />
        <el-table-column prop="batchNo" label="批号" width="150" />
        <el-table-column label="数量" width="120" align="right">
          <template #default="{ row }">
            {{ row.quantity }}
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column label="成本价" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.costPrice?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="货位" width="120" />
        <el-table-column label="安全库存" width="120" align="right">
          <template #default="{ row }">
            {{ row.safeStock }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.quantity < row.safeStock" type="warning">
              库存不足
            </el-tag>
            <el-tag v-else-if="row.quantity === 0" type="danger">
              缺货
            </el-tag>
            <el-tag v-else type="success">
              正常
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="storeName" label="所属门店" width="150" />
        <el-table-column prop="updateTime" label="更新时间" width="160" />
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { getInventoryPage, getInventoryWarnings } from '@/api/inventory'

// 门店数据（示例，实际应从API获取）
const stores = ref([
  { id: 1, name: '总店' },
  { id: 2, name: '分店1' },
  { id: 3, name: '分店2' }
])

// 搜索表单
const searchForm = reactive({
  drugName: '',
  storeId: null,
  lowStock: false
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 低库存预警数量
const warningCount = computed(() => {
  return tableData.value.filter(item => item.quantity < item.safeStock).length
})

// 获取列表数据
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    const res = await getInventoryPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

// 重置
const handleReset = () => {
  searchForm.drugName = ''
  searchForm.storeId = null
  searchForm.lowStock = false
  handleSearch()
}

// 导出
const handleExport = () => {
  ElMessage.success('导出功能开发中')
}

// 行样式
const getRowClassName = ({ row }) => {
  if (row.quantity === 0) {
    return 'row-out-of-stock'
  } else if (row.quantity < row.safeStock) {
    return 'row-low-stock'
  }
  return ''
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.inventory-list-container {
  padding: 20px;

  .search-card {
    margin-bottom: 20px;
  }

  .table-card {
    .table-header {
      margin-bottom: 16px;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}

:deep(.row-out-of-stock) {
  background-color: #fef0f0;
}

:deep(.row-low-stock) {
  background-color: #fdf6ec;
}
</style>
