<template>
  <div class="warnings-container">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" type="card">
        <!-- 低库存预警 -->
        <el-tab-pane label="低库存预警" name="lowStock">
          <el-table
            v-loading="lowStockLoading"
            :data="lowStockData"
            style="width: 100%"
            border
          >
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="drugName" label="药品名称" min-width="150" />
            <el-table-column prop="specification" label="规格" width="120" />
            <el-table-column prop="currentStock" label="当前库存" width="100" align="right">
              <template #default="{ row }">
                <span style="color: #f56c6c; font-weight: 600">{{ row.currentStock }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="safeStock" label="安全库存" width="100" align="right" />
            <el-table-column prop="shortage" label="缺口数量" width="100" align="right">
              <template #default="{ row }">
                <el-tag type="danger">{{ row.shortage }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="manufacturer" label="生产厂家" min-width="150" show-overflow-tooltip />
            <el-table-column prop="lastPurchaseTime" label="上次采购时间" width="160" />
            <el-table-column label="操作" width="120" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleReplenish(row)">
                  一键补货
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="lowStockPagination.current"
            v-model:page-size="lowStockPagination.size"
            :total="lowStockPagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadLowStockData"
            @current-change="loadLowStockData"
            style="margin-top: 16px; justify-content: flex-end"
          />
        </el-tab-pane>

        <!-- 近效期预警 -->
        <el-tab-pane label="近效期预警" name="nearExpiry">
          <el-table
            v-loading="nearExpiryLoading"
            :data="nearExpiryData"
            style="width: 100%"
            border
          >
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="drugName" label="药品名称" min-width="150" />
            <el-table-column prop="specification" label="规格" width="120" />
            <el-table-column prop="batchNo" label="批号" width="120" />
            <el-table-column prop="expiryDate" label="有效期至" width="120" />
            <el-table-column prop="remainingDays" label="剩余天数" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getExpiryTagType(row.remainingDays)">
                  {{ row.remainingDays }} 天
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="stock" label="库存数量" width="100" align="right" />
            <el-table-column prop="manufacturer" label="生产厂家" min-width="150" show-overflow-tooltip />
            <el-table-column label="操作" width="150" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="warning" size="small" @click="handlePromotion(row)">
                  促销
                </el-button>
                <el-button type="danger" size="small" @click="handleLoss(row)">
                  报损
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="nearExpiryPagination.current"
            v-model:page-size="nearExpiryPagination.size"
            :total="nearExpiryPagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadNearExpiryData"
            @current-change="loadNearExpiryData"
            style="margin-top: 16px; justify-content: flex-end"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 补货对话框 -->
    <el-dialog
      v-model="replenishDialogVisible"
      title="一键补货"
      width="500px"
      destroy-on-close
    >
      <el-form v-if="currentDrug" :model="replenishForm" label-width="100px">
        <el-form-item label="药品名称">
          <el-input :value="currentDrug.drugName" disabled />
        </el-form-item>
        <el-form-item label="当前库存">
          <el-input :value="currentDrug.currentStock" disabled />
        </el-form-item>
        <el-form-item label="安全库存">
          <el-input :value="currentDrug.safeStock" disabled />
        </el-form-item>
        <el-form-item label="补货数量">
          <el-input-number v-model="replenishForm.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="replenishForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="replenishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReplenishSubmit">提交采购申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// Tab切换
const activeTab = ref('lowStock')

// 低库存预警数据
const lowStockData = ref([])
const lowStockLoading = ref(false)
const lowStockPagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 近效期预警数据
const nearExpiryData = ref([])
const nearExpiryLoading = ref(false)
const nearExpiryPagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 补货对话框
const replenishDialogVisible = ref(false)
const currentDrug = ref(null)
const replenishForm = reactive({
  quantity: 0,
  remark: ''
})

// 模拟低库存数据
const mockLowStockData = [
  {
    id: 1,
    drugName: '阿莫西林胶囊',
    specification: '0.25g*24粒',
    currentStock: 15,
    safeStock: 50,
    shortage: 35,
    manufacturer: '华北制药股份有限公司',
    lastPurchaseTime: '2026-02-15 10:30:00'
  },
  {
    id: 2,
    drugName: '感冒灵颗粒',
    specification: '10g*9袋',
    currentStock: 8,
    safeStock: 30,
    shortage: 22,
    manufacturer: '广州白云山制药总厂',
    lastPurchaseTime: '2026-02-10 14:20:00'
  },
  {
    id: 3,
    drugName: '布洛芬缓释胶囊',
    specification: '0.3g*10粒',
    currentStock: 12,
    safeStock: 40,
    shortage: 28,
    manufacturer: '中美天津史克制药有限公司',
    lastPurchaseTime: '2026-02-20 09:15:00'
  },
  {
    id: 4,
    drugName: '复方甘草片',
    specification: '100片',
    currentStock: 5,
    safeStock: 25,
    shortage: 20,
    manufacturer: '江苏康缘药业股份有限公司',
    lastPurchaseTime: '2026-02-05 16:45:00'
  },
  {
    id: 5,
    drugName: '维生素C片',
    specification: '100mg*100片',
    currentStock: 18,
    safeStock: 60,
    shortage: 42,
    manufacturer: '石药集团欧意药业有限公司',
    lastPurchaseTime: '2026-02-18 11:30:00'
  }
]

// 模拟近效期数据
const mockNearExpiryData = [
  {
    id: 1,
    drugName: '头孢克肟胶囊',
    specification: '0.1g*12粒',
    batchNo: 'H20210315',
    expiryDate: '2026-04-15',
    remainingDays: 44,
    stock: 28,
    manufacturer: '深圳致君制药有限公司'
  },
  {
    id: 2,
    drugName: '三九胃泰颗粒',
    specification: '20g*9袋',
    batchNo: 'H20210420',
    expiryDate: '2026-03-20',
    remainingDays: 18,
    stock: 15,
    manufacturer: '华润三九医药股份有限公司'
  },
  {
    id: 3,
    drugName: '复方丹参片',
    specification: '0.32g*60片',
    batchNo: 'H20210510',
    expiryDate: '2026-03-10',
    remainingDays: 8,
    stock: 42,
    manufacturer: '天津天士力制药股份有限公司'
  },
  {
    id: 4,
    drugName: '云南白药胶囊',
    specification: '0.25g*16粒',
    batchNo: 'H20210228',
    expiryDate: '2026-05-01',
    remainingDays: 60,
    stock: 20,
    manufacturer: '云南白药集团股份有限公司'
  },
  {
    id: 5,
    drugName: '板蓝根颗粒',
    specification: '10g*20袋',
    batchNo: 'H20210605',
    expiryDate: '2026-03-25',
    remainingDays: 23,
    stock: 35,
    manufacturer: '广州白云山光华制药股份有限公司'
  }
]

// 效期标签类型
const getExpiryTagType = (days) => {
  if (days <= 15) return 'danger'
  if (days <= 30) return 'warning'
  if (days <= 60) return 'info'
  return ''
}

// 加载低库存数据
const loadLowStockData = () => {
  lowStockLoading.value = true
  setTimeout(() => {
    const start = (lowStockPagination.current - 1) * lowStockPagination.size
    const end = start + lowStockPagination.size
    lowStockData.value = mockLowStockData.slice(start, end)
    lowStockPagination.total = mockLowStockData.length
    lowStockLoading.value = false
  }, 300)
}

// 加载近效期数据
const loadNearExpiryData = () => {
  nearExpiryLoading.value = true
  setTimeout(() => {
    const start = (nearExpiryPagination.current - 1) * nearExpiryPagination.size
    const end = start + nearExpiryPagination.size
    nearExpiryData.value = mockNearExpiryData.slice(start, end)
    nearExpiryPagination.total = mockNearExpiryData.length
    nearExpiryLoading.value = false
  }, 300)
}

// 补货
const handleReplenish = (row) => {
  currentDrug.value = row
  replenishForm.quantity = row.shortage
  replenishForm.remark = ''
  replenishDialogVisible.value = true
}

// 提交补货
const handleReplenishSubmit = () => {
  ElMessage.success('采购申请已提交')
  replenishDialogVisible.value = false
}

// 促销
const handlePromotion = (row) => {
  ElMessageBox.confirm(
    `确定为"${row.drugName}"创建促销活动吗？剩余有效期 ${row.remainingDays} 天`,
    '促销确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('促销活动创建成功')
  }).catch(() => {})
}

// 报损
const handleLoss = (row) => {
  ElMessageBox.confirm(
    `确定为"${row.drugName}"(批号: ${row.batchNo})创建报损单吗？库存数量 ${row.stock}`,
    '报损确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('报损单创建成功')
  }).catch(() => {})
}

// 监听Tab切换
watch(activeTab, (newTab) => {
  if (newTab === 'lowStock') {
    loadLowStockData()
  } else if (newTab === 'nearExpiry') {
    loadNearExpiryData()
  }
})

onMounted(() => {
  loadLowStockData()
})
</script>

<style scoped lang="scss">
.warnings-container {
  padding: 16px;

  :deep(.el-tabs__content) {
    padding-top: 16px;
  }
}
</style>
