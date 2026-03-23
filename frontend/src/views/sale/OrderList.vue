<template>
  <div class="order-list-container">
    <el-card shadow="never">
      <!-- 搜索区 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable @keydown.enter.prevent="handleSearch" @keydown.up.prevent="handleArrowUp" @keydown.down.prevent="handleArrowDown" />
        </el-form-item>
        <el-form-item label="会员姓名">
          <el-input v-model="searchForm.memberName" placeholder="请输入会员姓名" clearable />
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="searchForm.paymentMethod" placeholder="请选择" clearable>
            <el-option label="现金" value="CASH" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="医保" value="MEDICAL_INSURANCE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        border
      >
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="memberName" label="会员" width="100">
          <template #default="{ row }">
            {{ row.memberName || '非会员' }}
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.totalAmount?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="discountAmount" label="优惠金额" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.discountAmount?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="payAmount" label="实付金额" width="100" align="right">
          <template #default="{ row }">
            <span style="color: #ff6b00; font-weight: 600">¥{{ row.payAmount?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getPaymentMethodType(row.paymentMethod)">
              {{ getPaymentMethodLabel(row.paymentMethod) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cashierName" label="收银员" width="100" />
        <el-table-column prop="createTime" label="时间" width="160" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showDetail(row)">
              查看详情
            </el-button>
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
        @size-change="handleSearch"
        @current-change="handleSearch"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="订单详情"
      width="800px"
      destroy-on-close
    >
      <div v-if="currentOrder" class="order-detail">
        <!-- 订单信息 -->
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="会员">{{ currentOrder.memberName || '非会员' }}</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ currentOrder.totalAmount?.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="优惠金额">¥{{ currentOrder.discountAmount?.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">
            <span style="color: #ff6b00; font-weight: 600">¥{{ currentOrder.payAmount?.toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="支付方式">
            <el-tag :type="getPaymentMethodType(currentOrder.paymentMethod)">
              {{ getPaymentMethodLabel(currentOrder.paymentMethod) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="收银员">{{ currentOrder.cashierName }}</el-descriptions-item>
          <el-descriptions-item label="时间">{{ currentOrder.createTime }}</el-descriptions-item>
        </el-descriptions>

        <!-- 订单明细 -->
        <div class="order-items">
          <h3 style="margin: 20px 0 10px 0">订单明细</h3>
          <el-table :data="currentOrder.items" border>
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="drugName" label="商品名称" min-width="150" />
            <el-table-column prop="specification" label="规格" width="120" />
            <el-table-column prop="quantity" label="数量" width="80" align="right" />
            <el-table-column prop="unitPrice" label="单价" width="100" align="right">
              <template #default="{ row }">
                ¥{{ row.unitPrice?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="discount" label="折扣" width="100" align="right">
              <template #default="{ row }">
                ¥{{ row.discount?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" width="100" align="right">
              <template #default="{ row }">
                ¥{{ row.amount?.toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="handlePrint">打印小票</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSaleOrderPage, getSaleOrder } from '@/api/sale'
import { getConfigByGroup, getStoreList } from '@/api/system'
import { useTableKeyboardNav } from '@/composables/useTableKeyboardNav'
import {
  getPaymentLabel,
  generateReceiptHtml,
  generateHerbReceiptHtml,
  printViaIframe,
  DEFAULT_RECEIPT_FIELDS
} from '@/utils/receipt'

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  memberName: '',
  paymentMethod: ''
})

const dateRange = ref([])

// 表格数据
const tableData = ref([])
const loading = ref(false)
const { tableRef, handleArrowUp, handleArrowDown, selectFirstRow } = useTableKeyboardNav(tableData)

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 详情对话框
const detailVisible = ref(false)
const currentOrder = ref(null)

// 支付方式映射
const paymentMethodMap = {
  CASH: { label: '现金', type: '' },
  WECHAT: { label: '微信', type: 'success' },
  ALIPAY: { label: '支付宝', type: 'warning' },
  MEDICAL_INSURANCE: { label: '医保', type: 'info' }
}

const getPaymentMethodLabel = (method) => {
  return paymentMethodMap[method]?.label || method
}

const getPaymentMethodType = (method) => {
  return paymentMethodMap[method]?.type || ''
}

// 查询订单列表
const handleSearch = async () => {
  try {
    loading.value = true
    const params = {
      ...searchForm,
      current: pagination.current,
      size: pagination.size
    }

    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    const res = await getSaleOrderPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
      selectFirstRow()
    }
  } catch (error) {
    ElMessage.error('查询失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 重置搜索
const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.memberName = ''
  searchForm.paymentMethod = ''
  dateRange.value = []
  pagination.current = 1
  handleSearch()
}

// 查看详情
const showDetail = async (row) => {
  try {
    const res = await getSaleOrder(row.id)
    if (res.code === 200) {
      currentOrder.value = res.data
      detailVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取详情失败：' + error.message)
  }
}

// ========== 小票打印配置 ==========
const receiptFields = ref({ ...DEFAULT_RECEIPT_FIELDS })
const receiptPaperWidth = ref('80')
const receiptShopName = ref('')
const receiptFooter = ref('')
const tenantName = ref('')
const storeAddress = ref('')
const storePhone = ref('')
let receiptConfigLoaded = false

const loadReceiptConfig = async () => {
  if (receiptConfigLoaded) return
  try {
    const [configRes, storeRes] = await Promise.all([
      getConfigByGroup('sale'),
      getStoreList()
    ])
    if (configRes.code === 200 && configRes.data) {
      const configs = {}
      configRes.data.forEach(c => { configs[c.configKey] = c.configValue })
      receiptPaperWidth.value = configs['sale.receipt_paper_width'] || '80'
      receiptShopName.value = configs['sale.receipt_shop_name'] || ''
      receiptFooter.value = configs['sale.receipt_footer'] || ''
      if (configs['sale.receipt_fields']) {
        try {
          receiptFields.value = JSON.parse(configs['sale.receipt_fields'])
        } catch (e) { /* use default */ }
      }
    }
    if (storeRes.code === 200 && storeRes.data?.length > 0) {
      const store = storeRes.data[0]
      if (!receiptShopName.value) receiptShopName.value = store.name || ''
      tenantName.value = store.tenantName || ''
      storeAddress.value = store.address || ''
      storePhone.value = store.phone || ''
    }
    receiptConfigLoaded = true
  } catch (e) {
    console.warn('加载小票配置失败', e)
  }
}

// 打印小票
const handlePrint = async () => {
  if (!currentOrder.value) return
  await loadReceiptConfig()

  const order = currentOrder.value
  const allItems = order.items || []
  const normalItems = allItems.filter(i => !i.isHerb)
  const herbItems = allItems.filter(i => i.isHerb)
  const shopName = receiptShopName.value || '药房'
  const footer = receiptFooter.value || '感谢您的光临！\n如有问题请保留此小票\n药品为特殊商品，无质量问题概不退换'
  const payMethod = order.paymentMethod || ''

  // 打印普通小票
  if (normalItems.length > 0) {
    const data = {
      shopName,
      tenantName: tenantName.value,
      storeAddress: storeAddress.value,
      storePhone: storePhone.value,
      subtitle: '销售小票',
      orderNo: order.orderNo || '-',
      dateTime: order.createTime || '-',
      cashierName: order.cashierName || '-',
      member: order.memberName ? { name: order.memberName, phone: '', points: 0 } : null,
      items: normalItems.map(item => ({
        name: item.drugName || '-',
        specification: item.specification || '-',
        batchNo: item.batchNo || '-',
        quantity: item.quantity,
        unitPrice: Number(item.unitPrice || 0).toFixed(2),
        amount: Number(item.amount || 0).toFixed(2),
        manufacturer: ''
      })),
      itemCount: normalItems.reduce((s, i) => s + Number(i.quantity || 0), 0),
      totalAmount: Number(order.totalAmount || 0).toFixed(2),
      discountAmount: Number(order.discountAmount || 0),
      memberPriceSaving: 0,
      wholeOrderDiscountSaving: 0,
      enableWholeOrderDiscount: false,
      paymentLabel: getPaymentLabel(payMethod),
      paymentMethod: payMethod,
      cashReceived: 0,
      changeAmount: 0,
      payAmount: Number(order.payAmount || 0).toFixed(2),
      footerText: footer
    }
    const html = generateReceiptHtml(data, receiptFields.value, receiptPaperWidth.value)
    printViaIframe(html)
  }

  // 打印中药处方笺
  if (herbItems.length > 0) {
    const dc = order.herbDoseCount || herbItems[0]?.doseCount || 1
    const items = herbItems.map(item => {
      const dpg = Number(item.dosePerGram || 0)
      const up = Number(item.unitPrice || 0)
      return {
        name: item.drugName || '-',
        dosePerGram: dpg,
        totalGram: Math.round(dpg * dc * 10) / 10,
        unitPrice: up,
        amount: Number(item.amount || 0).toFixed(2)
      }
    })
    const perDoseTotalWeight = herbItems.reduce((s, i) => s + Number(i.dosePerGram || 0), 0)
    const herbTotal = herbItems.reduce((s, i) => s + Number(i.amount || 0), 0)

    const data = {
      shopName,
      tenantName: tenantName.value,
      storeAddress: storeAddress.value,
      storePhone: storePhone.value,
      subtitle: '中药处方笺',
      orderNo: order.orderNo || '-',
      dateTime: order.createTime || '-',
      cashierName: order.cashierName || '-',
      member: order.memberName ? { name: order.memberName, phone: '', points: 0 } : null,
      doseCount: dc,
      items,
      perDoseTotalWeight: Math.round(perDoseTotalWeight * 10) / 10,
      totalWeight: Math.round(perDoseTotalWeight * dc * 10) / 10,
      herbTotal: herbTotal.toFixed(2),
      payAmount: Number(order.payAmount || 0).toFixed(2),
      paymentLabel: getPaymentLabel(payMethod),
      paymentMethod: payMethod,
      cashReceived: 0,
      changeAmount: 0,
      discountAmount: Number(order.discountAmount || 0),
      footerText: footer
    }
    const delay = normalItems.length > 0 ? 500 : 0
    setTimeout(() => {
      const html = generateHerbReceiptHtml(data, receiptFields.value, receiptPaperWidth.value)
      printViaIframe(html)
    }, delay)
  }

  if (normalItems.length === 0 && herbItems.length === 0) {
    ElMessage.warning('该订单没有商品明细，无法打印')
  }
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped lang="scss">
.order-list-container {
  padding: 16px;

  .search-form {
    margin-bottom: 16px;
  }

  .order-detail {
    .order-items {
      margin-top: 20px;
    }
  }
}
</style>
