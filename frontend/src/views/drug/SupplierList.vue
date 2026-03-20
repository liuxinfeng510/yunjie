<template>
  <div class="supplier-list-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" style="margin-bottom: 16px;">
      <el-form :model="searchForm" inline>
        <el-form-item label="供应商名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入供应商名称"
            clearable
            style="width: 200px;"
            @keydown.enter.prevent="handleSearch"
            @keydown.up.prevent="handleArrowUp"
            @keydown.down.prevent="handleArrowDown"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px;">
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">查询</el-button>
          <el-button @click="handleReset" :icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 工具栏和表格 -->
    <el-card shadow="never">
      <div style="margin-bottom: 16px;">
        <el-button type="primary" @click="handleAdd" :icon="Plus">新增供应商</el-button>
      </div>

      <!-- 表格 -->
      <el-table ref="tableRef" :data="tableData" v-loading="loading" border stripe highlight-current-row>
        <el-table-column prop="name" label="供应商名称" width="200" show-overflow-tooltip />
        <el-table-column prop="shortName" label="简称" width="150" />
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column prop="creditCode" label="统一社会信用代码" width="200" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'">
              {{ row.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)" :icon="Edit">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)" :icon="Delete">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="140px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入供应商名称" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="简称" prop="shortName">
              <el-input v-model="formData.shortName" placeholder="请输入简称" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="统一社会信用代码" prop="creditCode">
          <el-input v-model="formData.creditCode" placeholder="请输入18位统一社会信用代码" maxlength="18" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactName">
              <el-input v-model="formData.contactName" placeholder="请输入联系人" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" maxlength="20" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="地址" prop="address">
          <el-input
            v-model="formData.address"
            type="textarea"
            :rows="2"
            placeholder="请输入详细地址"
            maxlength="200"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开户银行" prop="bankName">
              <el-input v-model="formData.bankName" placeholder="请输入开户银行" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="银行账号" prop="bankAccount">
              <el-input v-model="formData.bankAccount" placeholder="请输入银行账号" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { useTableKeyboardNav } from '@/composables/useTableKeyboardNav'
import {
  getSupplierPage,
  getSupplier,
  createSupplier,
  updateSupplier,
  deleteSupplier
} from '@/api/drug'

// 搜索表单
const searchForm = reactive({
  name: '',
  status: ''
})

// 表格数据
const tableData = ref([])
const loading = ref(false)
const { tableRef, handleArrowUp, handleArrowDown, selectFirstRow } = useTableKeyboardNav(tableData)

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增供应商')
const formRef = ref(null)
const submitLoading = ref(false)

// 表单数据
const formData = reactive({
  id: null,
  name: '',
  shortName: '',
  creditCode: '',
  contactName: '',
  contactPhone: '',
  address: '',
  bankName: '',
  bankAccount: '',
  remark: ''
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入供应商名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在2到100个字符', trigger: 'blur' }
  ],
  shortName: [
    { max: 50, message: '长度不能超过50个字符', trigger: 'blur' }
  ],
  creditCode: [
    { required: true, message: '请输入统一社会信用代码', trigger: 'blur' },
    { pattern: /^[0-9A-Z]{18}$/, message: '请输入正确的18位统一社会信用代码', trigger: 'blur' }
  ],
  contactName: [
    { required: true, message: '请输入联系人', trigger: 'blur' },
    { max: 50, message: '长度不能超过50个字符', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$|^0\d{2,3}-?\d{7,8}$/, message: '请输入正确的电话号码', trigger: 'blur' }
  ],
  address: [
    { max: 200, message: '长度不能超过200个字符', trigger: 'blur' }
  ],
  bankName: [
    { max: 100, message: '长度不能超过100个字符', trigger: 'blur' }
  ],
  bankAccount: [
    { pattern: /^\d{10,30}$/, message: '请输入正确的银行账号(10-30位数字)', trigger: 'blur' }
  ]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    const res = await getSupplierPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
      selectFirstRow()
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.name = ''
  searchForm.status = ''
  pagination.current = 1
  loadData()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增供应商'
  resetFormData()
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  dialogTitle.value = '编辑供应商'
  try {
    const res = await getSupplier(row.id)
    if (res.code === 200) {
      Object.assign(formData, res.data)
      dialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载供应商信息失败')
  }
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该供应商吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteSupplier(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadData()
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const res = formData.id
          ? await updateSupplier(formData.id, formData)
          : await createSupplier(formData)

        if (res.code === 200) {
          ElMessage.success(formData.id ? '更新成功' : '新增成功')
          dialogVisible.value = false
          loadData()
        }
      } catch (error) {
        ElMessage.error(formData.id ? '更新失败' : '新增失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields()
  resetFormData()
}

// 重置表单数据
const resetFormData = () => {
  formData.id = null
  formData.name = ''
  formData.shortName = ''
  formData.creditCode = ''
  formData.contactName = ''
  formData.contactPhone = ''
  formData.address = ''
  formData.bankName = ''
  formData.bankAccount = ''
  formData.remark = ''
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.supplier-list-container {
  padding: 16px;
}
</style>
