<template>
  <div class="herb-fill-log-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>装斗记录</span>
          <el-button type="primary" @click="handlePrint">
            <el-icon><Printer /></el-icon>
            打印
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="药品名称">
          <el-input v-model="searchForm.drugName" placeholder="请输入药品名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" stripe border style="width: 100%">
        <el-table-column prop="recordNo" label="编号" width="200" />
        <el-table-column prop="drugName" label="药品名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="specification" label="规格" width="100" />
        <el-table-column prop="unit" label="单位" width="60" />
        <el-table-column prop="batchNo" label="批号" width="120" />
        <el-table-column prop="fillQuantity" label="装斗数量" width="90" />
        <el-table-column prop="fillDate" label="装斗日期" width="110" />
        <el-table-column prop="origin" label="产地" width="100" show-overflow-tooltip />
        <el-table-column prop="manufacturer" label="生产企业" width="140" show-overflow-tooltip />
        <el-table-column prop="supplierName" label="购进企业" width="140" show-overflow-tooltip />
        <el-table-column prop="qualityStatus" label="质量状况" width="90">
          <template #default="{ row }">
            <el-tag :type="row.qualityStatus === '合格' ? 'success' : 'danger'" size="small">
              {{ row.qualityStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="acceptanceResult" label="验收结论" width="90">
          <template #default="{ row }">
            <el-tag :type="row.acceptanceResult === '合格' ? 'success' : 'danger'" size="small">
              {{ row.acceptanceResult }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fillPerson" label="装斗人" width="80" />
        <el-table-column prop="reviewer" label="复核人" width="80" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 编辑状态对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑装斗记录" width="450px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="质量状况">
          <el-select v-model="editForm.qualityStatus" style="width: 100%">
            <el-option label="合格" value="合格" />
            <el-option label="不合格" value="不合格" />
          </el-select>
        </el-form-item>
        <el-form-item label="验收结论">
          <el-select v-model="editForm.acceptanceResult" style="width: 100%">
            <el-option label="合格" value="合格" />
            <el-option label="不合格" value="不合格" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleEditSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 打印区域（隐藏） -->
    <div v-show="false">
      <div ref="printRef" class="print-area">
        <h2 style="text-align: center; margin-bottom: 10px;">中药饮片装斗记录</h2>
        <table class="print-table">
          <thead>
            <tr>
              <th>编号</th>
              <th>药品名称</th>
              <th>规格</th>
              <th>单位</th>
              <th>批号</th>
              <th>装斗数量</th>
              <th>装斗日期</th>
              <th>产地</th>
              <th>生产企业</th>
              <th>购进企业</th>
              <th>质量状况</th>
              <th>验收结论</th>
              <th>装斗人</th>
              <th>复核人</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in printData" :key="row.id">
              <td>{{ row.recordNo }}</td>
              <td>{{ row.drugName }}</td>
              <td>{{ row.specification }}</td>
              <td>{{ row.unit }}</td>
              <td>{{ row.batchNo }}</td>
              <td>{{ row.fillQuantity }}</td>
              <td>{{ row.fillDate }}</td>
              <td>{{ row.origin }}</td>
              <td>{{ row.manufacturer }}</td>
              <td>{{ row.supplierName }}</td>
              <td>{{ row.qualityStatus }}</td>
              <td>{{ row.acceptanceResult }}</td>
              <td>{{ row.fillPerson }}</td>
              <td>{{ row.reviewer }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Printer } from '@element-plus/icons-vue'
import { getHerbFillLogPage, getHerbFillLogList, updateHerbFillLogStatus } from '@/api/herb'

const loading = ref(false)
const tableData = ref([])
const printData = ref([])
const printRef = ref(null)

const searchForm = reactive({
  drugName: '',
  dateRange: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const editDialogVisible = ref(false)
const editLoading = ref(false)
const editForm = reactive({
  id: null,
  qualityStatus: '',
  acceptanceResult: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      drugName: searchForm.drugName || undefined,
      startDate: searchForm.dateRange ? searchForm.dateRange[0] : undefined,
      endDate: searchForm.dateRange ? searchForm.dateRange[1] : undefined
    }
    const res = await getHerbFillLogPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('获取装斗记录失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleReset = () => {
  searchForm.drugName = ''
  searchForm.dateRange = null
  pagination.pageNum = 1
  fetchData()
}

const handleEdit = (row) => {
  editForm.id = row.id
  editForm.qualityStatus = row.qualityStatus
  editForm.acceptanceResult = row.acceptanceResult
  editDialogVisible.value = true
}

const handleEditSubmit = async () => {
  editLoading.value = true
  try {
    const res = await updateHerbFillLogStatus(editForm.id, {
      qualityStatus: editForm.qualityStatus,
      acceptanceResult: editForm.acceptanceResult
    })
    if (res.code === 200) {
      ElMessage.success('更新成功')
      editDialogVisible.value = false
      fetchData()
    }
  } catch (error) {
    ElMessage.error('更新失败')
  } finally {
    editLoading.value = false
  }
}

const handlePrint = async () => {
  try {
    const params = {
      startDate: searchForm.dateRange ? searchForm.dateRange[0] : undefined,
      endDate: searchForm.dateRange ? searchForm.dateRange[1] : undefined
    }
    const res = await getHerbFillLogList(params)
    if (res.code === 200) {
      printData.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('获取打印数据失败')
    return
  }

  setTimeout(() => {
    const printWindow = window.open('', '_blank')
    printWindow.document.write(`
      <html>
      <head>
        <title>中药饮片装斗记录</title>
        <style>
          @page { size: A4 landscape; margin: 10mm; }
          body { font-family: SimSun, serif; font-size: 12px; }
          h2 { text-align: center; margin-bottom: 10px; }
          table { width: 100%; border-collapse: collapse; }
          th, td { border: 1px solid #000; padding: 4px 6px; text-align: center; font-size: 11px; }
          th { background-color: #f0f0f0; font-weight: bold; }
        </style>
      </head>
      <body>${printRef.value.innerHTML}</body>
      </html>
    `)
    printWindow.document.close()
    printWindow.focus()
    printWindow.print()
    printWindow.close()
  }, 300)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.herb-fill-log-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 16px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
