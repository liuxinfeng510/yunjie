<template>
  <div class="staff-manage">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>员工管理</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon> 新增员工
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="姓名/账号">
          <el-input v-model="query.keyword" placeholder="请输入姓名或账号" clearable style="width: 180px;" @keydown.enter.prevent="loadData" @keydown.up.prevent="handleArrowUp" @keydown.down.prevent="handleArrowDown" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.role" placeholder="全部" clearable style="width: 120px;">
            <el-option v-for="r in roles" :key="r.code" :label="r.name" :value="r.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 100px;">
            <el-option label="在职" value="active" />
            <el-option label="离职" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="staffList" v-loading="loading" stripe border>
        <el-table-column prop="username" label="登录账号" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getRoleName(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="storeName" label="所属门店" width="150" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
              {{ row.status === 'active' ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="入职时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="editStaff(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="resetPassword(row)">重置密码</el-button>
            <el-button v-if="row.status === 'active'" type="danger" link size="small" @click="toggleStatus(row)">离职</el-button>
            <el-button v-else type="success" link size="small" @click="toggleStatus(row)">复职</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end;"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑员工' : '新增员工'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="用于登录系统" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%;">
            <el-option v-for="r in roles" :key="r.code" :label="r.name" :value="r.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属门店" prop="storeId">
          <el-select v-model="form.storeId" placeholder="请选择门店" style="width: 100%;">
            <el-option v-for="s in stores" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入初始密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useTableKeyboardNav } from '@/composables/useTableKeyboardNav'

const loading = ref(false)
const submitting = ref(false)
const staffList = ref([])
const { tableRef, handleArrowUp, handleArrowDown, selectFirstRow } = useTableKeyboardNav(staffList)
const stores = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const roles = ref([
  { code: 'admin', name: '管理员' },
  { code: 'manager', name: '店长' },
  { code: 'pharmacist', name: '药师' },
  { code: 'clerk', name: '店员' }
])

const query = ref({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  role: '',
  status: ''
})

const form = ref({
  username: '',
  realName: '',
  phone: '',
  role: 'clerk',
  storeId: null,
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }]
}

function getRoleName(code) {
  return roles.value.find(r => r.code === code)?.name || code
}

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/user/page', { params: query.value })
    if (res.code === 200) {
      staffList.value = res.data.records || []
      total.value = res.data.total || 0
      selectFirstRow()
    }
  } finally {
    loading.value = false
  }
}

async function loadStores() {
  try {
    const res = await request.get('/store/list')
    if (res.code === 200) {
      stores.value = res.data || []
    }
  } catch (e) {}
}

function resetQuery() {
  query.value = { pageNum: 1, pageSize: 10, keyword: '', role: '', status: '' }
  loadData()
}

function showAddDialog() {
  isEdit.value = false
  form.value = { username: '', realName: '', phone: '', role: 'clerk', storeId: null, password: '' }
  dialogVisible.value = true
}

function editStaff(row) {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const url = isEdit.value ? `/user/${form.value.id}` : '/user'
    const method = isEdit.value ? 'put' : 'post'
    const res = await request[method](url, form.value)
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      dialogVisible.value = false
      loadData()
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

async function resetPassword(row) {
  try {
    await ElMessageBox.confirm(`确定重置员工"${row.realName}"的密码吗?`, '提示', { type: 'warning' })
    const res = await request.put(`/user/${row.id}/reset-password`)
    if (res.code === 200) {
      ElMessageBox.alert(`密码已重置为: ${res.data || '123456'}`, '重置成功', { type: 'success' })
    }
  } catch (e) {}
}

async function toggleStatus(row) {
  const action = row.status === 'active' ? '离职' : '复职'
  try {
    await ElMessageBox.confirm(`确定将员工"${row.realName}"设为${action}吗?`, '提示', { type: 'warning' })
    const newStatus = row.status === 'active' ? 'disabled' : 'active'
    const res = await request.put(`/user/${row.id}/status`, { status: newStatus })
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      loadData()
    }
  } catch (e) {}
}

onMounted(() => {
  loadData()
  loadStores()
})
</script>

<style scoped>
.search-form { margin-bottom: 16px; }
</style>
