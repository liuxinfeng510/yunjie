<template>
  <div class="first-marketing-supplier-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" style="margin-bottom: 16px;">
      <el-form :model="searchForm" inline>
        <el-form-item label="企业名称">
          <el-input v-model="searchForm.supplierName" placeholder="请输入企业名称" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 130px;">
            <el-option label="草稿" value="draft" />
            <el-option label="待一审" value="pending_first" />
            <el-option v-if="approvalLevel === '2'" label="待二审" value="pending_second" />
            <el-option label="已通过" value="approved" />
            <el-option label="已驳回" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">查询</el-button>
          <el-button @click="handleReset" :icon="Refresh">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 工具栏和表格 -->
    <el-card shadow="never">
      <div style="margin-bottom: 16px;">
        <el-button type="primary" @click="handleAdd" :icon="Plus">新增首营企业</el-button>
        <el-button type="success" @click="aiDialogVisible = true" :icon="MagicStick">AI新增</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="applyNo" label="申请编号" width="150" />
        <el-table-column prop="supplierName" label="企业名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="creditCode" label="统一社会信用代码" width="200" show-overflow-tooltip />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="applicantName" label="申请人" width="90" />
        <el-table-column prop="applyTime" label="申请时间" width="160" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 'draft' || row.status === 'rejected'" link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'draft' || row.status === 'rejected'" link type="success" size="small" @click="handleSubmit(row)">提交</el-button>
            <el-button v-if="row.status === 'pending_first'" link type="warning" size="small" @click="openApprove(row, 'first')">一审</el-button>
            <el-button v-if="row.status === 'pending_second'" link type="warning" size="small" @click="openApprove(row, 'second')">二审</el-button>
            <el-button v-if="row.status === 'draft'" link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top: 16px; justify-content: flex-end;"
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <!-- 新增/编辑/查看对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="900px" top="5vh" @close="handleDialogClose" destroy-on-close>
      <el-tabs v-model="activeTab">
        <!-- Tab1 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="formData" :rules="formRules" ref="formRef" label-width="140px" :disabled="dialogMode === 'view'">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="企业名称" prop="supplierName">
                  <el-input v-model="formData.supplierName" placeholder="请输入企业名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="统一社会信用代码" prop="creditCode">
                  <el-input v-model="formData.creditCode" placeholder="请输入信用代码" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="法定代表人">
                  <el-input v-model="formData.legalPerson" placeholder="请输入" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="注册资本">
                  <el-input v-model="formData.registeredCapital" placeholder="如：100万元" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="联系人">
                  <el-input v-model="formData.contactName" placeholder="请输入联系人" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="联系电话">
                  <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="企业地址">
                  <el-input v-model="formData.companyAddress" placeholder="请输入企业地址" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="经营范围">
                  <el-input v-model="formData.businessScope" type="textarea" :rows="3" placeholder="请输入经营范围" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>

        <!-- Tab2 证照资料 -->
        <el-tab-pane label="证照资料" name="license">
          <el-form :model="formData" label-width="140px" :disabled="dialogMode === 'view'">
            <el-divider content-position="left">营业执照</el-divider>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="执照编号">
                  <el-input v-model="formData.businessLicenseNo" placeholder="请输入" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="有效期">
                  <el-date-picker v-model="formData.businessLicenseValidRange" type="daterange" range-separator="至" start-placeholder="起始" end-placeholder="截止" value-format="YYYY-MM-DD" style="width:100%;" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="执照图片">
                  <ImageUpload v-model="formData.businessLicenseImage" :disabled="dialogMode === 'view'" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-divider content-position="left">药品经营许可证</el-divider>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="许可证编号">
                  <el-input v-model="formData.drugLicenseNo" placeholder="请输入" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="有效期">
                  <el-date-picker v-model="formData.drugLicenseValidRange" type="daterange" range-separator="至" start-placeholder="起始" end-placeholder="截止" value-format="YYYY-MM-DD" style="width:100%;" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="许可证图片">
                  <ImageUpload v-model="formData.drugLicenseImage" :disabled="dialogMode === 'view'" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-divider content-position="left">GSP认证证书</el-divider>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="证书编号">
                  <el-input v-model="formData.gspCertNo" placeholder="请输入" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="有效期">
                  <el-date-picker v-model="formData.gspCertValidRange" type="daterange" range-separator="至" start-placeholder="起始" end-placeholder="截止" value-format="YYYY-MM-DD" style="width:100%;" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="证书图片">
                  <ImageUpload v-model="formData.gspCertImage" :disabled="dialogMode === 'view'" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>

        <!-- Tab3 委托授权 -->
        <el-tab-pane label="委托授权" name="auth">
          <el-form :model="formData" label-width="140px" :disabled="dialogMode === 'view'">
            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item label="法人委托书">
                  <ImageUpload v-model="formData.legalAuthImage" :disabled="dialogMode === 'view'" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="销售人员姓名">
                  <el-input v-model="formData.salesPersonName" placeholder="请输入" />
                </el-form-item>
              </el-col>
              <el-col :span="12" />
              <el-col :span="24">
                <el-form-item label="身份证图片">
                  <ImageUpload v-model="formData.salesPersonIdImage" :disabled="dialogMode === 'view'" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="授权委托书">
                  <ImageUpload v-model="formData.salesAuthImage" :disabled="dialogMode === 'view'" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>

        <!-- Tab4 开票信息 -->
        <el-tab-pane label="开票信息" name="billing">
          <el-form :model="formData" label-width="140px" :disabled="dialogMode === 'view'">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="开票名称">
                  <el-input v-model="formData.billingName" placeholder="请输入" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="纳税人识别号">
                  <el-input v-model="formData.billingTaxNo" placeholder="请输入" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="开票地址">
                  <el-input v-model="formData.billingAddress" placeholder="请输入" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="开票电话">
                  <el-input v-model="formData.billingPhone" placeholder="请输入" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="开户行">
                  <el-input v-model="formData.billingBankName" placeholder="请输入" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="开户账号">
                  <el-input v-model="formData.billingBankAccount" placeholder="请输入" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <!-- 审批信息（查看模式下展示） -->
      <div v-if="dialogMode === 'view' && formData.status !== 'draft'" style="margin-top: 16px;">
        <el-divider content-position="left">审批信息</el-divider>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="一审人">{{ formData.firstApproverName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="一审时间">{{ formData.firstApproveTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="一审结果">{{ formData.firstApproveResult === 'approved' ? '通过' : formData.firstApproveResult === 'rejected' ? '驳回' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="一审意见">{{ formData.firstApproveOpinion || '-' }}</el-descriptions-item>
          <template v-if="approvalLevel === '2'">
            <el-descriptions-item label="二审人">{{ formData.secondApproverName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="二审时间">{{ formData.secondApproveTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="二审结果">{{ formData.secondApproveResult === 'approved' ? '通过' : formData.secondApproveResult === 'rejected' ? '驳回' : '-' }}</el-descriptions-item>
            <el-descriptions-item label="二审意见">{{ formData.secondApproveOpinion || '-' }}</el-descriptions-item>
          </template>
        </el-descriptions>
      </div>

      <template #footer v-if="dialogMode !== 'view'">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog v-model="approveDialogVisible" :title="approveLevel === 'first' ? '一级审批' : '二级审批'" width="500px">
      <el-descriptions :column="1" border size="small" style="margin-bottom: 16px;">
        <el-descriptions-item label="申请编号">{{ approveRow?.applyNo }}</el-descriptions-item>
        <el-descriptions-item label="企业名称">{{ approveRow?.supplierName }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="80px">
        <el-form-item label="审批意见">
          <el-input v-model="approveOpinion" type="textarea" :rows="3" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="approveLoading" @click="handleApprove(false)">驳回</el-button>
        <el-button type="success" :loading="approveLoading" @click="handleApprove(true)">通过</el-button>
      </template>
    </el-dialog>

    <!-- AI智能建档对话框 -->
    <AiFirstMarketingDialog v-model="aiDialogVisible" @complete="handleAiComplete" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, MagicStick } from '@element-plus/icons-vue'
import AiFirstMarketingDialog from './AiFirstMarketingDialog.vue'
import { useAuthStore } from '@/store/auth'
import {
  getFirstMarketingSupplierPage,
  getFirstMarketingSupplier,
  createFirstMarketingSupplier,
  updateFirstMarketingSupplier,
  deleteFirstMarketingSupplier,
  submitFirstMarketingSupplier,
  approveFirstMarketingSupplierFirst,
  approveFirstMarketingSupplierSecond,
  getApprovalLevel,
  uploadFile
} from '@/api/firstMarketing'

const authStore = useAuthStore()

// 搜索
const searchForm = ref({ supplierName: '', status: '' })
const tableData = ref([])
const loading = ref(false)
const pagination = ref({ current: 1, size: 10, total: 0 })
const approvalLevel = ref('1')

// AI对话框
const aiDialogVisible = ref(false)

// 对话框
const dialogVisible = ref(false)
const dialogMode = ref('add') // add/edit/view
const dialogTitle = ref('')
const activeTab = ref('basic')
const submitLoading = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const formRules = {
  supplierName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  creditCode: [{ required: true, message: '请输入统一社会信用代码', trigger: 'blur' }]
}

const defaultForm = () => ({
  supplierName: '', creditCode: '', legalPerson: '', registeredCapital: '',
  businessScope: '', companyAddress: '', contactName: '', contactPhone: '',
  businessLicenseNo: '', businessLicenseImage: '', businessLicenseValidRange: null,
  drugLicenseNo: '', drugLicenseImage: '', drugLicenseValidRange: null,
  gspCertNo: '', gspCertImage: '', gspCertValidRange: null,
  legalAuthImage: '', salesPersonName: '', salesPersonIdImage: '', salesAuthImage: '',
  billingName: '', billingTaxNo: '', billingAddress: '', billingPhone: '', billingBankName: '', billingBankAccount: '',
  remark: ''
})
const formData = ref(defaultForm())

// 审批
const approveDialogVisible = ref(false)
const approveRow = ref(null)
const approveLevel = ref('first')
const approveOpinion = ref('')
const approveLoading = ref(false)

const statusLabel = (s) => ({ draft: '草稿', pending_first: '待一审', pending_second: '待二审', approved: '已通过', rejected: '已驳回' }[s] || s)
const statusTagType = (s) => ({ draft: 'info', pending_first: 'warning', pending_second: 'warning', approved: 'success', rejected: 'danger' }[s] || '')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getFirstMarketingSupplierPage({
      current: pagination.value.current,
      size: pagination.value.size,
      supplierName: searchForm.value.supplierName || undefined,
      status: searchForm.value.status || undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.value.total = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.value.current = 1; fetchData() }
const handleReset = () => { searchForm.value = { supplierName: '', status: '' }; handleSearch() }

const handleAdd = () => {
  dialogMode.value = 'add'
  dialogTitle.value = '新增首营企业'
  editingId.value = null
  formData.value = defaultForm()
  activeTab.value = 'basic'
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    const res = await getFirstMarketingSupplier(row.id)
    if (res.code === 200) {
      dialogMode.value = 'edit'
      dialogTitle.value = '编辑首营企业'
      editingId.value = row.id
      fillForm(res.data)
      activeTab.value = 'basic'
      dialogVisible.value = true
    }
  } catch { ElMessage.error('获取详情失败') }
}

const handleView = async (row) => {
  try {
    const res = await getFirstMarketingSupplier(row.id)
    if (res.code === 200) {
      dialogMode.value = 'view'
      dialogTitle.value = '查看首营企业'
      editingId.value = null
      fillForm(res.data)
      activeTab.value = 'basic'
      dialogVisible.value = true
    }
  } catch { ElMessage.error('获取详情失败') }
}

const fillForm = (d) => {
  formData.value = {
    ...d,
    businessLicenseValidRange: d.businessLicenseValidFrom && d.businessLicenseValidUntil ? [d.businessLicenseValidFrom, d.businessLicenseValidUntil] : null,
    drugLicenseValidRange: d.drugLicenseValidFrom && d.drugLicenseValidUntil ? [d.drugLicenseValidFrom, d.drugLicenseValidUntil] : null,
    gspCertValidRange: d.gspCertValidFrom && d.gspCertValidUntil ? [d.gspCertValidFrom, d.gspCertValidUntil] : null
  }
}

const buildPayload = () => {
  const f = formData.value
  const payload = { ...f }
  // 展开日期范围
  if (f.businessLicenseValidRange && f.businessLicenseValidRange.length === 2) {
    payload.businessLicenseValidFrom = f.businessLicenseValidRange[0]
    payload.businessLicenseValidUntil = f.businessLicenseValidRange[1]
  } else {
    payload.businessLicenseValidFrom = null; payload.businessLicenseValidUntil = null
  }
  if (f.drugLicenseValidRange && f.drugLicenseValidRange.length === 2) {
    payload.drugLicenseValidFrom = f.drugLicenseValidRange[0]
    payload.drugLicenseValidUntil = f.drugLicenseValidRange[1]
  } else {
    payload.drugLicenseValidFrom = null; payload.drugLicenseValidUntil = null
  }
  if (f.gspCertValidRange && f.gspCertValidRange.length === 2) {
    payload.gspCertValidFrom = f.gspCertValidRange[0]
    payload.gspCertValidUntil = f.gspCertValidRange[1]
  } else {
    payload.gspCertValidFrom = null; payload.gspCertValidUntil = null
  }
  // 清除前端辅助字段
  delete payload.businessLicenseValidRange
  delete payload.drugLicenseValidRange
  delete payload.gspCertValidRange
  return payload
}

const handleSave = async () => {
  if (formRef.value) {
    try { await formRef.value.validate() } catch { activeTab.value = 'basic'; return }
  }
  submitLoading.value = true
  try {
    const payload = buildPayload()
    if (editingId.value) {
      await updateFirstMarketingSupplier(editingId.value, payload)
      ElMessage.success('保存成功')
    } else {
      // 填充申请人
      payload.applicantId = authStore.user?.id
      payload.applicantName = authStore.user?.realName || authStore.user?.username
      await createFirstMarketingSupplier(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch { ElMessage.error('操作失败') }
  finally { submitLoading.value = false }
}

const handleSubmit = async (row) => {
  try {
    await ElMessageBox.confirm('确认提交审批？', '提示', { type: 'warning' })
    await submitFirstMarketingSupplier(row.id)
    ElMessage.success('已提交')
    fetchData()
  } catch (e) { if (e !== 'cancel') ElMessage.error('提交失败') }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除此记录？', '提示', { type: 'warning' })
    await deleteFirstMarketingSupplier(row.id)
    ElMessage.success('已删除')
    fetchData()
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

const openApprove = (row, level) => {
  approveRow.value = row
  approveLevel.value = level
  approveOpinion.value = ''
  approveDialogVisible.value = true
}

const handleApprove = async (approved) => {
  approveLoading.value = true
  try {
    const data = {
      approverId: authStore.user?.id,
      approverName: authStore.user?.realName || authStore.user?.username,
      approved,
      opinion: approveOpinion.value
    }
    if (approveLevel.value === 'first') {
      await approveFirstMarketingSupplierFirst(approveRow.value.id, data)
    } else {
      await approveFirstMarketingSupplierSecond(approveRow.value.id, data)
    }
    ElMessage.success(approved ? '审批通过' : '已驳回')
    approveDialogVisible.value = false
    fetchData()
  } catch { ElMessage.error('审批失败') }
  finally { approveLoading.value = false }
}

const handleDialogClose = () => {
  formData.value = defaultForm()
  editingId.value = null
}

// AI识别完成回调
const handleAiComplete = (aiFormData) => {
  const form = defaultForm()
  // 直接映射文本字段
  const textFields = [
    'supplierName', 'creditCode', 'legalPerson', 'registeredCapital',
    'companyAddress', 'businessScope', 'contactName', 'contactPhone',
    'businessLicenseNo', 'drugLicenseNo', 'gspCertNo',
    'salesPersonName',
    'billingName', 'billingTaxNo', 'billingAddress', 'billingPhone', 'billingBankName', 'billingBankAccount',
    'businessLicenseImage', 'drugLicenseImage', 'gspCertImage',
    'legalAuthImage', 'salesPersonIdImage', 'salesAuthImage'
  ]
  for (const key of textFields) {
    if (aiFormData[key]) form[key] = aiFormData[key]
  }
  // 日期范围映射
  if (aiFormData.businessLicenseValidFrom && aiFormData.businessLicenseValidUntil) {
    form.businessLicenseValidRange = [aiFormData.businessLicenseValidFrom, aiFormData.businessLicenseValidUntil]
  }
  if (aiFormData.drugLicenseValidFrom && aiFormData.drugLicenseValidUntil) {
    form.drugLicenseValidRange = [aiFormData.drugLicenseValidFrom, aiFormData.drugLicenseValidUntil]
  }
  if (aiFormData.gspCertValidFrom && aiFormData.gspCertValidUntil) {
    form.gspCertValidRange = [aiFormData.gspCertValidFrom, aiFormData.gspCertValidUntil]
  }

  formData.value = form
  dialogMode.value = 'add'
  dialogTitle.value = 'AI新增首营企业'
  editingId.value = null
  activeTab.value = 'basic'
  dialogVisible.value = true
}

onMounted(async () => {
  try {
    const res = await getApprovalLevel()
    if (res.code === 200) approvalLevel.value = res.data || '1'
  } catch { /* ignore */ }
  fetchData()
})

// ========== 图片上传子组件 ==========
</script>

<script>
import { defineComponent, h, computed } from 'vue'
import { ElUpload, ElIcon, ElImage, ElLink } from 'element-plus'
import { Plus, Document } from '@element-plus/icons-vue'

const isPdf = (url) => url && url.toLowerCase().endsWith('.pdf')

const ImageUpload = defineComponent({
  name: 'ImageUpload',
  props: {
    modelValue: { type: String, default: '' },
    disabled: { type: Boolean, default: false }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const fileUrl = computed(() => props.modelValue ? `/api${props.modelValue}` : '')

    const handleSuccess = (response) => {
      if (response.code === 200) {
        emit('update:modelValue', response.data)
      }
    }

    const handleRemove = () => {
      emit('update:modelValue', '')
    }

    const renderPreview = () => {
      if (isPdf(fileUrl.value)) {
        return h('a', {
          href: fileUrl.value, target: '_blank', rel: 'noopener',
          style: 'width:100px;height:100px;border:1px solid #dcdfe6;border-radius:4px;display:flex;flex-direction:column;align-items:center;justify-content:center;text-decoration:none;color:#409eff;background:#f5f7fa;'
        }, [
          h(ElIcon, { size: 32, color: '#f56c6c' }, () => h(Document)),
          h('span', { style: 'font-size:12px;margin-top:4px;' }, 'PDF')
        ])
      }
      return h(ElImage, { src: fileUrl.value, style: 'width:100px;height:100px;border-radius:4px;', fit: 'cover', previewSrcList: [fileUrl.value] })
    }

    return () => {
      if (fileUrl.value && !props.disabled) {
        return h('div', { style: 'display:flex;align-items:center;gap:8px;' }, [
          renderPreview(),
          h('a', { style: 'color:#f56c6c;cursor:pointer;font-size:13px;', onClick: handleRemove }, '删除')
        ])
      }
      if (fileUrl.value && props.disabled) {
        return renderPreview()
      }
      if (props.disabled) return h('span', { style: 'color:#999;' }, '暂无')
      return h(ElUpload, {
        action: '/api/file/upload',
        name: 'file',
        showFileList: false,
        headers: { Authorization: 'Bearer ' + (localStorage.getItem('token') || '') },
        onSuccess: handleSuccess,
        accept: '.jpg,.jpeg,.png,.gif,.pdf'
      }, {
        default: () => h('div', {
          style: 'width:100px;height:100px;border:1px dashed #d9d9d9;border-radius:4px;display:flex;align-items:center;justify-content:center;cursor:pointer;color:#8c939d;'
        }, [h(ElIcon, { size: 28 }, () => h(Plus))])
      })
    }
  }
})

export default { components: { ImageUpload } }
</script>

<style scoped>
.first-marketing-supplier-container {
  padding: 0;
}
</style>
