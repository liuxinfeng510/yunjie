<template>
  <div class="drug-list-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" style="margin-bottom: 16px;">
      <el-form :model="searchForm" inline>
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="药品分类">
          <el-tree-select
            v-model="searchForm.categoryId"
            :data="categoryTree"
            placeholder="请选择分类"
            clearable
            style="width: 200px;"
            :props="{ label: 'name', value: 'id' }"
          />
        </el-form-item>
        <el-form-item label="OTC类型">
          <el-select v-model="searchForm.otcType" placeholder="请选择OTC类型" clearable style="width: 150px;">
            <el-option label="甲类OTC" value="OTC_A" />
            <el-option label="乙类OTC" value="OTC_B" />
            <el-option label="处方药" value="RX" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px;">
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">查询</el-button>
          <el-button @click="handleReset" :icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 工具栏 -->
    <el-card shadow="never">
      <div style="margin-bottom: 16px;">
        <el-button type="primary" @click="handleAdd" :icon="Plus">新增药品</el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="code" label="药品编码" width="120" />
        <el-table-column prop="genericName" label="通用名" width="150" show-overflow-tooltip />
        <el-table-column prop="tradeName" label="商品名" width="150" show-overflow-tooltip />
        <el-table-column prop="specification" label="规格" width="120" />
        <el-table-column prop="dosageForm" label="剂型" width="100" />
        <el-table-column prop="manufacturer" label="生产企业" width="180" show-overflow-tooltip />
        <el-table-column prop="otcType" label="OTC类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.otcType === 'OTC_A'" type="success">甲类OTC</el-tag>
            <el-tag v-else-if="row.otcType === 'OTC_B'" type="warning">乙类OTC</el-tag>
            <el-tag v-else type="danger">处方药</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="retailPrice" label="零售价" width="100">
          <template #default="{ row }">¥{{ row.retailPrice?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
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
      width="900px"
      @close="handleDialogClose"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <!-- 第一行：分类、通用名、商品名 -->
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="药品分类" prop="categoryId">
              <el-tree-select
                v-model="formData.categoryId"
                :data="categoryTree"
                placeholder="请选择分类"
                clearable
                style="width: 100%;"
                :props="{ label: 'name', value: 'id' }"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="通用名" prop="genericName">
              <el-input v-model="formData.genericName" placeholder="请输入通用名" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="商品名" prop="tradeName">
              <el-input v-model="formData.tradeName" placeholder="请输入商品名" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第二行：批准文号、剂型、规格 -->
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="批准文号" prop="approvalNo">
              <el-input 
                v-model="formData.approvalNo" 
                placeholder="请输入批准文号"
                class="ime-disabled"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="剂型" prop="dosageForm">
              <DictSelect
                v-model="formData.dosageForm"
                dict-type="dosage_form"
                placeholder="请选择剂型"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="规格" prop="specification">
              <el-input v-model="formData.specification" placeholder="如:0.5g*24粒" @blur="parseSplitRatio" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第三行：单位、储存条件、OTC类型 -->
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="单位" prop="unit">
              <DictSelect
                v-model="formData.unit"
                dict-type="drug_unit"
                placeholder="请选择单位"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="储存条件" prop="storageCondition">
              <DictSelect
                v-model="formData.storageCondition"
                dict-type="storage_condition"
                placeholder="请选择储存条件"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="OTC类型" prop="otcType">
              <el-select v-model="formData.otcType" placeholder="请选择OTC类型" style="width: 100%;">
                <el-option label="甲类OTC" value="OTC_A" />
                <el-option label="乙类OTC" value="OTC_B" />
                <el-option label="处方药" value="RX" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第四行：生产企业 -->
        <el-form-item label="生产企业" prop="manufacturer">
          <ManufacturerSelect
            v-model="formData.manufacturer"
            v-model:manufacturer-id="formData.manufacturerId"
            placeholder="输入企业名称搜索"
          />
        </el-form-item>

        <!-- 上市许可持有人 -->
        <el-form-item label="上市许可持有人">
          <el-input v-model="formData.marketingAuthHolder" placeholder="请输入上市许可持有人" />
        </el-form-item>

        <!-- 第五行：条形码 -->
        <el-form-item label="条形码">
          <BarcodeInput v-model="barcodes" :drug-id="formData.id" />
        </el-form-item>

        <!-- 第六行：价格 -->
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="进货价" prop="purchasePrice">
              <el-input-number v-model="formData.purchasePrice" :precision="2" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="零售价" prop="retailPrice">
              <el-input-number v-model="formData.retailPrice" :precision="2" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="会员价" prop="memberPrice">
              <el-input-number v-model="formData.memberPrice" :precision="2" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第七行：医保类型、拆零 -->
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="医保类型" prop="medicalInsurance">
              <el-select v-model="formData.medicalInsurance" placeholder="请选择医保类型" style="width: 100%;">
                <el-option label="甲类" value="A" />
                <el-option label="乙类" value="B" />
                <el-option label="丙类(自费)" value="C" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="允许拆零">
              <el-switch v-model="formData.isSplit" />
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="formData.isSplit">
            <el-form-item label="拆零比例">
              <el-input-number v-model="formData.splitRatio" :min="1" :max="1000" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 第八行：其他属性 -->
        <el-row :gutter="16">
          <el-col :span="8" v-if="formData.isSplit">
            <el-form-item label="拆零优先">
              <el-switch v-model="splitPrioritySwitch" />
              <span class="form-hint">优先销售拆零商品</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="重点养护">
              <el-switch v-model="formData.isKeyMaintenance" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="进口药品">
              <el-switch v-model="formData.isImported" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import DictSelect from '@/components/DictSelect.vue'
