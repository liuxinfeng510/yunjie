<template>
  <div class="drug-list-container">
    <div v-if="fromPos" style="margin-bottom: 12px;">
      <el-button type="primary" plain size="small" @click="router.push('/sale/pos')">&larr; 返回收银台</el-button>
    </div>
    <!-- 搜索栏 -->
    <el-card shadow="never" style="margin-bottom: 16px;">
      <el-form :model="searchForm" inline>
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable style="width: 200px;" @keydown.enter.prevent="handleSearch" @keydown.up.prevent="handleArrowUp" @keydown.down.prevent="handleArrowDown" />
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
        <el-button type="warning" @click="importDialogVisible = true">批量导入</el-button>
      </div>

      <!-- 表格 -->
      <el-table ref="tableRef" :data="tableData" v-loading="loading" border stripe highlight-current-row>
        <el-table-column prop="drugCode" label="药品编码" width="160" />
        <el-table-column prop="genericName" label="通用名" width="150" show-overflow-tooltip />
        <el-table-column prop="tradeName" label="商品名" width="150" show-overflow-tooltip />
        <el-table-column prop="alias" label="别名" width="120" show-overflow-tooltip />
        <el-table-column prop="specification" label="规格" width="120" />
        <el-table-column prop="dosageForm" label="剂型" width="100" />
        <el-table-column prop="manufacturer" label="生产企业" width="180" show-overflow-tooltip />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.categoryName" :type="getCategoryTagType(row.categoryName)">{{ row.categoryName }}</el-tag>
            <el-tag v-else-if="row.isHerb" type="success">中药饮片</el-tag>
            <el-tag v-else-if="row.otcType === 'OTC_A'">甲类OTC</el-tag>
            <el-tag v-else-if="row.otcType === 'OTC_B'" type="warning">乙类OTC</el-tag>
            <el-tag v-else type="info">未分类</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="retailPrice" label="零售价" width="100">
          <template #default="{ row }">¥{{ row.retailPrice?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '启用' ? 'success' : 'info'">
              {{ row.status === '启用' ? '启用' : '停用' }}
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
        <!-- 药品编码 -->
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="药品编码">
              <el-input
                v-model="formData.drugCode"
                :placeholder="formData.id ? '' : '保存后自动生成'"
                disabled
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 分类、通用名、商品名/别名 -->
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
            <el-form-item v-if="isHerbMode" label="别名">
              <el-input v-model="formData.alias" placeholder="多个别名用逗号分隔" />
            </el-form-item>
            <el-form-item v-else label="商品名">
              <el-input v-model="formData.tradeName" placeholder="请输入商品名" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- ===== 中药饮片特有字段 ===== -->
        <template v-if="isHerbMode">
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="饮片类别">
                <el-select v-model="formData.herbType" placeholder="请选择饮片类别" style="width: 100%;">
                  <el-option v-for="t in herbTypeOptions" :key="t" :label="t" :value="t" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="性">
                <el-select v-model="formData.nature" placeholder="请选择药性" style="width: 100%;">
                  <el-option v-for="n in ['寒','热','温','凉','平']" :key="n" :label="n" :value="n" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="味">
                <el-input v-model="formData.flavor" placeholder="如: 苦,甘" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="归经">
                <el-input v-model="formData.meridian" placeholder="如: 肺,胃" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="产地">
                <el-input v-model="formData.origin" placeholder="请输入产地" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="炮制方法">
                <el-input v-model="formData.processingMethod" placeholder="如: 蜜炙,酒炒" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="功效">
            <el-input v-model="formData.efficacy" type="textarea" :rows="2" placeholder="请输入功效描述" />
          </el-form-item>
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
          </el-row>
          <el-row :gutter="16">
            <el-col :span="6">
              <el-form-item label="最小用量(g)">
                <el-input-number v-model="formData.dosageMin" :precision="1" :min="0" style="width: 100%;" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="最大用量(g)">
                <el-input-number v-model="formData.dosageMax" :precision="1" :min="0" style="width: 100%;" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="有毒">
                <el-switch v-model="formData.isToxic" />
              </el-form-item>
            </el-col>
            <el-col :span="6" v-if="formData.isToxic">
              <el-form-item label="毒性等级">
                <el-select v-model="formData.toxicLevel" placeholder="请选择" style="width: 100%;">
                  <el-option label="小毒" value="小毒" />
                  <el-option label="有毒" value="有毒" />
                  <el-option label="大毒" value="大毒" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="贵细药材">
                <el-switch v-model="formData.isPrecious" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="储存条件">
                <DictSelect
                  v-model="formData.storageCondition"
                  dict-type="storage_condition"
                  placeholder="请选择储存条件"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="6">
              <el-form-item label="库存下限">
                <el-input-number v-model="formData.stockLowerLimit" :min="0" placeholder="最小库存" style="width: 100%;" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="库存上限">
                <el-input-number v-model="formData.stockUpperLimit" :min="0" placeholder="最大库存" style="width: 100%;" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="养护方式">
                <DictSelect
                  v-model="formData.maintenanceMethod"
                  dict-type="maintenance_method"
                  placeholder="请选择养护方式"
                />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="重点养护">
                <el-switch v-model="formData.isKeyMaintenance" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <!-- ===== 西药/成药字段 ===== -->
        <template v-else>
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

          <el-form-item label="生产企业" prop="manufacturer">
            <ManufacturerSelect
              v-model="formData.manufacturer"
              v-model:manufacturer-id="formData.manufacturerId"
              placeholder="输入企业名称搜索"
              @change="handleManufacturerChange"
            />
          </el-form-item>

          <el-form-item label="上市许可持有人">
            <el-input v-model="formData.marketingAuthHolder" placeholder="请输入上市许可持有人" />
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="有效期(月)">
                <el-input-number v-model="formData.validPeriod" :min="1" :max="120" placeholder="如: 24" style="width: 100%;" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="产地">
                <el-input v-model="formData.origin" placeholder="请输入产地" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="库存数量">
                <el-input-number v-model="formData.stockQuantity" :precision="2" :min="0" placeholder="参考值" style="width: 100%;" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="库存下限">
                <el-input-number v-model="formData.stockLowerLimit" :min="0" placeholder="最小库存" style="width: 100%;" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="库存上限">
                <el-input-number v-model="formData.stockUpperLimit" :min="0" placeholder="最大库存" style="width: 100%;" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="养护方式">
                <DictSelect
                  v-model="formData.maintenanceMethod"
                  dict-type="maintenance_method"
                  placeholder="请选择养护方式"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="条形码">
            <BarcodeInput v-model="barcodes" :drug-id="formData.id" />
          </el-form-item>
        </template>

        <!-- 价格（通用） -->
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

        <!-- 西药/成药附加属性 -->
        <template v-if="!isHerbMode">
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

          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="实名登记">
                <el-switch v-model="formData.requireRealName" />
                <span class="form-hint">销售时需实名登记</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="销售可调价">
                <el-switch v-model="formData.allowPriceAdjust" />
                <span class="form-hint">允许收银员调整售价</span>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="状态">
                <el-select v-model="formData.status" style="width: 100%;">
                  <el-option label="启用" value="启用" />
                  <el-option label="停用" value="停用" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </template>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <BatchImportDialog
      v-model:visible="importDialogVisible"
      title="批量导入药品"
      :parse-api="parseDrugImport"
      :execute-api="executeDrugImport"
      @success="loadData"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import DictSelect from '@/components/DictSelect.vue'
