<template>
  <div class="incompatibility">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>中药配伍禁忌</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增配伍
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="中药名称">
          <el-input v-model="queryForm.herbName" placeholder="请输入中药名称" clearable />
        </el-form-item>
        <el-form-item label="禁忌类型">
          <el-select v-model="queryForm.incompatibilityType" placeholder="请选择" clearable>
            <el-option label="十八反" value="eighteen_incompatibility" />
            <el-option label="十九畏" value="nineteen_fear" />
            <el-option label="妊娠禁忌" value="pregnancy" />
            <el-option label="配伍禁忌" value="combination" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 快速查询 -->
      <el-alert type="info" :closable="false" style="margin-bottom: 16px;">
        <template #title>
          <span>快速检测配伍：</span>
          <el-select v-model="checkHerb1" placeholder="选择药材1" style="width: 150px; margin-left: 10px;" filterable>
            <el-option v-for="h in herbList" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
          <span style="margin: 0 10px;">+</span>
          <el-select v-model="checkHerb2" placeholder="选择药材2" style="width: 150px;" filterable>
            <el-option v-for="h in herbList" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
          <el-button type="primary" size="small" style="margin-left: 10px;" @click="checkIncompatibility">检测</el-button>
          <span v-if="checkResult" :style="{ marginLeft: '10px', color: checkResult.compatible ? '#67c23a' : '#f56c6c' }">
            {{ checkResult.message }}
          </span>
        </template>
      </el-alert>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="herb1Name" label="药材1" width="120" />
        <el-table-column prop="herb2Name" label="药材2" width="120" />
        <el-table-column prop="incompatibilityType" label="禁忌类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.incompatibilityType)">{{ getTypeName(row.incompatibilityType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="severity" label="严重程度" width="100">
          <template #default="{ row }">
            <el-tag :type="row.severity === 'high' ? 'danger' : row.severity === 'medium' ? 'warning' : 'info'">
              {{ row.severity === 'high' ? '高' : row.severity === 'medium' ? '中' : '低' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="禁忌说明" min-width="200" show-overflow-tooltip />
        <el-table-column prop="reference" label="参考来源" width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="药材1" prop="herb1Id">
          <el-select v-model="form.herb1Id" placeholder="请选择药材" filterable style="width: 100%;">
            <el-option v-for="h in herbList" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="药材2" prop="herb2Id">
          <el-select v-model="form.herb2Id" placeholder="请选择药材" filterable style="width: 100%;">
            <el-option v-for="h in herbList" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="禁忌类型" prop="incompatibilityType">
          <el-select v-model="form.incompatibilityType" placeholder="请选择" style="width: 100%;">
            <el-option label="十八反" value="eighteen_incompatibility" />
            <el-option label="十九畏" value="nineteen_fear" />
            <el-option label="妊娠禁忌" value="pregnancy" />
            <el-option label="配伍禁忌" value="combination" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重程度" prop="severity">
          <el-radio-group v-model="form.severity">
            <el-radio label="high">高</el-radio>
            <el-radio label="medium">中</el-radio>
            <el-radio label="low">低</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="禁忌说明" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入禁忌说明" />
        </el-form-item>
        <el-form-item label="参考来源" prop="reference">
          <el-input v-model="form.reference" placeholder="请输入参考来源" />
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
import { getIncompatibilityPage, createIncompatibility, updateIncompatibility, deleteIncompatibility, checkHerbIncompatibility } from '@/api/herb'
import { getHerbList } from '@/api/herb'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const herbList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const checkHerb1 = ref(null)
const checkHerb2 = ref(null)
const checkResult = ref(null)

const queryForm = reactive({
  herbName: '',
  incompatibilityType: ''
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

const form = reactive({
  id: null,
  herb1Id: null,
  herb2Id: null,
  incompatibilityType: '',
  severity: 'medium',
  description: '',
  reference: ''
})

const rules = {
  herb1Id: [{ required: true, message: '请选择药材1', trigger: 'change' }],
  herb2Id: [{ required: true, message: '请选择药材2', trigger: 'change' }],
  incompatibilityType: [{ required: true, message: '请选择禁忌类型', trigger: 'change' }],
  severity: [{ required: true, message: '请选择严重程度', trigger: 'change' }]
}

const typeMap = {
  eighteen_incompatibility: '十八反',
  nineteen_fear: '十九畏',
  pregnancy: '妊娠禁忌',
  combination: '配伍禁忌'
}

const typeColorMap = {
  eighteen_incompatibility: 'danger',
  nineteen_fear: 'warning',
  pregnancy: 'info',
  combination: ''
}

const getTypeName = (type) => typeMap[type] || type
const getTypeColor = (type) => typeColorMap[type] || ''

const loadData = async () => {
  loading.value = true
  try {
    const res = await getIncompatibilityPage({
      current: pagination.current,
      size: pagination.size,
      ...queryForm
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadHerbList = async () => {
  try {
    const res = await getHerbList()
    herbList.value = res.data
  } catch (error) {
    console.error('加载中药列表失败', error)
  }
}

const resetQuery = () => {
  queryForm.herbName = ''
  queryForm.incompatibilityType = ''
  pagination.current = 1
  loadData()
}

const resetForm = () => {
  form.id = null
  form.herb1Id = null
  form.herb2Id = null
  form.incompatibilityType = ''
  form.severity = 'medium'
  form.description = ''
  form.reference = ''
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增配伍禁忌'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  Object.assign(form, row)
  dialogTitle.value = '编辑配伍禁忌'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (form.herb1Id === form.herb2Id) {
    ElMessage.warning('请选择两种不同的药材')
    return
  }

  submitting.value = true
  try {
    if (form.id) {
      await updateIncompatibility(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createIncompatibility(form)
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

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该配伍禁忌记录吗？', '提示', { type: 'warning' })
    await deleteIncompatibility(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const checkIncompatibility = async () => {
  if (!checkHerb1.value || !checkHerb2.value) {
    ElMessage.warning('请选择两种药材')
    return
  }
  try {
    const res = await checkHerbIncompatibility(checkHerb1.value, checkHerb2.value)
    checkResult.value = res.data
  } catch (error) {
    ElMessage.error('检测失败')
  }
}

onMounted(() => {
  loadData()
  loadHerbList()
})
</script>

<style scoped>
.incompatibility {
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
