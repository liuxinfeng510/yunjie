<template>
  <div class="stock-check-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="盘点类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable @change="handleSearch">
            <el-option label="全盘" value="全盘" />
            <el-option label="抽盘" value="抽盘" />
            <el-option label="动态盘" value="动态盘" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable @change="handleSearch">
            <el-option label="盘点中" value="盘点中" />
            <el-option label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>查询</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <el-button type="primary" @click="handleCreate"><el-icon><Plus /></el-icon>发起盘点</el-button>
      </div>

      <el-table ref="tableRef" v-loading="loading" :data="tableData" stripe border style="width:100%" highlight-current-row>
        <el-table-column prop="orderNo" label="盘点单号" width="180" />
        <el-table-column label="盘点类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === '全盘' ? 'primary' : row.type === '抽盘' ? 'warning' : 'info'">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '盘点中' ? 'warning' : 'success'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === '盘点中'" link type="warning" @click="handleEdit(row)">录入</el-button>
            <el-button v-if="row.status === '盘点中'" link type="success" @click="handleComplete(row)">完成</el-button>
          </template>
        </el-table-column>
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

    <!-- 发起盘点 -->
    <el-dialog v-model="createVisible" title="发起盘点" width="500px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="盘点类型" prop="type">
          <el-select v-model="createForm.type" placeholder="请选择盘点类型">
            <el-option label="全盘" value="全盘" />
            <el-option label="抽盘" value="抽盘" />
            <el-option label="动态盘" value="动态盘" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="submitCreate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 盘点详情/录入 -->
    <el-dialog v-model="detailVisible" :title="isEditing ? '录入盘点数据' : '盘点详情'" width="1000px">
      <div v-if="currentDetail">
        <el-descriptions :column="3" border style="margin-bottom:16px;">
          <el-descriptions-item label="盘点单号">{{ currentDetail.stockCheck.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="盘点类型">
            <el-tag>{{ currentDetail.stockCheck.type }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentDetail.stockCheck.status === '盘点中' ? 'warning' : 'success'">{{ currentDetail.stockCheck.status }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-table :data="currentDetail.details" stripe border>
          <el-table-column type="index" label="#" width="50" align="center" />
          <el-table-column prop="drugId" label="药品ID" width="100" />
          <el-table-column prop="batchId" label="批次ID" width="100" />
          <el-table-column prop="systemQuantity" label="系统数量" width="110" align="right" />
          <el-table-column label="实际数量" width="140" align="center">
            <template #default="{ row }">
              <el-input-number
                v-if="isEditing"
                v-model="row.actualQuantity"
                size="small"
                :min="0"
                :precision="2"
              />
              <span v-else>{{ row.actualQuantity ?? '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="差异" width="100" align="right">
            <template #default="{ row }">
              <span v-if="row.actualQuantity != null" :style="{ color: row.actualQuantity - row.systemQuantity !== 0 ? '#f56c6c' : '#67c23a' }">
                {{ (row.actualQuantity - row.systemQuantity).toFixed(2) }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="差异原因" min-width="160">
            <template #default="{ row }">
              <el-input
                v-if="isEditing && row.actualQuantity != null && row.actualQuantity - row.systemQuantity !== 0"
                v-model="row.diffReason"
                size="small"
                placeholder="请输入差异原因"
              />
              <span v-else>{{ row.diffReason || '-' }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="isEditing" type="primary" :loading="saveLoading" @click="saveDetails">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getStockCheckPage,
  getStockCheck,
  createStockCheck,
  updateStockCheckDetail,
  completeStockCheck
} from '@/api/inventory'
import { useTableKeyboardNav } from '@/composables/useTableKeyboardNav'

const searchForm = reactive({ type: '', status: '' })
const loading = ref(false)
const tableData = ref([])
const { tableRef, selectFirstRow } = useTableKeyboardNav(tableData)
const pagination = reactive({ current: 1, size: 10, total: 0 })

const createVisible = ref(false)
const createLoading = ref(false)
const createFormRef = ref(null)
const createForm = reactive({ type: '', remark: '' })
const createRules = { type: [{ required: true, message: '请选择盘点类型', trigger: 'change' }] }

const detailVisible = ref(false)
const currentDetail = ref(null)
const isEditing = ref(false)
const saveLoading = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getStockCheckPage({
      pageNum: pagination.current,
      pageSize: pagination.size,
      type: searchForm.type || undefined,
      status: searchForm.status || undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
      selectFirstRow()
    }
  } catch { ElMessage.error('获取数据失败') } finally { loading.value = false }
}

const handleSearch = () => { pagination.current = 1; fetchData() }
const handleReset = () => { searchForm.type = ''; searchForm.status = ''; handleSearch() }

const handleCreate = () => { createVisible.value = true }

const submitCreate = async () => {
  await createFormRef.value.validate(async (valid) => {
    if (!valid) return
    createLoading.value = true
    try {
      const res = await createStockCheck(createForm)
      if (res.code === 200) {
        ElMessage.success('盘点单创建成功，系统已自动生成盘点明细')
        createVisible.value = false
        createForm.type = ''; createForm.remark = ''
        fetchData()
      }
    } catch { ElMessage.error('创建失败') } finally { createLoading.value = false }
  })
}

const handleView = async (row) => {
  isEditing.value = false
  await loadDetail(row.id)
}

const handleEdit = async (row) => {
  isEditing.value = true
  await loadDetail(row.id)
}

const loadDetail = async (id) => {
  try {
    const res = await getStockCheck(id)
    if (res.code === 200) { currentDetail.value = res.data; detailVisible.value = true }
  } catch { ElMessage.error('获取详情失败') }
}

const saveDetails = async () => {
  saveLoading.value = true
  try {
    for (const detail of currentDetail.value.details) {
      if (detail.actualQuantity != null) {
        await updateStockCheckDetail(detail.id, detail.actualQuantity, detail.diffReason)
      }
    }
    ElMessage.success('保存成功')
    detailVisible.value = false
    fetchData()
  } catch { ElMessage.error('保存失败') } finally { saveLoading.value = false }
}

const handleComplete = async (row) => {
  try {
    await ElMessageBox.confirm('完成盘点将把差异同步到库存，确认继续？', '提示', { type: 'warning' })
    const res = await completeStockCheck(row.id)
    if (res.code === 200) { ElMessage.success('盘点完成'); fetchData() }
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

onMounted(() => { fetchData() })
</script>

<style scoped lang="scss">
.stock-check-container {
  padding: 20px;
  .search-card { margin-bottom: 20px; }
  .table-card {
    .table-header { margin-bottom: 16px; }
    .pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
  }
}
</style>