import ManufacturerSelect from '@/components/ManufacturerSelect.vue'
import BarcodeInput from '@/components/BarcodeInput.vue'
import BatchImportDialog from '@/components/BatchImportDialog.vue'
import { useTableKeyboardNav } from '@/composables/useTableKeyboardNav'
import {
  getDrugPage,
  getDrug,
  createDrug,
  updateDrug,
  deleteDrug,
  getCategoryTree,
  initHerbCategories,
  parseDrugImport,
  executeDrugImport
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
const herbCategoryIds = ref(new Set())

// 饮片类别选项
const herbTypeOptions = ['解表药', '清热药', '补虚药', '理气药', '活血化瘀药',
  '止血药', '化痰止咳平喘药', '安神药', '平肝息风药', '开窍药', '其他']

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
const dialogTitle = ref('新增药品')
const formRef = ref(null)
const submitLoading = ref(false)

// 条形码列表
const barcodes = ref([])

// 导入对话框
const importDialogVisible = ref(false)

// 表单数据
const formData = reactive({
  id: null,
  drugCode: '',
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
  isImported: false,
  validPeriod: null,
  stockQuantity: null,
  requireRealName: false,
  stockUpperLimit: null,
  stockLowerLimit: null,
  allowPriceAdjust: true,
  maintenanceMethod: '',
  status: '启用',
  // 中药饮片字段
  herbType: '',
  alias: '',
  nature: '',
  flavor: '',
  meridian: '',
  efficacy: '',
  origin: '',
  processingMethod: '',
  dosageMin: null,
  dosageMax: null,
  isToxic: false,
  toxicLevel: '',
  isPrecious: false
})

// 拆零优先开关
const splitPrioritySwitch = computed({
  get: () => formData.splitPriority === 1,
  set: (val) => { formData.splitPriority = val ? 1 : 0 }
})

// 判断当前表单分类是否为中药饮片
const isHerbMode = computed(() => {
  return formData.categoryId && herbCategoryIds.value.has(formData.categoryId)
})

// 表单验证规则（动态）
const formRules = computed(() => {
  const base = {
    categoryId: [{ required: true, message: '请选择药品分类', trigger: 'change' }],
    genericName: [{ required: true, message: '请输入通用名', trigger: 'blur' }],
    unit: [{ required: true, message: '请选择单位', trigger: 'change' }],
    retailPrice: [{ required: true, message: '请输入零售价', trigger: 'blur' }]
  }
  if (isHerbMode.value) {
    return base
  }
  return {
    ...base,
    specification: [{ required: true, message: '请输入规格', trigger: 'blur' }],
    manufacturer: [{ required: true, message: '请输入生产企业', trigger: 'blur' }]
  }
})

// 切换中药饮片/西药时自动设置单位
watch(isHerbMode, (val) => {
  if (val) {
    formData.unit = 'g'
  } else if (!formData.id) {
    formData.unit = '盒'
  }
})

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

const getCategoryTagType = (name) => {
  if (name === '处方药') return 'danger'
  if (name === '非处方药') return 'warning'
  if (name === '中药饮片') return 'success'
  if (name === '医疗器械') return ''
  return 'info'
}

// 从分类树中提取中药饮片分类ID（仅根节点）
const extractHerbIds = (tree) => {
  const ids = new Set()
  const herbNode = tree.find(n => n.name === '中药饮片')
  if (herbNode) {
    ids.add(herbNode.id)
  }
  return ids
}

// 加载分类树
const loadCategoryTree = async () => {
  try {
    const res = await getCategoryTree()
    if (res.code === 200) {
      categoryTree.value = res.data || []
      herbCategoryIds.value = extractHerbIds(categoryTree.value)
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
  formData.drugCode = ''
  formData.categoryId = null
  formData.genericName = ''
  formData.tradeName = ''
  formData.approvalNo = ''
  formData.dosageForm = ''
  formData.specification = ''
  formData.unit = '盒'
  formData.manufacturer = ''
  formData.manufacturerId = null
  formData.marketingAuthHolder = ''
  formData.otcType = 'RX'
  formData.storageCondition = '密封常温保存'
  formData.purchasePrice = 0
  formData.retailPrice = 0
  formData.memberPrice = 0
  formData.medicalInsurance = ''
  formData.isSplit = false
  formData.splitRatio = 1
  formData.splitPriority = 0
  formData.isKeyMaintenance = false
  formData.isImported = false
  formData.validPeriod = null
  formData.stockQuantity = null
  formData.requireRealName = false
  formData.stockUpperLimit = null
  formData.stockLowerLimit = null
  formData.allowPriceAdjust = true
  formData.maintenanceMethod = ''
  formData.status = '启用'
  formData.herbType = ''
  formData.alias = ''
  formData.nature = ''
  formData.flavor = ''
  formData.meridian = ''
  formData.efficacy = ''
  formData.origin = ''
  formData.processingMethod = ''
  formData.dosageMin = null
  formData.dosageMax = null
  formData.isToxic = false
  formData.toxicLevel = ''
  formData.isPrecious = false
  barcodes.value = []
}

// ========== 产地自动提取 ==========
const CITY_NAMES = [
  '石家庄','哈尔滨','呼和浩特','乌鲁木齐',
  '北京','上海','天津','重庆','广州','深圳','成都','武汉',
  '南京','杭州','济南','西安','长沙','郑州','昆明','贵阳',
  '南宁','兰州','太原','合肥','南昌','福州','海口','银川',
  '长春','沈阳','大连','青岛','苏州','无锡','常州','徐州',
  '宁波','温州','厦门','珠海','佛山','东莞','中山',
  '保定','廊坊','邯郸','淄博','烟台','扬州','泉州','吉林',
  '通化','敦化','白山','延吉','四平','梅河口',
  '亳州','安国','樟树','禹州','荷泽','安阳','焦作','新乡',
  '连云港','泰州','盐城','莆田','漳州','三明','赣州','宜春',
  '桂林','玉林','柳州','遵义','曲靖','包头','鄂尔多斯',
  '吉安','九江','上饶','抚州','萍乡','景德镇','鹰潭',
  '株洲','湘潭','衡阳','岳阳','常德','邵阳','益阳','永州',
  '襄阳','宜昌','荆州','黄石','十堰','孝感','荆门',
  '绵阳','德阳','乐山','宜宾','泸州','达州','南充','眉山',
  '芜湖','蚌埠','马鞍山','安庆','阜阳','滁州','宿州','六安',
  '洛阳','开封','平顶山','许昌','南阳','商丘','信阳','周口',
  '潍坊','威海','临沂','德州','聊城','泰安','济宁','日照',
  '唐山','秦皇岛','张家口','承德','沧州','衡水','邢台',
  '太仓','昆山','张家港','江阴','宜兴',
].sort((a, b) => b.length - a.length)

const extractOrigin = (name) => {
  if (!name) return ''
  for (const city of CITY_NAMES) {
    if (name.includes(city)) return city
  }
  return ''
}

const handleManufacturerChange = (item) => {
  if (item && !formData.origin) {
    const city = extractOrigin(typeof item === 'string' ? item : (item.name || ''))
    if (city) formData.origin = city
  }
}

const route = useRoute()
const router = useRouter()
const fromPos = computed(() => route.query.from === 'pos')

// 初始化
onMounted(async () => {
  loadCategoryTree()
  await loadData()

  // 从POS跳转时自动打开药品编辑
  if (route.query.editId) {
    handleEdit({ id: route.query.editId })
  }
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
