<template>
  <div class="tenant-list">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>租户管理</span>
          <el-button type="primary" @click="$router.push('/admin/tenant/create')">
            <el-icon><Plus /></el-icon> 开通租户
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="租户名称">
          <el-input v-model="query.name" placeholder="请输入租户名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="正常" value="active" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item label="模式">
          <el-select v-model="query.businessMode" placeholder="全部" clearable style="width: 120px;">
            <el-option label="单店" value="single_store" />
            <el-option label="连锁" value="chain" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tenants" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="租户名称" min-width="150" />
        <el-table-column prop="creditCode" label="信用代码" width="180" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="businessMode" label="经营模式" width="100">
          <template #default="{ row }">
            <el-tag :type="row.businessMode === 'chain' ? 'primary' : 'success'" size="small">
              {{ row.businessMode === 'chain' ? '连锁' : '单店' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
              {{ row.status === 'active' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="开通时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="editTenant(row)">编辑</el-button>
            <el-button v-if="row.status === 'active'" type="warning" link size="small" @click="toggleStatus(row, 'disabled')">禁用</el-button>
            <el-button v-else type="success" link size="small" @click="toggleStatus(row, 'active')">启用</el-button>
            <el-button type="danger" link size="small" @click="deleteTenant(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; justify-content: flex-end;"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑租户" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="租户名称">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="信用代码">
          <el-input v-model="editForm.creditCode" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="editForm.contactName" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="editForm.contactPhone" />
        </el-form-item>
        <el-form-item label="经营模式">
          <el-radio-group v-model="editForm.businessMode">
            <el-radio label="single_store">单店模式</el-radio>
            <el-radio label="chain">连锁模式</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTenant">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tenants = ref([])
const total = ref(0)
const query = ref({
  pageNum: 1,
  pageSize: 10,
  name: '',
  status: '',
  businessMode: ''
})

const editDialogVisible = ref(false)
const editForm = ref({})

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/tenant/page', { params: query.value })
    if (res.code === 200) {
      tenants.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.value = { pageNum: 1, pageSize: 10, name: '', status: '', businessMode: '' }
  loadData()
}

function editTenant(row) {
  editForm.value = { ...row }
  editDialogVisible.value = true
}

async function saveTenant() {
  try {
    const res = await request.put(`/tenant/${editForm.value.id}`, editForm.value)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      editDialogVisible.value = false
      loadData()
    }
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

async function toggleStatus(row, newStatus) {
  const action = newStatus === 'active' ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定要${action}租户"${row.name}"吗?`, '提示', { type: 'warning' })
    const res = await request.put(`/tenant/${row.id}/${newStatus === 'active' ? 'enable' : 'disable'}`)
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      loadData()
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(`${action}失败`)
  }
}

async function deleteTenant(row) {
  try {
    await ElMessageBox.confirm(`确定要删除租户"${row.name}"吗? 此操作不可恢复!`, '警告', { type: 'error' })
    const res = await request.delete(`/tenant/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
}
</style>
