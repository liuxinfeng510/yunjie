<template>
  <div class="knowledge-manage">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>药品知识库</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon> 新增
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query">
        <el-form-item label="商品名称">
          <el-input v-model="query.name" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.category" placeholder="全部" clearable style="width: 120px;">
            <el-option label="处方药" value="prescription" />
            <el-option label="OTC" value="otc" />
            <el-option label="中成药" value="chinese_patent" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="drugs" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="tradeName" label="商品名称" min-width="150" />
        <el-table-column prop="genericName" label="通用名" width="150" />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            {{ categoryMap[row.category] || row.category }}
          </template>
        </el-table-column>
        <el-table-column prop="manufacturer" label="生产厂家" width="150" show-overflow-tooltip />
        <el-table-column prop="indication" label="适应症" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="editDrug(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="deleteDrug(row)">删除</el-button>
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
      :title="isEdit ? '编辑药品知识' : '新增药品知识'"
      width="700px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="商品名称" prop="tradeName">
              <el-input v-model="form.tradeName" placeholder="请输入商品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="通用名" prop="genericName">
              <el-input v-model="form.genericName" placeholder="请输入通用名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="批准文号">
              <el-input v-model="form.approvalNo" placeholder="请输入批准文号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择" style="width: 100%;">
                <el-option label="处方药" value="prescription" />
                <el-option label="OTC" value="otc" />
                <el-option label="中成药" value="chinese_patent" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="剂型">
              <el-input v-model="form.dosageForm" placeholder="如片剂、胶囊" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格">
              <el-input v-model="form.specification" placeholder="如0.25g*12片" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="生产厂家">
              <el-input v-model="form.manufacturer" placeholder="请输入生产厂家" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="储存条件">
              <el-input v-model="form.storageCondition" placeholder="如阴凉处保存" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="有效期(月)">
              <el-input-number v-model="form.validPeriod" :min="1" :max="120" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="参考价格">
              <el-input-number v-model="form.referencePrice" :min="0" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="适应症">
          <el-input v-model="form.indication" type="textarea" :rows="2" placeholder="请输入适应症" />
        </el-form-item>
        <el-form-item label="用法用量">
          <el-input v-model="form.dosage" type="textarea" :rows="2" placeholder="请输入用法用量" />
        </el-form-item>
        <el-form-item label="不良反应">
          <el-input v-model="form.adverseReaction" type="textarea" :rows="2" placeholder="请输入不良反应" />
        </el-form-item>
        <el-form-item label="禁忌">
          <el-input v-model="form.contraindication" type="textarea" :rows="2" placeholder="请输入禁忌" />
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
const drugs = ref([])
const total = ref(0)
const query = ref({ pageNum: 1, pageSize: 10, name: '', category: '' })

const categoryMap = { prescription: '处方药', otc: 'OTC', chinese_patent: '中成药' }

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = ref({})

const rules = {
  tradeName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  genericName: [{ required: true, message: '请输入通用名', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

function initForm() {
  return {
    tradeName: '', genericName: '', approvalNo: '', category: '',
    dosageForm: '', specification: '', manufacturer: '', storageCondition: '',
    validPeriod: null, referencePrice: null, indication: '', dosage: '',
    adverseReaction: '', contraindication: ''
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/drug-knowledge/page', { params: query.value })
    if (res.code === 200) {
      drugs.value = res.data?.records || []
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

function editDrug(row) {
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
      await request.put(`/drug-knowledge/${form.value.id}`, form.value)
    } else {
      await request.post('/drug-knowledge', form.value)
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

async function deleteDrug(row) {
  try {
    await ElMessageBox.confirm(`确定删除"${row.tradeName || row.genericName}"吗?`, '提示', { type: 'warning' })
    await request.delete(`/drug-knowledge/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(loadData)
</script>
