<template>
  <div class="manufacturer-manage">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>全局生产企业资料库</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon> 新增
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" @submit.prevent="loadData">
        <el-form-item label="搜索">
          <el-input v-model="query.keyword" placeholder="企业名称/简称" clearable style="width: 240px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="企业名称" min-width="200" />
        <el-table-column prop="shortName" label="简称" width="120" />
        <el-table-column prop="pinyinShort" label="拼音简码" width="100" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
              {{ row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="editRow(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="deleteRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end;"
        @current-change="loadData"
        @size-change="loadData"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑生产企业' : '新增生产企业'" width="500px">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="企业名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入企业全称" />
        </el-form-item>
        <el-form-item label="简称" prop="shortName">
          <el-input v-model="formData.shortName" placeholder="请输入简称（可选）" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入地址（可选）" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio value="active">启用</el-radio>
            <el-radio value="disabled">停用</el-radio>
          </el-radio-group>
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
import { getManufacturerPage, createManufacturer, updateManufacturer, deleteManufacturer } from '@/api/drug'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const query = ref({ keyword: '', pageNum: 1, pageSize: 20 })

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const formData = ref({ name: '', shortName: '', address: '', status: 'active' })
const editId = ref(null)

const rules = {
  name: [{ required: true, message: '请输入企业名称', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await getManufacturerPage(query.value)
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
  editId.value = null
  formData.value = { name: '', shortName: '', address: '', status: 'active' }
  dialogVisible.value = true
}

function editRow(row) {
  isEdit.value = true
  editId.value = row.id
  formData.value = {
    name: row.name || '',
    shortName: row.shortName || '',
    address: row.address || '',
    status: row.status || 'active'
  }
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    let res
    if (isEdit.value) {
      res = await updateManufacturer(editId.value, formData.value)
    } else {
      res = await createManufacturer(formData.value)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

async function deleteRow(row) {
  try {
    await ElMessageBox.confirm(`确定删除"${row.name}"吗？删除后所有租户将无法使用该企业。`, '提示', { type: 'warning' })
    const res = await deleteManufacturer(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {}
}

onMounted(loadData)
</script>

<style scoped>
.manufacturer-manage {
  padding: 0;
}
</style>