import ManufacturerSelect from '@/components/ManufacturerSelect.vue'
import BarcodeInput from '@/components/BarcodeInput.vue'
import {
  getDrugPage,
  getDrug,
  createDrug,
  updateDrug,
  deleteDrug,
  getCategoryTree
} from '@/api/drug'

// 搜索表单
const searchForm = reactive({
  name: '',
  categoryId: null,
  otcType: '',
  status: ''
})

// 分类树
const categoryTree = ref([])

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增药品')
const formRef = ref(null)
const submitLoading = ref(false)

// 条形码列表
const barcodes = ref([])

// 表单数据
const formData = reactive({
  id: null,
  categoryId: null,
  genericName: '',
  tradeName: '',
  approvalNo: '',
  dosageForm: '',
  specification: '',
  unit: '',
  manufacturer: '',
  manufacturerId: null,
  marketingAuthHolder: '',
  otcType: '',
  storageCondition: '',
  purchasePrice: 0,
  retailPrice: 0,
  memberPrice: 0,
  medicalInsurance: '',
  isSplit: false,
  splitRatio: 1,
  splitPriority: 0,
  isKeyMaintenance: false,
  isImported: false
})

// 拆零优先开关
const splitPrioritySwitch = computed({
  get: () => formData.splitPriority === 1,
  set: (val) => { formData.splitPriority = val ? 1 : 0 }
})

// 表单验证规则
const formRules = {
  categoryId: [{ required: true, message: '请选择药品分类', trigger: 'change' }],
  genericName: [{ required: true, message: '请输入通用名', trigger: 'blur' }],
  tradeName: [{ required: true, message: '请输入商品名', trigger: 'blur' }],
  specification: [{ required: true, message: '请输入规格', trigger: 'blur' }],
  unit: [{ required: true, message: '请选择单位', trigger: 'change' }],
  manufacturer: [{ required: true, message: '请输入生产企业', trigger: 'blur' }],
  otcType: [{ required: true, message: '请选择OTC类型', trigger: 'change' }],
  retailPrice: [{ required: true, message: '请输入零售价', trigger: 'blur' }]
}

// 从规格解析拆零比例
const parseSplitRatio = () => {
  const spec = formData.specification
  if (!spec) return
  
  // 匹配常见规格格式：0.5g*24粒, 10mg×30片 等
  const match = spec.match(/[*×x]\s*(\d+)/i)
  if (match && formData.isSplit) {
    formData.splitRatio = parseInt(match[1], 10)
  }
}

// 加载分类树
const loadCategoryTree = async () => {
  try {
    const res = await getCategoryTree()
    if (res.code === 200) {
      categoryTree.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载分类树失败')
  }
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
    const res = await getDrugPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
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
  searchForm.categoryId = null
  searchForm.otcType = ''
  searchForm.status = ''
  pagination.current = 1
  loadData()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增药品'
  resetFormData()
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  dialogTitle.value = '编辑药品'
  try {
    const res = await getDrug(row.id)
    if (res.code === 200) {
      const { drug, barcodes: drugBarcodes } = res.data
      Object.assign(formData, drug)
      barcodes.value = drugBarcodes || []
      dialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载药品信息失败')
  }
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该药品吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteDrug(row.id)
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
        // 构建请求数据（新的 DTO 结构）
        const requestData = {
          drug: { ...formData },
          barcodes: barcodes.value
        }
        
        const res = formData.id
          ? await updateDrug(formData.id, requestData)
          : await createDrug(requestData)
        
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
  formData.categoryId = null
  formData.genericName = ''
  formData.tradeName = ''
  formData.approvalNo = ''
  formData.dosageForm = ''
  formData.specification = ''
  formData.unit = ''
  formData.manufacturer = ''
  formData.manufacturerId = null
  formData.marketingAuthHolder = ''
  formData.otcType = ''
  formData.storageCondition = ''
  formData.purchasePrice = 0
  formData.retailPrice = 0
  formData.memberPrice = 0
  formData.medicalInsurance = ''
  formData.isSplit = false
  formData.splitRatio = 1
  formData.splitPriority = 0
  formData.isKeyMaintenance = false
  formData.isImported = false
  barcodes.value = []
}

// 初始化
onMounted(() => {
  loadCategoryTree()
  loadData()
})
</script>

<style scoped>
.drug-list-container {
  padding: 16px;
}

/* 禁用输入法 */
.ime-disabled :deep(input) {
  ime-mode: disabled;
}

.form-hint {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
