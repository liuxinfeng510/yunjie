<template>
  <div class="incompatibility-manage">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>配伍禁忌管理</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon> 新增
          </el-button>
        </div>
      </template>

      <el-alert type="info" :closable="false" style="margin-bottom: 16px;">
        配伍禁忌数据为全局公共数据，修改后将影响所有租户的处方校验。
      </el-alert>

      <el-form :inline="true" :model="query">
        <el-form-item label="药材名称">
          <el-input v-model="query.herbName" placeholder="请输入药材名称" clearable />
        </el-form-item>
        <el-form-item label="禁忌类型">
          <el-select v-model="query.type" placeholder="全部" clearable style="width: 120px;">
            <el-option label="十八反" value="18_oppose" />
            <el-option label="十九畏" value="19_fear" />
            <el-option label="妊娠禁忌" value="pregnancy" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="herbA" label="药材1" width="120" />
        <el-table-column prop="herbB" label="药材2" width="120" />
        <el-table-column prop="type" label="禁忌类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)" size="small">{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="severity" label="严重程度" width="100">
          <template #default="{ row }">
            <el-tag :type="row.severity === 'forbidden' ? 'danger' : 'warning'" size="small">
              {{ row.severity === 'forbidden' ? '禁用' : '慎用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="editItem(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="deleteItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end;"
        @current-change="loadData"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑配伍禁忌' : '新增配伍禁忌'"
      width="550px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="药材1" prop="herbA">
          <el-input v-model="form.herbA" placeholder="请输入药材名称" />
        </el-form-item>
        <el-form-item label="药材2" prop="herbB">
          <el-input v-model="form.herbB" placeholder="请输入药材名称" />
        </el-form-item>
        <el-form-item label="禁忌类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择" style="width: 100%;">
            <el-option label="十八反" value="18_oppose" />
            <el-option label="十九畏" value="19_fear" />
            <el-option label="妊娠禁忌" value="pregnancy" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重程度" prop="severity">
          <el-select v-model="form.severity" placeholder="请选择" style="width: 100%;">
            <el-option label="禁用" value="forbidden" />
            <el-option label="慎用" value="caution" />
          </el-select>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入禁忌说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const query = ref({ pageNum: 1, pageSize: 10, herbName: '', type: '' })

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = ref({})

const rules = {
  herbA: [{ required: true, message: '请输入药材1', trigger: 'blur' }],
  herbB: [{ required: true, message: '请输入药材2', trigger: 'blur' }],
  type: [{ required: true, message: '请选择禁忌类型', trigger: 'change' }],
  severity: [{ required: true, message: '请选择严重程度', trigger: 'change' }]
}

function initForm() {
  return { herbA: '', herbB: '', type: '', severity: '', description: '' }
}

function getTypeLabel(type) {
  const map = { '18_oppose': '十八反', '19_fear': '十九畏', pregnancy: '妊娠禁忌' }
  return map[type] || type
}

function getTypeTag(type) {
  const map = { '18_oppose': 'danger', '19_fear': 'warning', pregnancy: 'info' }
  return map[type] || ''
}

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/herb-incompatibility/page', { params: query.value })
    if (res.code === 200) {
      list.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } finally {
    loading.value = false
  }
}

function showAddDialog() {
  isEdit.value = false
  form.value = initForm()
  dialogVisible.value = true
}

function editItem(row) {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

async function submitForm() {
  try {
    await formRef.value.validate()
  } catch { return }
  saving.value = true
  try {
    if (isEdit.value) {
      await request.put(`/herb-incompatibility/${form.value.id}`, form.value)
    } else {
      await request.post('/herb-incompatibility', form.value)
    }
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

async function deleteItem(row) {
  try {
    await ElMessageBox.confirm('确定删除此配伍禁忌吗?', '提示', { type: 'warning' })
    await request.delete(`/herb-incompatibility/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(loadData)
</script>
