<template>
  <div class="maintenance-list-container">
    <el-card shadow="never">
      <!-- 操作区 -->
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">新增养护记录</el-button>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%; margin-top: 16px"
        border
      >
        <el-table-column prop="drugName" label="药品名称" min-width="150" />
        <el-table-column prop="maintenanceType" label="养护类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getMaintenanceTypeTag(row.maintenanceType)">
              {{ getMaintenanceTypeLabel(row.maintenanceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="appearanceCheck" label="外观检查" min-width="150" show-overflow-tooltip />
        <el-table-column prop="result" label="综合结果" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.result === 'NORMAL' ? 'success' : 'danger'">
              {{ row.result === 'NORMAL' ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handleMeasure" label="处理措施" min-width="150" show-overflow-tooltip />
        <el-table-column prop="maintenanceTime" label="养护时间" width="160" />
        <el-table-column prop="nextMaintenanceTime" label="下次养护时间" width="160" />
        <el-table-column prop="maintainerName" label="养护人" width="100" />
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="handleSearch"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="新增养护记录"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="药品名称" prop="drugName">
          <el-input v-model="formData.drugName" placeholder="请输入药品名称" />
        </el-form-item>
        <el-form-item label="养护类型" prop="maintenanceType">
          <el-select v-model="formData.maintenanceType" placeholder="请选择" style="width: 100%">
            <el-option label="日常养护" value="DAILY" />
            <el-option label="重点养护" value="KEY" />
            <el-option label="特殊养护" value="SPECIAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="外观检查" prop="appearanceCheck">
          <el-input
            v-model="formData.appearanceCheck"
            type="textarea"
            :rows="3"
            placeholder="请输入外观检查情况"
          />
        </el-form-item>
        <el-form-item label="包装检查" prop="packageCheck">
          <el-input
            v-model="formData.packageCheck"
            type="textarea"
            :rows="2"
            placeholder="请输入包装检查情况"
          />
        </el-form-item>
        <el-form-item label="温湿度情况" prop="tempHumidity">
          <el-input v-model="formData.tempHumidity" placeholder="例如：温度20℃，湿度60%" />
        </el-form-item>
        <el-form-item label="综合结果" prop="result">
          <el-radio-group v-model="formData.result">
            <el-radio value="NORMAL">正常</el-radio>
            <el-radio value="ABNORMAL">异常</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理措施" prop="handleMeasure">
          <el-input
            v-model="formData.handleMeasure"
            type="textarea"
            :rows="3"
            placeholder="如有异常，请输入处理措施"
          />
        </el-form-item>
        <el-form-item label="下次养护时间" prop="nextMaintenanceTime">
          <el-date-picker
            v-model="formData.nextMaintenanceTime"
            type="datetime"
            placeholder="请选择下次养护时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMaintenances, createMaintenance } from '@/api/gsp'

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const formRef = ref(null)
const formData = reactive({
  drugName: '',
  maintenanceType: 'DAILY',
  appearanceCheck: '',
  packageCheck: '',
  tempHumidity: '',
  result: 'NORMAL',
  handleMeasure: '',
  nextMaintenanceTime: '',
  remark: ''
})

const formRules = {
  drugName: [{ required: true, message: '请输入药品名称', trigger: 'blur' }],
  maintenanceType: [{ required: true, message: '请选择养护类型', trigger: 'change' }],
  appearanceCheck: [{ required: true, message: '请输入外观检查情况', trigger: 'blur' }],
  result: [{ required: true, message: '请选择综合结果', trigger: 'change' }],
  nextMaintenanceTime: [{ required: true, message: '请选择下次养护时间', trigger: 'change' }]
}

// 养护类型映射
const maintenanceTypeMap = {
  DAILY: { label: '日常养护', type: '' },
  KEY: { label: '重点养护', type: 'warning' },
  SPECIAL: { label: '特殊养护', type: 'danger' }
}

const getMaintenanceTypeLabel = (type) => {
  return maintenanceTypeMap[type]?.label || type
}

const getMaintenanceTypeTag = (type) => {
  return maintenanceTypeMap[type]?.type || ''
}

// 查询列表
const handleSearch = async () => {
  try {
    loading.value = true
    const params = {
      current: pagination.current,
      size: pagination.size
    }

    const res = await getMaintenances(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('查询失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 新增
const handleAdd = () => {
  Object.assign(formData, {
    drugName: '',
    maintenanceType: 'DAILY',
    appearanceCheck: '',
    packageCheck: '',
    tempHumidity: '',
    result: 'NORMAL',
    handleMeasure: '',
    nextMaintenanceTime: '',
    remark: ''
  })
  dialogVisible.value = true
}

// 提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    const res = await createMaintenance(formData)
    if (res.code === 200) {
      ElMessage.success('新增成功')
      dialogVisible.value = false
      handleSearch()
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败：' + error.message)
    }
  }
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped lang="scss">
.maintenance-list-container {
  padding: 16px;

  .toolbar {
    margin-bottom: 16px;
  }
}
</style>
