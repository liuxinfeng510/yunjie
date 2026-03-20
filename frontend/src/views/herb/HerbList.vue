<template>
  <div class="herb-list-container">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="药材名称/拼音">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入药材名称或拼音"
            clearable
            @clear="handleSearch"
            @keydown.enter.prevent="handleSearch"
            @keydown.up.prevent="handleArrowUp"
            @keydown.down.prevent="handleArrowDown"
          />
        </el-form-item>
        <el-form-item label="药材类别">
          <el-select
            v-model="searchForm.category"
            placeholder="请选择类别"
            clearable
            @clear="handleSearch"
          >
            <el-option label="解表药" value="解表药" />
            <el-option label="清热药" value="清热药" />
            <el-option label="补虚药" value="补虚药" />
            <el-option label="理气药" value="理气药" />
            <el-option label="活血化瘀药" value="活血化瘀药" />
            <el-option label="止血药" value="止血药" />
            <el-option label="化痰止咳平喘药" value="化痰止咳平喘药" />
            <el-option label="安神药" value="安神药" />
            <el-option label="平肝息风药" value="平肝息风药" />
            <el-option label="开窍药" value="开窍药" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="性味">
          <el-select
            v-model="searchForm.nature"
            placeholder="请选择性味"
            clearable
            @clear="handleSearch"
          >
            <el-option label="寒" value="寒" />
            <el-option label="热" value="热" />
            <el-option label="温" value="温" />
            <el-option label="凉" value="凉" />
            <el-option label="平" value="平" />
          </el-select>
        </el-form-item>
        <el-form-item label="有毒药材">
          <el-switch v-model="searchForm.isToxic" @change="handleSearch" />
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

    <!-- 表格操作栏 -->
    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增药材
        </el-button>
      </div>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="name" label="药名" width="150" />
        <el-table-column prop="alias" label="别名" width="150" />
        <el-table-column label="性味" width="120">
          <template #default="{ row }">
            {{ row.nature }}{{ row.flavor ? '/' + row.flavor : '' }}
          </template>
        </el-table-column>
        <el-table-column prop="meridian" label="归经" width="150" />
        <el-table-column
          prop="efficacy"
          label="功效"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column label="用量范围" width="120">
          <template #default="{ row }">
            {{ row.dosageMin }}-{{ row.dosageMax }}g
          </template>
        </el-table-column>
        <el-table-column label="零售价" width="100">
          <template #default="{ row }">
            ¥{{ row.retailPrice?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="是否有毒" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isToxic" type="danger" size="small">
              {{ row.toxicLevel || '有毒' }}
            </el-tag>
            <el-tag v-else type="success" size="small">无毒</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="药材名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入药材名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="别名" prop="alias">
              <el-input v-model="formData.alias" placeholder="请输入别名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="拼音" prop="pinyin">
              <el-input v-model="formData.pinyin" placeholder="请输入拼音" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="药材类别" prop="category">
              <el-select v-model="formData.category" placeholder="请选择类别">
                <el-option label="解表药" value="解表药" />
                <el-option label="清热药" value="清热药" />
                <el-option label="补虚药" value="补虚药" />
                <el-option label="理气药" value="理气药" />
                <el-option label="活血化瘀药" value="活血化瘀药" />
                <el-option label="止血药" value="止血药" />
                <el-option label="化痰止咳平喘药" value="化痰止咳平喘药" />
                <el-option label="安神药" value="安神药" />
                <el-option label="平肝息风药" value="平肝息风药" />
                <el-option label="开窍药" value="开窍药" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性" prop="nature">
              <el-select v-model="formData.nature" placeholder="请选择性">
                <el-option label="寒" value="寒" />
                <el-option label="热" value="热" />
                <el-option label="温" value="温" />
                <el-option label="凉" value="凉" />
                <el-option label="平" value="平" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="味" prop="flavor">
              <el-input v-model="formData.flavor" placeholder="如：苦、甘" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="归经" prop="meridian">
              <el-input v-model="formData.meridian" placeholder="如：肝、肾" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产地" prop="origin">
              <el-input v-model="formData.origin" placeholder="请输入产地" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="功效" prop="efficacy">
          <el-input
            v-model="formData.efficacy"
            type="textarea"
            :rows="2"
            placeholder="请输入功效"
          />
        </el-form-item>

        <el-form-item label="炮制方法" prop="processingMethod">
          <el-input
            v-model="formData.processingMethod"
            placeholder="请输入炮制方法"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最小用量(g)" prop="dosageMin">
              <el-input-number
                v-model="formData.dosageMin"
                :min="0"
                :precision="1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大用量(g)" prop="dosageMax">
              <el-input-number
                v-model="formData.dosageMax"
                :min="0"
                :precision="1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="贮藏方法" prop="storage">
          <el-input v-model="formData.storage" placeholder="请输入贮藏方法" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="是否有毒" prop="isToxic">
              <el-switch v-model="formData.isToxic" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              v-if="formData.isToxic"
              label="毒性等级"
              prop="toxicLevel"
            >
              <el-select v-model="formData.toxicLevel" placeholder="请选择毒性等级">
                <el-option label="小毒" value="小毒" />
                <el-option label="有毒" value="有毒" />
                <el-option label="大毒" value="大毒" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="进货价(元)" prop="purchasePrice">
              <el-input-number
                v-model="formData.purchasePrice"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="零售价(元)" prop="retailPrice">
              <el-input-number
                v-model="formData.retailPrice"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import {
  getHerbPage,
  createHerb,
  updateHerb,
  deleteHerb
} from '@/api/herb'
import { useTableKeyboardNav } from '@/composables/useTableKeyboardNav'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  category: '',
  nature: '',
  isToxic: false
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const { tableRef, handleArrowUp, handleArrowDown, selectFirstRow } = useTableKeyboardNav(tableData)
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增药材')
const formRef = ref(null)
const submitLoading = ref(false)
const editId = ref(null)

const formData = reactive({
  name: '',
  alias: '',
  pinyin: '',
  category: '',
  nature: '',
  flavor: '',
  meridian: '',
  efficacy: '',
  origin: '',
  processingMethod: '',
  dosageMin: 0,
  dosageMax: 0,
  storage: '',
  isToxic: false,
  toxicLevel: '',
  purchasePrice: 0,
  retailPrice: 0
})

const formRules = {
  name: [{ required: true, message: '请输入药材名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择药材类别', trigger: 'change' }],
  nature: [{ required: true, message: '请选择性', trigger: 'change' }],
  retailPrice: [{ required: true, message: '请输入零售价', trigger: 'blur' }]
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
    const res = await getHerbPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
      selectFirstRow()
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
  searchForm.keyword = ''
  searchForm.category = ''
  searchForm.nature = ''
  searchForm.isToxic = false
  handleSearch()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增药材'
  editId.value = null
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑药材'
  editId.value = row.id
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该药材吗？', '提示', {
      type: 'warning'
    })
    const res = await deleteHerb(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      const res = editId.value
        ? await updateHerb(editId.value, formData)
        : await createHerb(formData)

      if (res.code === 200) {
        ElMessage.success(editId.value ? '编辑成功' : '新增成功')
        dialogVisible.value = false
        fetchData()
      }
    } catch (error) {
      ElMessage.error(editId.value ? '编辑失败' : '新增失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    name: '',
    alias: '',
    pinyin: '',
    category: '',
    nature: '',
    flavor: '',
    meridian: '',
    efficacy: '',
    origin: '',
    processingMethod: '',
    dosageMin: 0,
    dosageMax: 0,
    storage: '',
    isToxic: false,
    toxicLevel: '',
    purchasePrice: 0,
    retailPrice: 0
  })
  formRef.value?.clearValidate()
}

// 对话框关闭
const handleDialogClose = () => {
  resetForm()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.herb-list-container {
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
}
</style>
