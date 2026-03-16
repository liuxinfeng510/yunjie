<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>数据迁移</span>
          <el-button type="primary" @click="showUploadDialog = true">导入数据</el-button>
        </div>
      </template>

      <!-- 筛选 -->
      <el-form :inline="true" style="margin-bottom: 16px;">
        <el-form-item label="目标模块">
          <el-select v-model="query.targetModule" clearable placeholder="全部模块" style="width: 160px;" @change="loadData">
            <el-option label="药品" value="drug" />
            <el-option label="中药" value="herb" />
            <el-option label="会员" value="member" />
            <el-option label="供应商" value="supplier" />
            <el-option label="库存" value="inventory" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 140px;" @change="loadData">
            <el-option label="待执行" value="pending" />
            <el-option label="执行中" value="processing" />
            <el-option label="已完成" value="completed" />
            <el-option label="失败" value="failed" />
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 任务列表 -->
      <el-table :data="taskList" v-loading="loading" border stripe>
        <el-table-column prop="taskName" label="任务名称" min-width="200" />
        <el-table-column prop="targetModule" label="目标模块" width="100">
          <template #default="{ row }">
            {{ moduleLabels[row.targetModule] || row.targetModule }}
          </template>
        </el-table-column>
        <el-table-column prop="sourceType" label="来源类型" width="90">
          <template #default="{ row }">
            <el-tag size="small">{{ row.sourceType?.toUpperCase() }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="200">
          <template #default="{ row }">
            <div v-if="row.totalCount > 0">
              <el-progress :percentage="Math.round((row.successCount / row.totalCount) * 100)"
                :status="row.status === 'completed' ? 'success' : row.status === 'failed' ? 'exception' : ''" />
              <span style="font-size: 12px; color: #909399;">
                成功 {{ row.successCount }} / 失败 {{ row.failCount }} / 跳过 {{ row.skipCount }} / 共 {{ row.totalCount }}
              </span>
            </div>
            <span v-else style="color: #909399;">--</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'pending' || row.status === 'failed'"
              link type="primary" :loading="row._executing" @click="handleExecute(row)">执行</el-button>
            <el-button v-if="row.errorLog" link type="warning" @click="showError(row)">错误日志</el-button>
            <el-button v-if="row.skipLog || (row.skipCount > 0 && row.status === 'completed')" link type="info" @click="showSkip(row)">跳过日志</el-button>
            <el-popconfirm title="确定删除该任务?" @confirm="handleDelete(row.id)"
              v-if="row.status !== 'processing'">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination style="margin-top: 16px; justify-content: flex-end;" background
        layout="total, sizes, prev, pager, next" :total="total" v-model:current-page="query.current"
        v-model:page-size="query.size" @change="loadData" />
    </el-card>

    <!-- 上传对话框 -->
    <el-dialog v-model="showUploadDialog" title="导入数据" width="500px">
      <el-form label-width="100px">
        <el-form-item label="目标模块" required>
          <el-select v-model="uploadForm.targetModule" placeholder="请选择导入目标">
            <el-option label="药品数据" value="drug" />
            <el-option label="中药数据" value="herb" />
            <el-option label="会员数据" value="member" />
            <el-option label="供应商数据" value="supplier" />
            <el-option label="库存数据" value="inventory" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件类型">
          <el-radio-group v-model="uploadForm.sourceType">
            <el-radio value="excel">Excel (.xlsx)</el-radio>
            <el-radio value="csv">CSV (.csv)</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="上传文件" required>
          <el-upload ref="uploadRef" :auto-upload="false" :limit="1" :on-change="handleFileChange"
            :on-remove="() => uploadFile = null" accept=".xlsx,.xls,.csv" drag>
            <el-icon style="font-size: 40px; color: #909399;"><UploadFilled /></el-icon>
            <div>将文件拖到此处，或<em>点击上传</em></div>
            <template #tip>
              <div style="color: #909399; font-size: 12px;">支持 .xlsx / .xls / .csv 格式，单次导入建议不超过5000条</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="重复处理">
          <el-switch v-model="uploadForm.skipDuplicate" active-text="跳过重复" inactive-text="覆盖已有" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button type="primary" :loading="uploadLoading" @click="handleUpload">上传并创建任务</el-button>
      </template>
    </el-dialog>

    <!-- 错误日志对话框 -->
    <el-dialog v-model="showErrorDialog" title="错误日志" width="600px">
      <pre style="white-space: pre-wrap; word-break: break-all; max-height: 400px; overflow-y: auto; background: #f5f7fa; padding: 12px; border-radius: 4px;">{{ currentErrorLog }}</pre>
    </el-dialog>

    <!-- 跳过日志对话框 -->
    <el-dialog v-model="showSkipDialog" title="跳过日志" width="600px">
      <pre style="white-space: pre-wrap; word-break: break-all; max-height: 400px; overflow-y: auto; background: #fdf6ec; padding: 12px; border-radius: 4px; border: 1px solid #f5dab1;">{{ currentSkipLog }}</pre>
    </el-dialog>

    <!-- 执行结果对话框 -->
    <el-dialog v-model="showResultDialog" title="执行结果" width="550px">
      <el-result
        :icon="executeResult.fail > 0 ? 'warning' : 'success'"
        :title="executeResult.fail > 0 ? '导入完成（部分失败）' : '导入完成'"
      >
        <template #sub-title>
          <div style="font-size: 14px; line-height: 2;">
            总计：<b>{{ executeResult.total }}</b> 条 &nbsp;
            <span style="color: #67c23a;">成功：<b>{{ executeResult.success }}</b></span> &nbsp;
            <span style="color: #909399;">跳过：<b>{{ executeResult.skip }}</b></span> &nbsp;
            <span style="color: #f56c6c;">失败：<b>{{ executeResult.fail }}</b></span>
          </div>
        </template>
      </el-result>
      <div v-if="executeResult.skipLog" style="margin-top: 8px;">
        <div style="font-weight: 600; margin-bottom: 8px; color: #e6a23c;">跳过详情：</div>
        <pre style="white-space: pre-wrap; word-break: break-all; max-height: 200px; overflow-y: auto; background: #fdf6ec; padding: 12px; border-radius: 4px; font-size: 13px; border: 1px solid #f5dab1;">{{ executeResult.skipLog }}</pre>
      </div>
      <div v-if="executeResult.errorLog" style="margin-top: 8px;">
        <div style="font-weight: 600; margin-bottom: 8px; color: #f56c6c;">失败详情：</div>
        <pre style="white-space: pre-wrap; word-break: break-all; max-height: 200px; overflow-y: auto; background: #f5f7fa; padding: 12px; border-radius: 4px; font-size: 13px;">{{ executeResult.errorLog }}</pre>
      </div>
      <template #footer>
        <el-button type="primary" @click="showResultDialog = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMigrationPage, uploadMigrationFile, executeMigration, deleteMigration } from '@/api/system'

const loading = ref(false)
const taskList = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 20, targetModule: '', status: '' })

