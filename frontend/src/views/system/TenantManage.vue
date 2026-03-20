<template>
  <div class="tenant-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>租户管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增租户
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="租户名称">
          <el-input v-model="queryForm.name" placeholder="请输入租户名称" clearable @keydown.enter.prevent="loadData" @keydown.up.prevent="handleArrowUp" @keydown.down.prevent="handleArrowDown" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="code" label="租户编码" width="120" />
        <el-table-column prop="name" label="租户名称" min-width="150" />
        <el-table-column prop="creditCode" label="统一社会信用代码" width="200" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="businessMode" label="经营模式" width="100">
          <template #default="{ row }">
            <el-tag :type="row.businessMode === 'chain_store' ? 'success' : 'info'">
              {{ row.businessMode === 'chain_store' ? '连锁' : '单店' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
              {{ row.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'active'" type="warning" link @click="handleDisable(row)">禁用</el-button>
            <el-button v-else type="success" link @click="handleEnable(row)">启用</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="租户编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入租户编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="租户名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入租户名称" />
        </el-form-item>
        <el-form-item label="统一社会信用代码" prop="creditCode">
          <el-input v-model="form.creditCode" placeholder="请输入统一社会信用代码" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="经营模式" prop="businessMode">
          <el-radio-group v-model="form.businessMode">
            <el-radio label="single_store">单店模式</el-radio>
            <el-radio label="chain_store">连锁模式</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getTenantPage, createTenant, updateTenant, deleteTenant, enableTenant, disableTenant } from '@/api/system'
import { useTableKeyboardNav } from '@/composables/useTableKeyboardNav'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const { tableRef, handleArrowUp, handleArrowDown, selectFirstRow } = useTableKeyboardNav(tableData)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)

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
  code: '',
  name: '',
  creditCode: '',
  contactName: '',
  contactPhone: '',
  businessMode: 'single_store'
})

const rules = {
  code: [{ required: true, message: '请输入租户编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入租户名称', trigger: 'blur' }],
  businessMode: [{ required: true, message: '请选择经营模式', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTenantPage({
      current: pagination.current,
      size: pagination.size,
      ...queryForm
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
    selectFirstRow()
  } catch (error) {
    ElMessage.error('加载租户列表失败')
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
  form.code = ''
  form.name = ''
  form.creditCode = ''
  form.contactName = ''
  form.contactPhone = ''
  form.businessMode = 'single_store'
}

const handleAdd = () => {
  resetForm()
  isEdit.value = false
  dialogTitle.value = '新增租户'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  Object.assign(form, row)
  isEdit.value = true
  dialogTitle.value = '编辑租户'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateTenant(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createTenant(form)
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

const handleEnable = async (row) => {
  try {
    await enableTenant(row.id)
    ElMessage.success('启用成功')
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDisable = async (row) => {
  try {
    await ElMessageBox.confirm('确定要禁用该租户吗？禁用后该租户下所有用户将无法登录', '提示', { type: 'warning' })
    await disableTenant(row.id)
    ElMessage.success('禁用成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该租户吗？此操作不可恢复', '警告', { type: 'error' })
    await deleteTenant(row.id)
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
.tenant-manage {
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
