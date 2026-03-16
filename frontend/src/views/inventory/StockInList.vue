<template>
  <div class="stock-in-list-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="入库单号">
          <el-input
            v-model="searchForm.stockInNo"
            placeholder="请输入入库单号"
            clearable
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            @clear="handleSearch"
          >
            <el-option label="待审核" value="待审核" />
            <el-option label="已审核" value="已审核" />
            <el-option label="已入库" value="已入库" />
            <el-option label="已驳回" value="已驳回" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleSearch"
          />
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

    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增入库单
        </el-button>
        <el-button type="success" @click="handleSmartStockIn">
          <el-icon><MagicStick /></el-icon>
          智能入库
        </el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="orderNo" label="入库单号" width="180" />
        <el-table-column label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="supplierName" label="供应商" width="200" />
        <el-table-column label="金额" width="150" align="right">
          <template #default="{ row }">
            ¥{{ row.totalAmount?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">
              查看
            </el-button>
            <el-button
              v-if="row.status === '待审核'"
              link
              type="primary"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="row.status === '待审核'"
              link
              type="success"
              @click="handleApprove(row)"
            >
              审核
            </el-button>
            <el-button
              v-if="row.status === '已审核'"
              link
              type="warning"
              @click="handleStockIn(row)"
            >
              入库
            </el-button>
            <el-button
              v-if="row.status === '待审核'"
              link
              type="danger"
              @click="handleReject(row)"
            >
              驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="入库单详情"
      width="1000px"
      @close="handleDetailDialogClose"
    >
      <div v-if="currentStockIn" class="stock-in-detail">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="入库单号">
            {{ currentStockIn.orderNo }}
          </el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="getTypeTagType(currentStockIn.type)">
              {{ currentStockIn.type }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentStockIn.status)">
              {{ currentStockIn.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="供应商">
            {{ currentStockIn.supplierName }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ currentStockIn.createdAt }}
          </el-descriptions-item>
          <el-descriptions-item label="总金额">
            ¥{{ currentStockIn.totalAmount?.toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentStockIn.approvedAt" label="审核时间">
            {{ currentStockIn.approvedAt }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentStockIn.remark" label="备注" :span="3">
            {{ currentStockIn.remark }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">
          <span style="font-weight: 600; font-size: 16px">入库明细</span>
        </el-divider>

        <el-table
          :data="currentStockIn.items"
          stripe
          border
          style="width: 100%"
          show-summary
          :summary-method="getSummaries"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="drugName" label="商品名称" width="200" />
          <el-table-column prop="specification" label="规格" width="150" />
          <el-table-column prop="batchNo" label="批号" width="150" />
          <el-table-column label="数量" width="120" align="right">
            <template #default="{ row }">
              {{ row.quantity }}
            </template>
          </el-table-column>
          <el-table-column prop="unit" label="单位" width="80" align="center" />
          <el-table-column label="单价" width="120" align="right">
            <template #default="{ row }">
              ¥{{ row.unitPrice?.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="金额" width="120" align="right">
            <template #default="{ row }">
              ¥{{ (row.quantity * row.unitPrice).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="productionDate" label="生产日期" width="120" />
          <el-table-column prop="expiryDate" label="有效期至" width="120" />
          <el-table-column label="追溯码" width="90" align="center">
            <template #default="{ row }">
              <el-badge :value="row.traceCodeCount || (row.traceCodes ? row.traceCodes.length : 0)" :hidden="!(row.traceCodeCount || (row.traceCodes && row.traceCodes.length))" type="primary">
                <span>{{ row.traceCodeCount || (row.traceCodes ? row.traceCodes.length : 0) || '-' }}</span>
              </el-badge>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增入库单对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      :title="editingId ? '编辑入库单' : '新增入库单'"
      width="70%"
      top="5vh"
      @close="handleAddDialogClose"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="入库类型" prop="type">
              <el-select v-model="formData.type" placeholder="请选择类型">
                <el-option label="采购入库" value="采购入库" />
                <el-option label="退货入库" value="退货入库" />
                <el-option label="调拨入库" value="调拨入库" />
                <el-option label="其他入库" value="其他入库" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="供应商" prop="supplierId">
              <SupplierSelect
                v-model="formData.supplierName"
                v-model:supplier-id="formData.supplierId"
                @change="handleSupplierChange"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="订单号">
              <el-input :model-value="formData.orderNo || '自动生成'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="备注">
              <el-input v-model="formData.remark" placeholder="备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <el-divider content-position="left">
        <span style="font-weight: 600; font-size: 15px">入库明细</span>
      </el-divider>

      <div class="detail-toolbar">
        <el-button type="primary" size="small" @click="handleAddRow">
          <el-icon><Plus /></el-icon>
          添加药品
        </el-button>
      </div>

      <el-table :data="detailList" border size="small" style="width: 100%">
        <el-table-column type="index" label="#" width="40" align="center" />
        <el-table-column label="商品名称" min-width="200">
          <template #default="{ row }">
            <el-autocomplete
              v-model="row.drugName"
              :fetch-suggestions="(q, cb) => fetchDrugSuggestions(q, cb)"
              :debounce="300"
              value-key="genericName"
              placeholder="名称/拼音/条码/批准文号"
              style="width: 100%"
              @select="(drug) => handleDrugSelect(drug, row)"
            >
              <template #default="{ item }">
                <div style="line-height: 1.4">
                  <div style="font-weight: 500">{{ item.genericName }}</div>
                  <div style="font-size: 12px; color: #909399">
                    {{ item.specification }} | {{ item.manufacturer }}
                  </div>
                </div>
              </template>
            </el-autocomplete>
          </template>
        </el-table-column>
        <el-table-column label="规格" width="90">
          <template #default="{ row }">
            <span>{{ row.specification }}</span>
          </template>
        </el-table-column>
        <el-table-column label="生产厂家" width="140">
          <template #default="{ row }">
            <span>{{ row.manufacturer }}</span>
          </template>
        </el-table-column>
        <el-table-column label="批号" width="100">
          <template #default="{ row }">
            <el-input v-model="row.batchNo" placeholder="批号" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="生产日期" width="140">
          <template #default="{ row }">
            <el-date-picker
              v-model="row.produceDate"
              type="date"
              placeholder="生产日期"
              value-format="YYYY-MM-DD"
              size="small"
              style="width: 100%"
            />
          </template>
        </el-table-column>
        <el-table-column label="有效期至" width="140">
          <template #default="{ row }">
            <el-date-picker
              v-model="row.expireDate"
              type="date"
              placeholder="有效期至"
              value-format="YYYY-MM-DD"
              size="small"
              style="width: 100%"
            />
          </template>
        </el-table-column>
        <el-table-column label="数量" width="90">
          <template #default="{ row }">
            <el-input-number
              v-model="row.quantity"
              :min="1"
              :precision="0"
              size="small"
              controls-position="right"
              style="width: 100%"
              @change="recalcAmount(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="单位" width="50" align="center">
          <template #default="{ row }">
            <span>{{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column label="进货价" width="100">
          <template #default="{ row }">
            <el-input-number
              v-model="row.purchasePrice"
              :min="0"
              :precision="2"
              size="small"
              controls-position="right"
              style="width: 100%"
              @change="recalcAmount(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="金额" width="80" align="right">
          <template #default="{ row }">
            ¥{{ row.amount?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column label="追溯码" width="90" align="center">
          <template #default="{ row }">
            <el-badge :value="row.traceCodes?.length || 0" :max="999" :type="row.traceCodes?.length ? 'primary' : 'info'">
              <el-button link type="primary" size="small" @click="openTraceDialog(row)">
                录入
              </el-button>
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column label="" width="45" align="center">
          <template #default="{ $index }">
            <el-button link type="danger" size="small" @click="handleRemoveRow($index)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="detail-total">
        合计金额：<span class="amount">¥{{ totalDetailAmount.toFixed(2) }}</span>
      </div>

      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 智能入库对话框 -->
    <el-dialog
      v-model="smartDialogVisible"
      title="智能入库"
      width="80%"
      top="3vh"
      @close="handleSmartDialogClose"
    >
      <!-- 阶段1: 上传文件 -->
      <div v-if="smartStep === 0">
        <el-upload
          ref="smartUploadRef"
          class="smart-upload-area"
          drag
          multiple
          :auto-upload="false"
          :show-file-list="false"
          :on-change="handleSmartFileChange"
          accept="image/*,.pdf"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            拖拽图片/PDF到此处，或 <em>点击上传</em>
          </div>
          <div class="el-upload__tip">
            支持同时上传多张不同供应商的单据，系统将自动区分并生成独立入库单
          </div>
        </el-upload>

        <div v-if="smartFiles.length > 0" class="smart-file-list">
          <div class="smart-file-header">
            已选择 {{ smartFiles.length }} 个文件
            <el-button link type="danger" @click="smartFiles = []">清空</el-button>
          </div>
          <div class="smart-file-items">
            <div v-for="(file, index) in smartFiles" :key="index" class="smart-file-item">
              <el-icon v-if="file.raw.type.startsWith('image/')" style="font-size:20px;color:#409eff"><Picture /></el-icon>
              <el-icon v-else style="font-size:20px;color:#e6a23c"><Document /></el-icon>
              <span class="file-name">{{ file.name }}</span>
              <el-button link type="danger" size="small" @click="removeSmartFile(index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </div>

        <div class="smart-actions">
          <el-button type="primary" :disabled="smartFiles.length === 0" @click="handleSmartParse">
            开始智能解析 ({{ smartFiles.length }}张)
          </el-button>
        </div>
      </div>

      <!-- 阶段2: 解析中 -->
      <div v-if="smartStep === 1" class="smart-loading">
        <el-icon class="is-loading" style="font-size:48px;color:#409eff;margin-bottom:16px"><Loading /></el-icon>
        <div class="smart-progress-tip">{{ smartProgressTip }}</div>
        <el-progress :percentage="smartProgress" :stroke-width="10" style="width:60%;margin-top:16px" />
      </div>

      <!-- 阶段3: 结果展示 -->
      <div v-if="smartStep === 2 && smartResult">
        <!-- 统计栏 -->
        <el-alert
          v-if="smartResult.success"
          type="success"
          :closable="false"
          style="margin-bottom:16px"
        >
          <template #title>
            共解析出 <strong>{{ smartResult.orders.length }}</strong> 张入库单，
            涉及 <strong>{{ getUniqueSupplierCount }}</strong> 家供应商，
            <strong>{{ getTotalDrugCount }}</strong> 种药品
            <span v-if="smartResult.totalDrugsCreated > 0">（其中新建 {{ smartResult.totalDrugsCreated }} 种）</span>
          </template>
        </el-alert>

        <el-alert v-else type="error" :closable="false" style="margin-bottom:16px">
          {{ smartResult.errorMessage || '解析失败' }}
        </el-alert>

        <!-- 入库单标签页 -->
        <el-tabs v-if="smartResult.orders && smartResult.orders.length > 0" v-model="smartActiveTab" type="border-card">
          <el-tab-pane
            v-for="(order, orderIndex) in smartResult.orders"
            :key="orderIndex"
            :name="String(orderIndex)"
          >
            <template #label>
              <span>
                {{ order.supplierName || '未知供应商' }}
                <el-tag v-if="order.supplierNewCreated" type="warning" size="small" style="margin-left:4px">新</el-tag>
                <span style="color:#909399;font-size:12px;margin-left:4px">{{ order.invoiceDate || '' }}</span>
              </span>
            </template>

            <!-- 供应商信息 -->
            <el-descriptions :column="3" border size="small" style="margin-bottom:16px">
              <el-descriptions-item label="供应商">
                {{ order.supplierName }}
                <el-tag v-if="order.supplierNewCreated" type="warning" size="small">新建</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="单据日期">{{ order.invoiceDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="药品数">{{ order.details?.length || 0 }} 种</el-descriptions-item>
            </el-descriptions>

            <!-- 药品明细表 -->
            <el-table
              :data="order.details"
              border
              size="small"
              :row-class-name="(row) => row.row.missingFields?.length > 0 ? 'warning-row' : ''"
              style="width:100%;margin-bottom:16px"
            >
              <el-table-column type="index" label="#" width="40" align="center" />
              <el-table-column label="商品名称" min-width="150">
                <template #default="{ row }">
                  {{ row.genericName }}
                  <el-tag v-if="row.drugNewCreated" type="warning" size="small" style="margin-left:4px">新建</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="specification" label="规格" width="100" />
              <el-table-column prop="manufacturer" label="厂家" width="140" show-overflow-tooltip />
              <el-table-column prop="approvalNo" label="批准文号" width="150" show-overflow-tooltip />
              <el-table-column prop="batchNo" label="批号" width="90" />
              <el-table-column prop="expireDate" label="有效期" width="100" />
              <el-table-column prop="quantity" label="数量" width="70" align="right" />
              <el-table-column prop="unit" label="单位" width="50" align="center" />
              <el-table-column label="进价" width="90" align="right">
                <template #default="{ row }">
                  {{ row.purchasePrice != null ? '¥' + Number(row.purchasePrice).toFixed(4) : '-' }}
                </template>
              </el-table-column>
              <el-table-column label="金额" width="90" align="right">
                <template #default="{ row }">
                  {{ row.amount != null ? '¥' + Number(row.amount).toFixed(2) : '-' }}
                </template>
              </el-table-column>
            </el-table>

            <!-- 不完整字段警告 -->
            <el-collapse v-if="order.incompleteFields?.length > 0" style="margin-bottom:16px">
              <el-collapse-item>
                <template #title>
                  <el-icon style="color:#e6a23c;margin-right:6px"><Warning /></el-icon>
                  检测到 {{ order.incompleteFields.length }} 个字段待完善
                </template>
                <el-table :data="order.incompleteFields" border size="small">
                  <el-table-column label="范围" width="140">
                    <template #default="{ row }">
                      {{ row.scope === 'SUPPLIER' ? '供应商' : '药品第' + row.rowIndex + '行' }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="fieldLabel" label="字段" width="120" />
                  <el-table-column label="严重程度" width="100">
                    <template #default="{ row }">
                      <el-tag :type="row.severity === 'ERROR' ? 'danger' : 'warning'" size="small">
                        {{ row.severity === 'ERROR' ? '必填' : '建议' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </el-collapse-item>
            </el-collapse>
          </el-tab-pane>
        </el-tabs>
      </div>

      <template #footer>
        <el-button v-if="smartStep === 2" @click="smartStep = 0">重新上传</el-button>
        <el-button @click="smartDialogVisible = false">取消</el-button>
        <el-button
          v-if="smartStep === 2 && smartResult?.success"
          type="primary"
          :loading="smartBatchLoading"
          @click="handleBatchCreateStockIn"
        >
          一键生成全部入库单 ({{ smartResult.orders?.length || 0 }}张)
        </el-button>
      </template>
    </el-dialog>

    <!-- 追溯码录入对话框 -->
    <TraceCodeInputDialog
      v-model:visible="traceDialogVisible"
      :trace-codes="traceDialogRow?.traceCodes || []"
      :expected-quantity="traceDialogRow?.quantity || 0"
      :drug-name="traceDialogRow?.drugName || ''"
      :batch-no="traceDialogRow?.batchNo || ''"
      @confirm="handleTraceConfirm"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Delete, MagicStick, UploadFilled, Warning, Loading, Picture, Document } from '@element-plus/icons-vue'
import SupplierSelect from '@/components/SupplierSelect.vue'
import TraceCodeInputDialog from '@/components/TraceCodeInputDialog.vue'
import {
  getStockInPage,
  getStockIn,
  createStockIn,
  updateStockIn,
  approveStockIn,
  completeStockIn,
  rejectStockIn,
  smartParseStockIn,
  smartCreateStockIn
} from '@/api/inventory'
import { getDrugPage, getDrug } from '@/api/drug'

// 搜索表单
const searchForm = reactive({
  stockInNo: '',
  status: '',
  dateRange: null
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 详情对话框
const detailDialogVisible = ref(false)
const currentStockIn = ref(null)

// 追溯码对话框
const traceDialogVisible = ref(false)
const traceDialogRow = ref(null)

// 新增对话框
const addDialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const detailList = ref([])
const editingId = ref(null) // null=新增, 有值=编辑

// ========== 智能入库相关 ==========
const smartDialogVisible = ref(false)
const smartStep = ref(0) // 0=上传, 1=解析中, 2=结果
const smartFiles = ref([])
const smartUploadRef = ref(null)
const smartResult = ref(null)
const smartActiveTab = ref('0')
const smartProgress = ref(0)
const smartProgressTip = ref('')
const smartBatchLoading = ref(false)

const getUniqueSupplierCount = computed(() => {
  if (!smartResult.value?.orders) return 0
  const names = new Set(smartResult.value.orders.map(o => o.supplierName))
  return names.size
})

const getTotalDrugCount = computed(() => {
  if (!smartResult.value?.orders) return 0
  return smartResult.value.orders.reduce((sum, o) => sum + (o.details?.length || 0), 0)
})

const totalDetailAmount = computed(() => {
  return detailList.value.reduce((sum, row) => sum + (row.amount || 0), 0)
})

const formData = reactive({
  type: '采购入库',
  supplierId: null,
  supplierName: '',
  orderNo: '',
  remark: ''
})

const formRules = {
  type: [{ required: true, message: '请选择入库类型', trigger: 'change' }],
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }]
}

// 获取类型标签类型
const getTypeTagType = (type) => {
  const typeMap = {
    采购入库: 'primary',
    退货入库: 'warning',
    调拨入库: 'info',
    其他入库: 'default'
  }
  return typeMap[type] || 'default'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    待审核: 'warning',
    已审核: 'primary',
    已入库: 'success',
    已驳回: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取列表数据
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.size,
      stockInNo: searchForm.stockInNo,
      status: searchForm.status
    }
    
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }

    const res = await getStockInPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
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
  searchForm.stockInNo = ''
  searchForm.status = ''
  searchForm.dateRange = null
  handleSearch()
}

// 新增
const handleAdd = () => {
  editingId.value = null
  addDialogVisible.value = true
}

// 编辑（加载已有数据到对话框）
const handleEdit = async (row) => {
  try {
    const res = await getStockIn(row.id)
    if (res.code === 200) {
      const data = res.data
      const stockIn = data.stockIn
      const details = data.details || []

      editingId.value = row.id
      Object.assign(formData, {
        type: stockIn.type,
        supplierId: stockIn.supplierId,
        supplierName: row.supplierName || '',
        orderNo: stockIn.orderNo || '',
        remark: stockIn.remark || ''
      })

      // 先并行获取所有药品信息，再一次性构建明细列表
      const drugInfoMap = {}
      await Promise.all(details.map(async (d) => {
        if (d.drugId) {
          try {
            const drugRes = await getDrug(d.drugId)
            if (drugRes.code === 200 && drugRes.data) {
              drugInfoMap[d.drugId] = drugRes.data.drug || drugRes.data
            }
          } catch { /* ignore */ }
        }
      }))

      detailList.value = details.map(d => {
        const drug = drugInfoMap[d.drugId] || {}
        return {
          key: Date.now() + Math.random(),
          drugId: d.drugId,
          drugName: drug.genericName || '',
          specification: drug.specification || '',
          manufacturer: drug.manufacturer || '',
          batchNo: d.batchNo || '',
          produceDate: d.produceDate,
          expireDate: d.expireDate,
          quantity: Number(d.quantity) || 1,
          unit: d.unit || drug.unit || '',
          purchasePrice: Number(d.purchasePrice) || 0,
          amount: Number(d.amount) || 0,
          traceCodes: d.traceCodes || []
        }
      })

      addDialogVisible.value = true
    }
  } catch {
    ElMessage.error('获取入库单数据失败')
  }
}

// 查看详情
const handleViewDetail = async (row) => {
  try {
    const res = await getStockIn(row.id)
    if (res.code === 200) {
      const data = res.data
      currentStockIn.value = {
        ...data.stockIn,
        items: data.details || []
      }
      // 填充供应商名称（从列表行中获取）
      if (!currentStockIn.value.supplierName && row.supplierName) {
        currentStockIn.value.supplierName = row.supplierName
      }
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

// 审核
const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确认审核通过该入库单吗？', '提示', {
      type: 'warning'
    })
    const res = await approveStockIn(row.id)
    if (res.code === 200) {
      ElMessage.success('审核成功')
      fetchData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审核失败')
    }
  }
}

// 入库
const handleStockIn = async (row) => {
  try {
    await ElMessageBox.confirm('确认完成入库吗？', '提示', {
      type: 'warning'
    })
    const res = await completeStockIn(row.id)
    if (res.code === 200) {
      ElMessage.success('入库成功')
      fetchData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('入库失败')
    }
  }
}

// 驳回
const handleReject = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入驳回原因', '驳回', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入驳回原因'
    })
    const res = await rejectStockIn(row.id, reason)
    if (res.code === 200) {
      ElMessage.success('已驳回')
      fetchData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('驳回失败')
    }
  }
}

// 供应商选择变更 - 手动触发表单验证
const handleSupplierChange = () => {
  formRef.value?.validateField('supplierId')
}

// ========== 入库明细相关 ==========

// 药品搜索
const fetchDrugSuggestions = async (queryString, callback) => {
  if (!queryString) {
    callback([])
    return
  }
  try {
    const res = await getDrugPage({ keyword: queryString, current: 1, size: 20 })
    if (res.code === 200) {
      callback(res.data.records || [])
    } else {
      callback([])
    }
  } catch {
    callback([])
  }
}

// 选中药品后填充行
const handleDrugSelect = (drug, row) => {
  row.drugId = drug.id
  row.drugName = drug.genericName
  row.specification = drug.specification || ''
  row.manufacturer = drug.manufacturer || ''
  row.unit = drug.unit || ''
  row.purchasePrice = drug.purchasePrice || 0
  recalcAmount(row)
}

// 添加明细行
const handleAddRow = () => {
  detailList.value.push({
    key: Date.now(),
    drugId: null,
    drugName: '',
    specification: '',
    manufacturer: '',
    batchNo: '',
    produceDate: null,
    expireDate: null,
    quantity: 1,
    unit: '',
    purchasePrice: 0,
    amount: 0,
    traceCodes: []
  })
}

// 删除明细行
const handleRemoveRow = (index) => {
  detailList.value.splice(index, 1)
}

// 打开追溯码对话框
const openTraceDialog = (row) => {
  traceDialogRow.value = row
  traceDialogVisible.value = true
}

// 追溯码确认回调
const handleTraceConfirm = (codes) => {
  if (traceDialogRow.value) {
    traceDialogRow.value.traceCodes = codes
  }
}

// 重算行金额
const recalcAmount = (row) => {
  row.amount = parseFloat(((row.quantity || 0) * (row.purchasePrice || 0)).toFixed(2))
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  // 明细校验
  if (detailList.value.length === 0) {
    ElMessage.warning('请至少添加一条入库明细')
    return
  }
  for (let i = 0; i < detailList.value.length; i++) {
    const row = detailList.value[i]
    if (!row.drugId) {
      ElMessage.warning(`第 ${i + 1} 行未选择药品`)
      return
    }
    if (!row.quantity || row.quantity <= 0) {
      ElMessage.warning(`第 ${i + 1} 行数量不正确`)
      return
    }
  }

  submitLoading.value = true
  try {
    const requestData = {
      stockIn: {
        type: formData.type,
        supplierId: formData.supplierId,
        remark: formData.remark
      },
      details: detailList.value.map(row => ({
        drugId: row.drugId,
        batchNo: row.batchNo,
        produceDate: row.produceDate,
        expireDate: row.expireDate,
        quantity: row.quantity,
        unit: row.unit,
        purchasePrice: row.purchasePrice,
        amount: row.amount,
        traceCodes: row.traceCodes || []
      }))
    }

    let res
    if (editingId.value) {
      res = await updateStockIn(editingId.value, requestData)
    } else {
      res = await createStockIn(requestData)
    }
    if (res.code === 200) {
      ElMessage.success(editingId.value ? '保存成功' : '新增成功')
      addDialogVisible.value = false
      fetchData()
    }
  } catch (error) {
    ElMessage.error('新增失败')
  } finally {
    submitLoading.value = false
  }
}

// 合计行计算
const getSummaries = (param) => {
  const { columns, data } = param
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (index === 4) {
      // 数量列
      const values = data.map(item => Number(item.quantity))
      if (!values.every(value => isNaN(value))) {
        sums[index] = values.reduce((prev, curr) => {
          const value = Number(curr)
          if (!isNaN(value)) {
            return prev + curr
          } else {
            return prev
          }
        }, 0)
      } else {
        sums[index] = '-'
      }
    } else if (index === 7) {
      // 金额列
      const values = data.map(item => Number(item.quantity) * Number(item.unitPrice))
      if (!values.every(value => isNaN(value))) {
        sums[index] = '¥' + values.reduce((prev, curr) => {
          const value = Number(curr)
          if (!isNaN(value)) {
            return prev + curr
          } else {
            return prev
          }
        }, 0).toFixed(2)
      } else {
        sums[index] = '-'
      }
    } else {
      sums[index] = ''
    }
  })
  return sums
}

// 关闭详情对话框
const handleDetailDialogClose = () => {
  currentStockIn.value = null
}

// 关闭新增对话框
const handleAddDialogClose = () => {
  Object.assign(formData, {
    type: '采购入库',
    supplierId: null,
    supplierName: '',
    orderNo: '',
    remark: ''
  })
  detailList.value = []
  editingId.value = null
  formRef.value?.clearValidate()
}

// ========== 智能入库方法 ==========

// 打开智能入库对话框
const handleSmartStockIn = () => {
  smartDialogVisible.value = true
  smartStep.value = 0
  smartFiles.value = []
  smartResult.value = null
  smartActiveTab.value = '0'
}

// 文件选择变更
const handleSmartFileChange = (file) => {
  smartFiles.value.push(file)
}

// 移除单个文件
const removeSmartFile = (index) => {
  smartFiles.value.splice(index, 1)
}

// 开始智能解析
const handleSmartParse = async () => {
  if (smartFiles.value.length === 0) return

  smartStep.value = 1
  smartProgress.value = 0
  smartProgressTip.value = '正在准备解析...'

  try {
    // 转换文件为base64
    const filesData = []
    for (let i = 0; i < smartFiles.value.length; i++) {
      smartProgressTip.value = `正在读取第 ${i + 1}/${smartFiles.value.length} 个文件...`
      smartProgress.value = Math.floor((i / smartFiles.value.length) * 30)

      const file = smartFiles.value[i].raw
      const base64 = await fileToBase64(file)
      filesData.push({
        fileBase64: base64,
        fileType: file.type
      })
    }

    smartProgressTip.value = '正在AI识别单据...'
    smartProgress.value = 40

    // 模拟进度
    const progressInterval = setInterval(() => {
      if (smartProgress.value < 90) {
        smartProgress.value += 5
        const tips = ['正在识别供应商信息...', '正在匹配药品数据...', '正在检测字段完整性...']
        smartProgressTip.value = tips[Math.floor((smartProgress.value - 40) / 20)] || '正在处理中...'
      }
    }, 1500)

    // 调用后端解析
    const res = await smartParseStockIn({ files: filesData })

    clearInterval(progressInterval)
    smartProgress.value = 100
    smartProgressTip.value = '解析完成'

    if (res.code === 200) {
      smartResult.value = res.data
      smartStep.value = 2
    } else {
      ElMessage.error(res.message || '解析失败')
      smartStep.value = 0
    }
  } catch (error) {
    console.error('智能入库解析失败', error)
    ElMessage.error('解析失败: ' + (error.message || '未知错误'))
    smartStep.value = 0
  }
}

// 文件转base64
const fileToBase64 = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      // 去掉 data:xxx;base64, 前缀
      const base64 = reader.result.split(',')[1]
      resolve(base64)
    }
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

// 批量创建入库单（调用smart-create，由后端统一创建供应商+药品+入库单）
const handleBatchCreateStockIn = async () => {
  if (!smartResult.value?.orders?.length) return

  smartBatchLoading.value = true
  try {
    const res = await smartCreateStockIn(smartResult.value)
    if (res.code === 200) {
      const data = res.data
      const orderCount = data.orders?.length || 0
      const drugsCreated = data.totalDrugsCreated || 0
      const suppliersCreated = data.totalSuppliersCreated || 0
      let msg = `成功生成 ${orderCount} 张入库单`
      if (suppliersCreated > 0) msg += `，新建 ${suppliersCreated} 个供应商`
      if (drugsCreated > 0) msg += `，新建 ${drugsCreated} 个药品`
      ElMessage.success(msg)
      smartDialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.message || '入库单生成失败')
    }
  } catch (e) {
    ElMessage.error('入库单生成失败: ' + (e.message || '未知错误'))
  } finally {
    smartBatchLoading.value = false
  }
}

// 关闭智能入库对话框
const handleSmartDialogClose = () => {
  smartStep.value = 0
  smartFiles.value = []
  smartResult.value = null
  smartActiveTab.value = '0'
  smartProgress.value = 0
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.stock-in-list-container {
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

  .stock-in-detail {
    .el-divider {
      margin: 20px 0;
    }
  }

  .detail-toolbar {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 12px;
  }

  .detail-total {
    margin-top: 12px;
    text-align: right;
    font-size: 14px;
    color: #606266;

    .amount {
      font-size: 18px;
      font-weight: 600;
      color: #f56c6c;
    }
  }

  // 智能入库相关样式
  .smart-upload-area {
    width: 100%;
    :deep(.el-upload-dragger) {
      width: 100%;
      padding: 40px 20px;
    }
    .el-upload__tip {
      color: #909399;
      font-size: 12px;
      margin-top: 8px;
    }
  }

  .smart-file-list {
    margin-top: 20px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    padding: 12px;

    .smart-file-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      font-weight: 500;
    }

    .smart-file-items {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
    }

    .smart-file-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 12px;
      background: #f5f7fa;
      border-radius: 4px;

      .file-name {
        max-width: 150px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-size: 13px;
      }
    }
  }

  .smart-actions {
    margin-top: 24px;
    text-align: center;
  }

  .smart-loading {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 20px;

    .smart-progress-tip {
      font-size: 14px;
      color: #606266;
    }
  }

  :deep(.warning-row) {
    background-color: #fdf6ec !important;
  }
}
</style>
