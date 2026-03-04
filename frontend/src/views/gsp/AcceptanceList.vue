<template>
  <div class="acceptance-list-container">
    <el-card shadow="never">
      <!-- 操作区 -->
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">新增验收记录</el-button>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%; margin-top: 16px"
        border
      >
        <el-table-column prop="drugName" label="商品名称" min-width="150" />
        <el-table-column prop="batchNo" label="批号" width="120" />
        <el-table-column prop="quantity" label="数量" width="100" align="right" />
        <el-table-column prop="appearanceCheck" label="外观检查" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.appearanceCheck === 'PASS' ? 'success' : 'danger'" v-if="row.appearanceCheck">
              {{ row.appearanceCheck === 'PASS' ? '合格' : '不合格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="packageCheck" label="包装检查" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.packageCheck === 'PASS' ? 'success' : 'danger'" v-if="row.packageCheck">
              {{ row.packageCheck === 'PASS' ? '合格' : '不合格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="labelCheck" label="标签检查" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.labelCheck === 'PASS' ? 'success' : 'danger'" v-if="row.labelCheck">
              {{ row.labelCheck === 'PASS' ? '合格' : '不合格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expireCheck" label="效期检查" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.expireCheck === 'PASS' ? 'success' : 'danger'" v-if="row.expireCheck">
              {{ row.expireCheck === 'PASS' ? '合格' : '不合格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="overallResult" label="综合结论" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.overallResult === 'PASS' ? 'success' : 'danger'">
              {{ row.overallResult === 'PASS' ? '合格' : '不合格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="acceptTime" label="验收时间" width="160" />
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
      title="新增验收记录"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="商品名称" prop="drugName">
          <el-input v-model="formData.drugName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="批号" prop="batchNo">
          <el-input v-model="formData.batchNo" placeholder="请输入批号" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId">
          <SupplierSelect
            v-model="formData.supplierName"
            v-model:supplier-id="formData.supplierId"
            placeholder="输入供应商名称/拼音搜索"
          />
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="formData.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="外观检查" prop="appearanceCheck">
          <el-radio-group v-model="formData.appearanceCheck">
            <el-radio value="PASS">合格</el-radio>
            <el-radio value="FAIL">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="包装检查" prop="packageCheck">
          <el-radio-group v-model="formData.packageCheck">
            <el-radio value="PASS">合格</el-radio>
            <el-radio value="FAIL">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标签检查" prop="labelCheck">
          <el-radio-group v-model="formData.labelCheck">
            <el-radio value="PASS">合格</el-radio>
            <el-radio value="FAIL">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="效期检查" prop="expireCheck">
          <el-radio-group v-model="formData.expireCheck">
            <el-radio value="PASS">合格</el-radio>
            <el-radio value="FAIL">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="综合结论" prop="overallResult">
          <el-radio-group v-model="formData.overallResult">
            <el-radio value="PASS">合格</el-radio>
            <el-radio value="FAIL">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="formData.overallResult === 'FAIL'" label="不合格原因" prop="rejectReason">
          <el-input
            v-model="formData.rejectReason"
            type="textarea"
            :rows="3"
            placeholder="请输入不合格原因"
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
import { getAcceptances, createAcceptance } from '@/api/gsp'
import SupplierSelect from '@/components/SupplierSelect.vue'

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
  batchNo: '',
  supplierId: null,
  supplierName: '',
  quantity: 1,
  appearanceCheck: 'PASS',
  packageCheck: 'PASS',
  labelCheck: 'PASS',
  expireCheck: 'PASS',
  overallResult: 'PASS',
  rejectReason: ''
})

const formRules = {
  drugName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  batchNo: [{ required: true, message: '请输入批号', trigger: 'blur' }],
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  appearanceCheck: [{ required: true, message: '请选择外观检查结果', trigger: 'change' }],
  packageCheck: [{ required: true, message: '请选择包装检查结果', trigger: 'change' }],
  labelCheck: [{ required: true, message: '请选择标签检查结果', trigger: 'change' }],
  expireCheck: [{ required: true, message: '请选择效期检查结果', trigger: 'change' }],
  overallResult: [{ required: true, message: '请选择综合结论', trigger: 'change' }]
}

// 查询列表
const handleSearch = async () => {
  try {
    loading.value = true
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.size
    }

    const res = await getAcceptances(params)
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
    batchNo: '',
    supplierId: null,
    supplierName: '',
    quantity: 1,
    appearanceCheck: 'PASS',
    packageCheck: 'PASS',
    labelCheck: 'PASS',
    expireCheck: 'PASS',
    overallResult: 'PASS',
    rejectReason: ''
  })
  dialogVisible.value = true
}

// 提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    // 只发送后端实体需要的字段，排除 supplierName
    const submitData = {
      drugName: formData.drugName,
      batchNo: formData.batchNo,
      supplierId: formData.supplierId,
      quantity: formData.quantity,
      appearanceCheck: formData.appearanceCheck,
      packageCheck: formData.packageCheck,
      labelCheck: formData.labelCheck,
      expireCheck: formData.expireCheck,
      overallResult: formData.overallResult,
      rejectReason: formData.rejectReason || null
    }

    const res = await createAcceptance(submitData)
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
.acceptance-list-container {
  padding: 16px;

  .toolbar {
    margin-bottom: 16px;
  }
}
</style>
