<template>
  <div class="scale-device">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>电子秤设备管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增设备
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="设备名称">
          <el-input v-model="queryForm.name" placeholder="请输入设备名称" clearable @keydown.enter.prevent="loadData" @keydown.up.prevent="handleArrowUp" @keydown.down.prevent="handleArrowDown" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="在线" value="online" />
            <el-option label="离线" value="offline" />
            <el-option label="故障" value="error" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table ref="tableRef" :data="tableData" v-loading="loading" stripe highlight-current-row>
        <el-table-column prop="deviceCode" label="设备编码" width="120" />
        <el-table-column prop="name" label="设备名称" width="150" />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column prop="model" label="型号" width="100" />
        <el-table-column prop="ipAddress" label="IP地址" width="130" />
        <el-table-column prop="port" label="端口" width="80" />
        <el-table-column prop="precision" label="精度" width="80">
          <template #default="{ row }">{{ row.precision }}g</template>
        </el-table-column>
        <el-table-column prop="maxWeight" label="最大量程" width="100">
          <template #default="{ row }">{{ row.maxWeight }}kg</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastCalibrationDate" label="上次校准" width="110" />
        <el-table-column prop="nextCalibrationDate" label="下次校准" width="110" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleTest(row)">测试</el-button>
            <el-button type="warning" link @click="handleCalibrate(row)">校准</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="设备编码" prop="deviceCode">
          <el-input v-model="form.deviceCode" placeholder="请输入设备编码" />
        </el-form-item>
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-input v-model="form.brand" placeholder="请输入品牌" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="型号" prop="model">
              <el-input v-model="form.model" placeholder="请输入型号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="IP地址" prop="ipAddress">
              <el-input v-model="form.ipAddress" placeholder="请输入IP地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="端口" prop="port">
              <el-input-number v-model="form.port" :min="1" :max="65535" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="精度(g)" prop="precision">
              <el-input-number v-model="form.precision" :min="0.01" :max="10" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大量程(kg)" prop="maxWeight">
              <el-input-number v-model="form.maxWeight" :min="1" :max="1000" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="通讯协议" prop="protocol">
          <el-select v-model="form.protocol" placeholder="请选择" style="width: 100%;">
            <el-option label="TCP" value="TCP" />
            <el-option label="UDP" value="UDP" />
            <el-option label="串口" value="SERIAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 测试结果对话框 -->
    <el-dialog v-model="testDialogVisible" title="设备测试" width="400px">
      <div v-if="testing" class="test-loading">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>正在连接设备...</p>
      </div>
      <div v-else class="test-result">
        <el-result :icon="testResult.success ? 'success' : 'error'" :title="testResult.success ? '连接成功' : '连接失败'">
          <template #sub-title>
            <p v-if="testResult.success">当前读数: {{ testResult.weight }}g</p>
            <p v-else>{{ testResult.message }}</p>
          </template>
        </el-result>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Loading } from '@element-plus/icons-vue'
import { getScaleDevicePage, createScaleDevice, updateScaleDevice, deleteScaleDevice, testScaleDevice, calibrateScaleDevice } from '@/api/device'
import { useTableKeyboardNav } from '@/composables/useTableKeyboardNav'

const loading = ref(false)
const submitting = ref(false)
const testing = ref(false)
const tableData = ref([])
const { tableRef, handleArrowUp, handleArrowDown, selectFirstRow } = useTableKeyboardNav(tableData)
const dialogVisible = ref(false)
const testDialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const testResult = ref({ success: false, message: '', weight: 0 })

const queryForm = reactive({
  name: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

const form = reactive({
  id: null,
  deviceCode: '',
  name: '',
  brand: '',
  model: '',
  ipAddress: '',
  port: 8080,
  precision: 0.1,
  maxWeight: 30,
  protocol: 'TCP',
  remark: ''
})

const rules = {
  deviceCode: [{ required: true, message: '请输入设备编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  ipAddress: [{ required: true, message: '请输入IP地址', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口', trigger: 'blur' }]
}

const statusMap = { online: '在线', offline: '离线', error: '故障' }
const statusColorMap = { online: 'success', offline: 'info', error: 'danger' }
const getStatusName = (status) => statusMap[status] || status
const getStatusColor = (status) => statusColorMap[status] || 'info'

const loadData = async () => {
  loading.value = true
  try {
    const res = await getScaleDevicePage({
      current: pagination.current,
      size: pagination.size,
      ...queryForm
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
    selectFirstRow()
  } catch (error) {
    ElMessage.error('加载设备列表失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryForm.name = ''
  queryForm.status = ''
  pagination.current = 1
  loadData()
}

const resetForm = () => {
  form.id = null
  form.deviceCode = ''
  form.name = ''
  form.brand = ''
  form.model = ''
  form.ipAddress = ''
  form.port = 8080
  form.precision = 0.1
  form.maxWeight = 30
  form.protocol = 'TCP'
  form.remark = ''
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增电子秤设备'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  Object.assign(form, row)
  dialogTitle.value = '编辑电子秤设备'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (form.id) {
      await updateScaleDevice(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createScaleDevice(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleTest = async (row) => {
  testDialogVisible.value = true
  testing.value = true
  try {
    const res = await testScaleDevice(row.id)
    testResult.value = res.data
  } catch (error) {
    testResult.value = { success: false, message: '测试失败', weight: 0 }
  } finally {
    testing.value = false
  }
}

const handleCalibrate = async (row) => {
  try {
    await ElMessageBox.confirm('确定要对该设备进行校准吗？', '提示', { type: 'warning' })
    await calibrateScaleDevice(row.id)
    ElMessage.success('校准指令已发送')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('校准失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该设备吗？', '提示', { type: 'warning' })
    await deleteScaleDevice(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.scale-device {
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
.test-loading {
  text-align: center;
  padding: 40px;
}
.test-loading p {
  margin-top: 20px;
  color: #909399;
}
</style>
