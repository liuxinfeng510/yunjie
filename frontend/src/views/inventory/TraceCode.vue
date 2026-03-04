<template>
  <div class="trace-code">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>药品追溯码管理</span>
          <div>
            <el-button type="primary" @click="handleScan">
              <el-icon><Camera /></el-icon>扫码录入
            </el-button>
            <el-button type="success" @click="handleExport">导出报表</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="追溯码">
          <el-input v-model="queryForm.traceCode" placeholder="请输入追溯码" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="queryForm.drugName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="追溯状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="已激活" value="activated" />
            <el-option label="已核销" value="used" />
            <el-option label="已召回" value="recalled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="traceCode" label="追溯码" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleQuery(row.traceCode)">{{ row.traceCode }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="drugName" label="商品名称" min-width="150" />
        <el-table-column prop="specification" label="规格" width="100" />
        <el-table-column prop="batchNo" label="批号" width="120" />
        <el-table-column prop="manufacturer" label="生产企业" width="150" show-overflow-tooltip />
        <el-table-column prop="packageLevel" label="包装级别" width="100">
          <template #default="{ row }">
            <el-tag :type="row.packageLevel === 'box' ? 'primary' : row.packageLevel === 'case' ? 'success' : 'info'">
              {{ getPackageLevelName(row.packageLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="activatedAt" label="激活时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleTrace(row)">追溯</el-button>
            <el-button type="warning" link @click="handlePrint(row)">打印</el-button>
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

    <!-- 扫码对话框 -->
    <el-dialog v-model="scanDialogVisible" title="扫码录入" width="500px">
      <el-form ref="scanFormRef" :model="scanForm" :rules="scanRules" label-width="100px">
        <el-form-item label="追溯码" prop="traceCode">
          <el-input v-model="scanForm.traceCode" placeholder="请扫描或输入追溯码" @keyup.enter="handleScanSubmit">
            <template #suffix>
              <el-icon><Camera /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="关联药品" prop="drugId">
          <el-select v-model="scanForm.drugId" placeholder="请选择药品" filterable style="width: 100%;">
            <el-option v-for="d in drugList" :key="d.id" :label="`${d.name} - ${d.specification}`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="批号" prop="batchNo">
          <el-input v-model="scanForm.batchNo" placeholder="请输入批号" />
        </el-form-item>
        <el-form-item label="包装级别" prop="packageLevel">
          <el-radio-group v-model="scanForm.packageLevel">
            <el-radio label="piece">最小单位</el-radio>
            <el-radio label="box">盒</el-radio>
            <el-radio label="case">箱</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scanDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleScanSubmit" :loading="submitting">确认录入</el-button>
      </template>
    </el-dialog>

    <!-- 追溯详情对话框 -->
    <el-dialog v-model="traceDialogVisible" title="追溯详情" width="800px">
      <el-descriptions :column="2" border style="margin-bottom: 20px;">
        <el-descriptions-item label="追溯码">{{ traceDetail.traceCode }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ traceDetail.drugName }}</el-descriptions-item>
        <el-descriptions-item label="生产企业">{{ traceDetail.manufacturer }}</el-descriptions-item>
        <el-descriptions-item label="批号">{{ traceDetail.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="生产日期">{{ traceDetail.productionDate }}</el-descriptions-item>
        <el-descriptions-item label="有效期至">{{ traceDetail.expiryDate }}</el-descriptions-item>
      </el-descriptions>
      <el-divider>流转记录</el-divider>
      <el-timeline>
        <el-timeline-item v-for="(item, index) in traceDetail.flowLogs" :key="index" :timestamp="item.time" :type="item.type">
          <el-card>
            <h4>{{ item.action }}</h4>
            <p>操作企业：{{ item.company }}</p>
            <p>操作人员：{{ item.operator }}</p>
            <p v-if="item.remark">备注：{{ item.remark }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { getTraceCodePage, createTraceCode, getTraceDetail, queryByCode } from '@/api/batch'
import { getDrugList } from '@/api/drug'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const drugList = ref([])
const scanDialogVisible = ref(false)
const traceDialogVisible = ref(false)
const scanFormRef = ref(null)
const traceDetail = ref({})

const queryForm = reactive({
  traceCode: '',
  drugName: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

const scanForm = reactive({
  traceCode: '',
  drugId: null,
  batchNo: '',
  packageLevel: 'box'
})

const scanRules = {
  traceCode: [{ required: true, message: '请输入追溯码', trigger: 'blur' }],
  drugId: [{ required: true, message: '请选择药品', trigger: 'change' }]
}

const statusMap = { activated: '已激活', used: '已核销', recalled: '已召回' }
const statusColorMap = { activated: 'success', used: 'info', recalled: 'danger' }
const packageLevelMap = { piece: '最小单位', box: '盒', case: '箱' }

const getStatusName = (status) => statusMap[status] || status
const getStatusColor = (status) => statusColorMap[status] || 'info'
const getPackageLevelName = (level) => packageLevelMap[level] || level

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTraceCodePage({
      current: pagination.current,
      size: pagination.size,
      ...queryForm
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载追溯码列表失败')
  } finally {
    loading.value = false
  }
}

const loadDrugList = async () => {
  try {
    const res = await getDrugList()
    drugList.value = res.data
  } catch (error) {
    console.error('加载药品列表失败', error)
  }
}

const resetQuery = () => {
  queryForm.traceCode = ''
  queryForm.drugName = ''
  queryForm.status = ''
  pagination.current = 1
  loadData()
}

const handleScan = () => {
  scanForm.traceCode = ''
  scanForm.drugId = null
  scanForm.batchNo = ''
  scanForm.packageLevel = 'box'
  scanDialogVisible.value = true
}

const handleScanSubmit = async () => {
  const valid = await scanFormRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createTraceCode(scanForm)
    ElMessage.success('录入成功')
    scanDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '录入失败')
  } finally {
    submitting.value = false
  }
}

const handleQuery = async (code) => {
  try {
    const res = await queryByCode(code)
    traceDetail.value = res.data
    traceDialogVisible.value = true
  } catch (error) {
    ElMessage.error('查询追溯信息失败')
  }
}

const handleTrace = async (row) => {
  try {
    const res = await getTraceDetail(row.id)
    traceDetail.value = res.data
    traceDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取追溯详情失败')
  }
}

const handlePrint = (row) => {
  ElMessage.info('打印功能开发中')
}

const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

onMounted(() => {
  loadData()
  loadDrugList()
})
</script>

<style scoped>
.trace-code {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-form {
  margin-bottom: 16px;
}
</style>