const showUploadDialog = ref(false)
const uploadLoading = ref(false)
const uploadRef = ref(null)
const uploadFile = ref(null)
const uploadForm = reactive({
  targetModule: 'drug',
  sourceType: 'excel',
  skipDuplicate: true
})

const showErrorDialog = ref(false)
const currentErrorLog = ref('')

const showSkipDialog = ref(false)
const currentSkipLog = ref('')

const showResultDialog = ref(false)
const executeResult = ref({ total: 0, success: 0, fail: 0, skip: 0, errorLog: '', skipLog: '' })

const moduleLabels = { drug: '药品', herb: '中药', member: '会员', supplier: '供应商', inventory: '库存' }

onMounted(() => loadData())

function statusType(status) {
  const map = { pending: 'info', processing: 'warning', completed: 'success', failed: 'danger' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { pending: '待执行', processing: '执行中', completed: '已完成', failed: '失败' }
  return map[status] || status
}

async function loadData() {
  loading.value = true
  try {
    const params = { current: query.current, size: query.size }
    if (query.targetModule) params.targetModule = query.targetModule
    if (query.status) params.status = query.status
    const res = await getMigrationPage(params)
    taskList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    ElMessage.error('加载任务列表失败')
  } finally {
    loading.value = false
  }
}

function handleFileChange(file) {
  uploadFile.value = file.raw
}

async function handleUpload() {
  if (!uploadForm.targetModule) {
    ElMessage.warning('请选择目标模块')
    return
  }
  if (!uploadFile.value) {
    ElMessage.warning('请选择要上传的文件')
    return
  }
  uploadLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', uploadFile.value)
    formData.append('targetModule', uploadForm.targetModule)
    formData.append('sourceType', uploadForm.sourceType)
    formData.append('skipDuplicate', uploadForm.skipDuplicate)
    await uploadMigrationFile(formData)
    ElMessage.success('任务创建成功')
    showUploadDialog.value = false
    uploadFile.value = null
    loadData()
  } catch (e) {
    ElMessage.error('上传失败: ' + (e.response?.data?.message || e.message))
  } finally {
    uploadLoading.value = false
  }
}

async function handleExecute(row) {
  row._executing = true
  try {
    const res = await executeMigration(row.id)
    const task = res.data
    executeResult.value = {
      total: task.totalCount || 0,
      success: task.successCount || 0,
      fail: task.failCount || 0,
      skip: task.skipCount || 0,
      errorLog: task.errorLog || '',
      skipLog: task.skipLog || ''
    }
    showResultDialog.value = true
    loadData()
  } catch (e) {
    ElMessage.error('执行失败: ' + (e.message || '未知错误'))
  } finally {
    row._executing = false
  }
}

function showError(row) {
  currentErrorLog.value = row.errorLog || '无错误信息'
  showErrorDialog.value = true
}

function showSkip(row) {
  currentSkipLog.value = row.skipLog || '无跳过信息'
  showSkipDialog.value = true
}

async function handleDelete(id) {
  try {
    await deleteMigration(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '删除失败')
  }
}
</script>
