<template>
  <div class="knowledge-manage">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>中药知识库</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon> 新增
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query">
        <el-form-item label="中药名称">
          <el-input v-model="query.name" placeholder="请输入中药名称" clearable />
        </el-form-item>
        <el-form-item label="药性">
          <el-select v-model="query.nature" placeholder="全部" clearable style="width: 100px;">
            <el-option label="寒" value="寒" />
            <el-option label="热" value="热" />
            <el-option label="温" value="温" />
            <el-option label="凉" value="凉" />
            <el-option label="平" value="平" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="herbs" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="herbName" label="中药名称" width="120" />
        <el-table-column prop="pinyin" label="拼音" width="100" />
        <el-table-column prop="nature" label="药性" width="60" />
        <el-table-column prop="flavor" label="药味" width="100" />
        <el-table-column prop="meridian" label="归经" width="150" />
        <el-table-column prop="efficacy" label="功效" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="editHerb(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="deleteHerb(row)">删除</el-button>
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
      :title="isEdit ? '编辑中药知识' : '新增中药知识'"
      width="700px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="中药名称" prop="herbName">
              <el-input v-model="form.herbName" placeholder="请输入中药名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="拼音">
              <el-input v-model="form.pinyin" placeholder="请输入拼音" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="别名">
              <el-input v-model="form.alias" placeholder="请输入别名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="拉丁名">
              <el-input v-model="form.latinName" placeholder="请输入拉丁名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="药性" prop="nature">
              <el-select v-model="form.nature" placeholder="请选择" style="width: 100%;">
                <el-option label="寒" value="寒" />
                <el-option label="热" value="热" />
                <el-option label="温" value="温" />
                <el-option label="凉" value="凉" />
                <el-option label="平" value="平" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="药味">
              <el-input v-model="form.flavor" placeholder="如甘、苦" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="归经">
              <el-input v-model="form.meridian" placeholder="如肺、胃" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="最小用量(g)">
              <el-input-number v-model="form.dosageMin" :min="0" :precision="1" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最大用量(g)">
              <el-input-number v-model="form.dosageMax" :min="0" :precision="1" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否有毒">
              <el-switch v-model="form.isToxic" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="功效">
          <el-input v-model="form.efficacy" type="textarea" :rows="2" placeholder="请输入功效" />
        </el-form-item>
        <el-form-item label="主治">
          <el-input v-model="form.indication" type="textarea" :rows="2" placeholder="请输入主治" />
        </el-form-item>
        <el-form-item label="用法用量">
          <el-input v-model="form.dosageUsage" type="textarea" :rows="2" placeholder="请输入用法用量" />
        </el-form-item>
        <el-form-item label="禁忌">
          <el-input v-model="form.contraindication" type="textarea" :rows="2" placeholder="请输入禁忌" />
        </el-form-item>
        <el-form-item label="注意事项">
          <el-input v-model="form.precaution" type="textarea" :rows="2" placeholder="请输入注意事项" />
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
const herbs = ref([])
const total = ref(0)
const query = ref({ pageNum: 1, pageSize: 10, name: '', nature: '' })

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = ref({})

const rules = {
  herbName: [{ required: true, message: '请输入中药名称', trigger: 'blur' }],
  nature: [{ required: true, message: '请选择药性', trigger: 'change' }]
}

function initForm() {
  return {
    herbName: '', pinyin: '', alias: '', latinName: '',
    nature: '', flavor: '', meridian: '',
    dosageMin: null, dosageMax: null, isToxic: false,
    efficacy: '', indication: '', dosageUsage: '',
    contraindication: '', precaution: ''
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/herb-knowledge/page', { params: query.value })
    if (res.code === 200) {
      herbs.value = res.data?.records || []
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

function editHerb(row) {
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
      await request.put(`/herb-knowledge/${form.value.id}`, form.value)
    } else {
      await request.post('/herb-knowledge', form.value)
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

async function deleteHerb(row) {
  try {
    await ElMessageBox.confirm(`确定删除"${row.herbName}"吗?`, '提示', { type: 'warning' })
    await request.delete(`/herb-knowledge/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(loadData)
</script>
