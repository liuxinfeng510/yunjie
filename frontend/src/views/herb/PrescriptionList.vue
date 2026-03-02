<template>
  <div class="prescription-list-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="处方号">
          <el-input
            v-model="searchForm.prescriptionNo"
            placeholder="请输入处方号"
            clearable
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="患者姓名">
          <el-input
            v-model="searchForm.patientName"
            placeholder="请输入患者姓名"
            clearable
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="开方医师">
          <el-input
            v-model="searchForm.doctorName"
            placeholder="请输入医师姓名"
            clearable
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="处方状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            @clear="handleSearch"
          >
            <el-option label="待调配" value="待调配" />
            <el-option label="调配中" value="调配中" />
            <el-option label="待复核" value="待复核" />
            <el-option label="已完成" value="已完成" />
            <el-option label="已取消" value="已取消" />
          </el-select>
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
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增处方
        </el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="prescriptionNo" label="处方号" width="180" />
        <el-table-column prop="patientName" label="患者姓名" width="120" />
        <el-table-column prop="diagnosis" label="诊断" width="200" show-overflow-tooltip />
        <el-table-column prop="dosageCount" label="剂数" width="80" align="center" />
        <el-table-column prop="doctorName" label="开方医师" width="100" />
        <el-table-column prop="dispenserName" label="调配人" width="100" />
        <el-table-column prop="reviewerName" label="复核人" width="100" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">
              查看详情
            </el-button>
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

    <!-- 处方详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="处方详情"
      width="900px"
      @close="handleDetailDialogClose"
    >
      <div v-if="currentPrescription" class="prescription-detail">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="处方号">
            {{ currentPrescription.prescriptionNo }}
          </el-descriptions-item>
          <el-descriptions-item label="患者姓名">
            {{ currentPrescription.patientName }}
          </el-descriptions-item>
          <el-descriptions-item label="性别/年龄">
            {{ currentPrescription.patientGender }} / {{ currentPrescription.patientAge }}岁
          </el-descriptions-item>
          <el-descriptions-item label="诊断" :span="3">
            {{ currentPrescription.diagnosis }}
          </el-descriptions-item>
          <el-descriptions-item label="开方医师">
            {{ currentPrescription.doctorName }}
          </el-descriptions-item>
          <el-descriptions-item label="剂数">
            {{ currentPrescription.dosageCount }}剂
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentPrescription.status)">
              {{ currentPrescription.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="调配人">
            {{ currentPrescription.dispenserName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="复核人">
            {{ currentPrescription.reviewerName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ currentPrescription.createdTime }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">
          <span style="font-weight: 600; font-size: 16px">处方明细</span>
        </el-divider>

        <el-table
          :data="currentPrescription.items"
          stripe
          border
          style="width: 100%"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="herbName" label="药材名称" width="150" />
          <el-table-column label="每剂用量" width="120">
            <template #default="{ row }">
              {{ row.dosagePerUnit }}g
            </template>
          </el-table-column>
          <el-table-column prop="specialProcess" label="特殊处理" width="150">
            <template #default="{ row }">
              <el-tag v-if="row.specialProcess" type="warning" size="small">
                {{ row.specialProcess }}
              </el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="cellPosition" label="斗格位置" width="120" />
          <el-table-column label="总用量" width="120">
            <template #default="{ row }">
              {{ (row.dosagePerUnit * currentPrescription.dosageCount).toFixed(1) }}g
            </template>
          </el-table-column>
          <el-table-column prop="remarks" label="备注" min-width="150" show-overflow-tooltip />
        </el-table>

        <div v-if="currentPrescription.remarks" style="margin-top: 16px">
          <el-divider content-position="left">处方备注</el-divider>
          <div style="padding: 12px; background-color: #f5f7fa; border-radius: 4px">
            {{ currentPrescription.remarks }}
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="currentPrescription?.status === '待调配'"
          type="primary"
          @click="handleStartDispense"
        >
          开始调配
        </el-button>
        <el-button
          v-if="currentPrescription?.status === '待复核'"
          type="success"
          @click="handleReview"
        >
          复核通过
        </el-button>
      </template>
    </el-dialog>

    <!-- 新增处方对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="新增处方"
      width="800px"
      @close="handleAddDialogClose"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="患者姓名" prop="patientName">
              <el-input v-model="formData.patientName" placeholder="请输入患者姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="patientGender">
              <el-select v-model="formData.patientGender" placeholder="请选择性别">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="年龄" prop="patientAge">
              <el-input-number
                v-model="formData.patientAge"
                :min="0"
                :max="150"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="剂数" prop="dosageCount">
              <el-input-number
                v-model="formData.dosageCount"
                :min="1"
                :max="100"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="诊断" prop="diagnosis">
          <el-input
            v-model="formData.diagnosis"
            type="textarea"
            :rows="2"
            placeholder="请输入诊断"
          />
        </el-form-item>

        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="formData.remarks"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getPrescriptionPage,
  getPrescription,
  createPrescription
} from '@/api/herb'

// 搜索表单
const searchForm = reactive({
  prescriptionNo: '',
  patientName: '',
  doctorName: '',
  status: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 详情对话框
const detailDialogVisible = ref(false)
const currentPrescription = ref(null)

// 新增对话框
const addDialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)

const formData = reactive({
  patientName: '',
  patientGender: '',
  patientAge: 0,
  diagnosis: '',
  dosageCount: 7,
  remarks: ''
})

const formRules = {
  patientName: [{ required: true, message: '请输入患者姓名', trigger: 'blur' }],
  patientGender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  patientAge: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
  diagnosis: [{ required: true, message: '请输入诊断', trigger: 'blur' }],
  dosageCount: [{ required: true, message: '请输入剂数', trigger: 'blur' }]
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    待调配: 'info',
    调配中: 'warning',
    待复核: 'primary',
    已完成: 'success',
    已取消: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取列表数据
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    const res = await getPrescriptionPage(params)
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
  searchForm.prescriptionNo = ''
  searchForm.patientName = ''
  searchForm.doctorName = ''
  searchForm.status = ''
  handleSearch()
}

// 新增
const handleAdd = () => {
  addDialogVisible.value = true
}

// 查看详情
const handleViewDetail = async (row) => {
  try {
    const res = await getPrescription(row.id)
    if (res.code === 200) {
      currentPrescription.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

// 开始调配
const handleStartDispense = () => {
  ElMessage.success('已开始调配')
  detailDialogVisible.value = false
  fetchData()
}

// 复核
const handleReview = () => {
  ElMessage.success('复核通过')
  detailDialogVisible.value = false
  fetchData()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      const res = await createPrescription(formData)
      if (res.code === 200) {
        ElMessage.success('新增成功')
        addDialogVisible.value = false
        fetchData()
      }
    } catch (error) {
      ElMessage.error('新增失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 关闭详情对话框
const handleDetailDialogClose = () => {
  currentPrescription.value = null
}

// 关闭新增对话框
const handleAddDialogClose = () => {
  Object.assign(formData, {
    patientName: '',
    patientGender: '',
    patientAge: 0,
    diagnosis: '',
    dosageCount: 7,
    remarks: ''
  })
  formRef.value?.clearValidate()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.prescription-list-container {
  padding: 20px;

  .search-card {
    margin-bottom: 20px;
  }

  .table-card {
    .table-header {
      margin-bottom: 16px;
    }

    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .prescription-detail {
    .el-divider {
      margin: 20px 0;
    }
  }
}
</style>
