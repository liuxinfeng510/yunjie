<template>
  <div class="pos-layout">
    <!-- 左侧：购物车+药品搜索 -->
    <div class="pos-left">
      <!-- 购物车区域（上部） -->
      <el-card shadow="never" class="cart-card">
        <template #header>
          <div class="card-header">
            <span>购物车</span>
            <el-button type="danger" size="small" text @click="clearCart">清空</el-button>
          </div>
        </template>
        
        <el-table
          :data="cartItems"
          style="width: 100%"
          max-height="280"
          size="small"
          show-summary
          :summary-method="getSummaries"
        >
          <el-table-column prop="genericName" label="商品名称" min-width="120" show-overflow-tooltip />
          <el-table-column prop="specification" label="规格" width="90" />
          <el-table-column prop="manufacturer" label="生产厂家" min-width="100" show-overflow-tooltip />
          <el-table-column prop="unit" label="单位" width="50" align="center" />
          <el-table-column prop="batchNo" label="批号" width="100" show-overflow-tooltip />
          <el-table-column prop="expireDate" label="到期时间" width="95" />
          <el-table-column label="数量" width="100" align="center">
            <template #default="{ row, $index }">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                :max="row.stock"
                size="small"
                controls-position="right"
                @change="updateCart"
              />
            </template>
          </el-table-column>
          <el-table-column prop="retailPrice" label="零售价" width="80" align="right">
            <template #default="{ row }">
              ¥{{ row.retailPrice?.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="金额" width="80" align="right">
            <template #default="{ row }">
              <span class="amount-text">¥{{ (row.quantity * row.retailPrice).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="medicalInsurance" label="医保" width="60" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.medicalInsurance === '甲类'" type="success" size="small">甲</el-tag>
              <el-tag v-else-if="row.medicalInsurance === '乙类'" type="warning" size="small">乙</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="50" align="center" fixed="right">
            <template #default="{ $index }">
              <el-button type="danger" size="small" text @click="removeFromCart($index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="cartItems.length === 0" class="empty-cart">
          <el-empty description="购物车为空" :image-size="60" />
        </div>
      </el-card>

      <!-- 药品搜索区域（下部） -->
      <el-card shadow="never" class="search-card">
        <template #header>
          <div class="card-header">
            <span>商品搜索</span>
          </div>
        </template>
        
        <el-input
          ref="searchInputRef"
          v-model="searchKeyword"
          placeholder="商品名称/拼音/条码/批准文号"
          clearable
          size="large"
          class="search-input"
          @input="handleSearch"
          @keydown.enter.prevent="handleEnterKey"
          @keydown.up.prevent="handleArrowUp"
          @keydown.down.prevent="handleArrowDown"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <div class="drug-list" v-loading="loading">
          <el-table
            ref="drugTableRef"
            :data="drugList"
            style="width: 100%; margin-top: 12px"
            max-height="220"
            size="small"
            @row-click="addToCart"
            highlight-current-row
            :current-row-key="selectedDrugId"
            row-key="id"
          >
            <el-table-column prop="genericName" label="商品名称" min-width="120" show-overflow-tooltip />
            <el-table-column prop="specification" label="规格" width="90" />
            <el-table-column prop="manufacturer" label="生产厂家" min-width="100" show-overflow-tooltip />
            <el-table-column prop="unit" label="单位" width="50" align="center" />
            <el-table-column prop="batchNo" label="批号" width="90" show-overflow-tooltip />
            <el-table-column prop="expiryDate" label="到期时间" width="95" />
            <el-table-column prop="retailPrice" label="零售价" width="80" align="right">
              <template #default="{ row }">
                ¥{{ row.retailPrice?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="stock" label="库存" width="60" align="right" />
          </el-table>
          <div v-if="drugList.length === 0 && searchKeyword" class="no-result">
            暂无搜索结果
          </div>
        </div>
      </el-card>
    </div>

    <!-- 右侧：收费区域 -->
    <div class="pos-right">
      <el-card shadow="never" class="payment-card">
        <template #header>
          <div class="card-header">
            <span>收费结算</span>
          </div>
        </template>

        <!-- 会员信息 -->
        <div class="section-title">会员信息</div>
        <div class="member-section">
          <el-input
            v-model="memberPhone"
            placeholder="姓名/拼音/手机号"
            clearable
            @keyup.enter="searchMember"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
            <template #append>
              <el-button @click="searchMember">查询</el-button>
            </template>
          </el-input>
          <!-- 搜索结果列表 -->
          <div v-if="memberSearchList.length > 0" class="member-search-list">
            <div
              v-for="item in memberSearchList" :key="item.id"
              class="member-search-item"
              @click="selectMember(item)"
            >
              <span class="name">{{ item.name }}</span>
              <span class="phone">{{ item.phone }}</span>
              <span class="points-tag">{{ item.points || 0 }}积分</span>
            </div>
          </div>
          <div v-if="currentMember" class="member-info">
            <div class="member-row">
              <span class="label">会员：</span>
              <span class="value">{{ currentMember.name }}</span>
              <el-tag type="success" size="small">{{ currentMember.levelName || '普通会员' }}</el-tag>
              <el-button link type="danger" size="small" style="margin-left: 8px;" @click="clearMember">清除</el-button>
            </div>
            <div class="member-row">
              <span class="label">积分：</span>
              <span class="value points">{{ currentMember.points || 0 }}</span>
            </div>
          </div>
        </div>

        <!-- 结算明细 -->
        <div class="section-title">结算明细</div>
        <div class="settlement-section">
          <div class="summary-row">
            <span class="label">商品数量：</span>
            <span class="value">{{ cartItems.reduce((sum, item) => sum + item.quantity, 0) }} 件</span>
          </div>
          <div class="summary-row">
            <span class="label">商品金额：</span>
            <span class="value amount">¥{{ totalAmount.toFixed(2) }}</span>
          </div>
          <div v-if="currentMember && memberDiscount < 1" class="summary-row discount">
            <span class="label">会员折扣（{{ (memberDiscount * 10).toFixed(1) }}折）：</span>
            <span class="value">-¥{{ (totalAmount * (1 - memberDiscount)).toFixed(2) }}</span>
          </div>
          <div class="summary-row">
            <span class="label">手动优惠：</span>
            <el-input-number
              v-model="discountAmount"
              :min="0"
              :max="totalAmount"
              :precision="2"
              size="small"
              style="width: 120px"
            />
          </div>
          <div class="summary-row total">
            <span class="label">实付金额：</span>
            <span class="value pay-amount">¥{{ payAmount.toFixed(2) }}</span>
          </div>
        </div>

        <!-- 支付方式 -->
        <div class="section-title">支付方式</div>
        <div class="payment-methods">
          <el-radio-group v-model="paymentMethod" size="large">
            <el-radio-button value="CASH">
              <el-icon><Wallet /></el-icon> 现金
            </el-radio-button>
            <el-radio-button value="WECHAT">
              <el-icon><ChatDotRound /></el-icon> 微信
            </el-radio-button>
            <el-radio-button value="ALIPAY">
              <el-icon><CreditCard /></el-icon> 支付宝
            </el-radio-button>
            <el-radio-button value="MEDICAL_INSURANCE">
              <el-icon><FirstAidKit /></el-icon> 医保
            </el-radio-button>
          </el-radio-group>
        </div>

        <!-- 现金找零 -->
        <div v-if="paymentMethod === 'CASH'" class="cash-change-section">
          <div class="summary-row">
            <span class="label">收取现金：</span>
            <el-input-number
              v-model="cashReceived"
              :min="0"
              :precision="2"
              :step="10"
              size="small"
              controls-position="right"
              style="width: 120px"
            />
          </div>
          <div class="summary-row">
            <span class="label">找零：</span>
            <span class="value" :class="{ 'change-amount': changeAmount >= 0, 'change-error': changeAmount < 0 }">
              ¥{{ changeAmount.toFixed(2) }}
            </span>
          </div>
        </div>

        <!-- 结算按钮 -->
        <div class="action-buttons">
          <el-button
            type="primary"
            size="large"
            :disabled="cartItems.length === 0 || !paymentMethod"
            @click="handleCheckout"
            @contextmenu.prevent="showPrintConfig"
            class="checkout-btn"
          >
            <el-icon><ShoppingCart /></el-icon>
            结算
          </el-button>
          <el-button 
            size="large" 
            @click="handleHangUp" 
            @contextmenu.prevent="showExpireConfig"
            :disabled="cartItems.length === 0"
          >
            挂单
          </el-button>
          <el-button size="large" type="warning" @click="showRetrieveDialog">
            取单
            <el-badge v-if="suspendedCount > 0" :value="suspendedCount" class="suspend-badge" />
          </el-button>
        </div>

        <!-- 快捷键提示 -->
        <div class="shortcut-tips">
          <span>↑↓ 选择</span>
          <span>Enter 添加</span>
          <span>F8 结算</span>
          <span>F9 挂单</span>
          <span>F10 取单</span>
        </div>
      </el-card>
    </div>

    <!-- 挂单列表对话框 -->
    <el-dialog
      v-model="retrieveDialogVisible"
      title="挂单列表"
      width="900px"
      destroy-on-close
    >
      <el-table 
        :data="suspendedOrders" 
        style="width: 100%" 
        v-loading="suspendedLoading"
        highlight-current-row
        @current-change="handleSuspendedSelect"
      >
        <el-table-column prop="orderNo" label="挂单号" width="180" />
        <el-table-column prop="memberName" label="会员" width="80">
          <template #default="{ row }">
            {{ row.memberName || '非会员' }}
          </template>
        </el-table-column>
        <el-table-column label="商品" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ parseItemNames(row.items) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" width="90" align="right">
          <template #default="{ row }">
            ¥{{ row.totalAmount?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="suspendedByName" label="挂单人" width="80" />
        <el-table-column prop="suspendedAt" label="挂单时间" width="140">
          <template #default="{ row }">
            {{ formatDateTime(row.suspendedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleRetrieve(row)">取单</el-button>
            <el-button type="danger" size="small" text @click="handleDeleteSuspended(row)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="retrieveDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 挂单过期时间配置对话框 -->
    <el-dialog
      v-model="expireConfigVisible"
      title="挂单过期时间设置"
      width="400px"
    >
      <el-form label-width="120px">
        <el-form-item label="过期时间(分钟)">
          <el-input-number v-model="expireMinutes" :min="1" :max="1440" />
        </el-form-item>
        <el-form-item>
          <span class="config-tip">设置挂单自动过期时间，超过该时间未取单将自动失效</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="expireConfigVisible = false">取消</el-button>
        <el-button type="primary" @click="saveExpireConfig">保存</el-button>
      </template>
    </el-dialog>

    <!-- 打印设置对话框 -->
    <el-dialog
      v-model="printConfigVisible"
      title="打印设置"
      width="400px"
    >
      <el-form label-width="100px">
        <el-form-item label="结算打印小票">
          <el-switch v-model="enablePrint" active-text="开启" inactive-text="关闭" />
        </el-form-item>
        <el-form-item>
          <span class="config-tip">开启后，结算完成将自动打印80mm小票</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="printConfigVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, User, Delete, Wallet, ChatDotRound, CreditCard, FirstAidKit, ShoppingCart } from '@element-plus/icons-vue'
import { getDrugPage } from '@/api/drug'
import { searchMembers, getMemberLevelList } from '@/api/member'
import { createSaleOrder, createSuspendedOrder, getSuspendedOrderList, retrieveSuspendedOrder, deleteSuspendedOrder, getSuspendedOrderExpireMinutes, updateSuspendedOrderExpireMinutes } from '@/api/sale'

// 搜索和药品列表
const searchKeyword = ref('')
const drugList = ref([])
const loading = ref(false)
const searchInputRef = ref(null)
const drugTableRef = ref(null)
const selectedIndex = ref(-1)
const selectedDrugId = ref(null)

// 购物车
const cartItems = ref([])

// 会员信息
const memberPhone = ref('')
const currentMember = ref(null)
const memberLevels = ref([])
const memberSearchList = ref([])

// 支付信息
const discountAmount = ref(0)
const paymentMethod = ref('')
const cashReceived = ref(0)

// 挂单相关
const retrieveDialogVisible = ref(false)
const suspendedOrders = ref([])
const suspendedLoading = ref(false)
const suspendedCount = ref(0)
const expireConfigVisible = ref(false)
const expireMinutes = ref(60)

// 打印相关
const printConfigVisible = ref(false)
const enablePrint = ref(true)

// 计算属性
const totalAmount = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.quantity * item.retailPrice, 0)
})

// 会员等级折扣
const memberDiscount = computed(() => {
  if (!currentMember.value || !currentMember.value.levelId) return 1
  const level = memberLevels.value.find(l => l.id === currentMember.value.levelId)
  return level ? level.discount : 1
})

const payAmount = computed(() => {
  const afterDiscount = totalAmount.value * memberDiscount.value
  return Math.max(0, afterDiscount - discountAmount.value)
})

// 找零金额
const changeAmount = computed(() => {
  return cashReceived.value - payAmount.value
})

// 搜索药品
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    drugList.value = []
    selectedIndex.value = -1
    selectedDrugId.value = null
    return
  }

  try {
    loading.value = true
    const res = await getDrugPage({
      keyword: searchKeyword.value,
      current: 1,
      size: 20
    })
    if (res.code === 200) {
      // API返回的stock字段已包含库存信息
      drugList.value = res.data.records || []
      // 自动选中第一行
      if (drugList.value.length > 0) {
        selectedIndex.value = 0
        selectedDrugId.value = drugList.value[0].id
        nextTick(() => {
          drugTableRef.value?.setCurrentRow(drugList.value[0])
        })
      } else {
        selectedIndex.value = -1
        selectedDrugId.value = null
      }
    }
  } catch (error) {
    ElMessage.error('搜索失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 上键选择
const handleArrowUp = () => {
  if (drugList.value.length === 0) return
  if (selectedIndex.value > 0) {
    selectedIndex.value--
    selectedDrugId.value = drugList.value[selectedIndex.value].id
    drugTableRef.value?.setCurrentRow(drugList.value[selectedIndex.value])
  }
}

// 下键选择
const handleArrowDown = () => {
  if (drugList.value.length === 0) return
  if (selectedIndex.value < drugList.value.length - 1) {
    selectedIndex.value++
    selectedDrugId.value = drugList.value[selectedIndex.value].id
    drugTableRef.value?.setCurrentRow(drugList.value[selectedIndex.value])
  }
}

// 回车添加
const handleEnterKey = () => {
  if (drugList.value.length > 0 && selectedIndex.value >= 0) {
    addToCart(drugList.value[selectedIndex.value])
  }
}

// 快速添加（回车键）- 保留兼容
const handleQuickAdd = () => {
  handleEnterKey()
}

// 添加到购物车
const addToCart = (drug) => {
  const stock = Number(drug.stock) || 0
  if (stock <= 0) {
    ElMessage.warning('库存不足')
    return
  }

  const existingItem = cartItems.value.find(item => item.id === drug.id)
  if (existingItem) {
    if (existingItem.quantity < stock) {
      existingItem.quantity++
      updateCart()
    } else {
      ElMessage.warning('库存不足')
    }
  } else {
    cartItems.value.push({
      ...drug,
      quantity: 1,
      stock: stock,
      batchNo: drug.batchNo || '-',
      expireDate: drug.expireDate || '-'
    })
  }

  searchKeyword.value = ''
  drugList.value = []
  selectedIndex.value = -1
  selectedDrugId.value = null
}

// 从购物车移除
const removeFromCart = (index) => {
  cartItems.value.splice(index, 1)
  updateCart()
}

// 清空购物车
const clearCart = () => {
  if (cartItems.value.length === 0) return
  ElMessageBox.confirm('确定清空购物车吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    cartItems.value = []
    discountAmount.value = 0
    ElMessage.success('购物车已清空')
  }).catch(() => {})
}

// 更新购物车
const updateCart = () => {
  // 触发响应式更新
}

// 表格合计
const getSummaries = (param) => {
  const { columns, data } = param
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (column.property === 'quantity') {
      sums[index] = data.reduce((sum, item) => sum + item.quantity, 0)
    } else if (column.label === '金额') {
      sums[index] = '¥' + data.reduce((sum, item) => sum + item.quantity * item.retailPrice, 0).toFixed(2)
    } else {
      sums[index] = ''
    }
  })
  return sums
}

// 搜索会员
const searchMember = async () => {
  if (!memberPhone.value.trim()) {
    currentMember.value = null
    memberSearchList.value = []
    return
  }

  try {
    const res = await searchMembers(memberPhone.value)
    if (res.code === 200 && res.data && res.data.length > 0) {
      if (res.data.length === 1) {
        // 只有一个结果，直接选中
        selectMember(res.data[0])
      } else {
        // 多个结果，显示列表让用户选
        memberSearchList.value = res.data
        currentMember.value = null
      }
    } else {
      currentMember.value = null
      memberSearchList.value = []
      ElMessage.warning('未找到该会员')
    }
  } catch (error) {
    ElMessage.error('查询会员失败：' + error.message)
  }
}

// 选择会员
const selectMember = (member) => {
  currentMember.value = member
  memberSearchList.value = []
  ElMessage.success('已选择会员：' + member.name)
}

// 清除会员
const clearMember = () => {
  currentMember.value = null
  memberPhone.value = ''
  memberSearchList.value = []
}

// 挂单
const handleHangUp = async () => {
  try {
    const orderData = {
      memberId: currentMember.value?.id,
      memberName: currentMember.value?.name,
      items: JSON.stringify(cartItems.value),
      totalAmount: totalAmount.value
    }
    
    const res = await createSuspendedOrder(orderData)
    if (res.code === 200) {
      ElMessage.success(`挂单成功，单号: ${res.data.orderNo}`)
      // 清空购物车
      cartItems.value = []
      discountAmount.value = 0
      memberPhone.value = ''
      currentMember.value = null
      // 刷新挂单数量
      loadSuspendedCount()
    }
  } catch (error) {
    ElMessage.error('挂单失败：' + error.message)
  }
}

// 显示取单对话框
const showRetrieveDialog = async () => {
  retrieveDialogVisible.value = true
  await loadSuspendedOrders()
}

// 加载挂单列表
const loadSuspendedOrders = async () => {
  try {
    suspendedLoading.value = true
    const res = await getSuspendedOrderList()
    if (res.code === 200) {
      suspendedOrders.value = res.data || []
      suspendedCount.value = suspendedOrders.value.length
    }
  } catch (error) {
    ElMessage.error('获取挂单列表失败')
  } finally {
    suspendedLoading.value = false
  }
}

// 加载挂单数量
const loadSuspendedCount = async () => {
  try {
    const res = await getSuspendedOrderList()
    if (res.code === 200) {
      suspendedCount.value = (res.data || []).length
    }
  } catch { /* 静默 */ }
}

// 取单
const handleRetrieve = async (order) => {
  try {
    const res = await retrieveSuspendedOrder(order.id)
    if (res.code === 200) {
      // 恢复购物车
      const items = JSON.parse(res.data.items || '[]')
      cartItems.value = items
      
      // 恢复会员信息
      if (res.data.memberId) {
        memberPhone.value = ''
        currentMember.value = {
          id: res.data.memberId,
          name: res.data.memberName
        }
      }
      
      ElMessage.success('取单成功')
      retrieveDialogVisible.value = false
      loadSuspendedCount()
    }
  } catch (error) {
    ElMessage.error('取单失败：' + error.message)
  }
}

// 删除挂单
const handleDeleteSuspended = async (order) => {
  try {
    await ElMessageBox.confirm('确定删除该挂单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await deleteSuspendedOrder(order.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadSuspendedOrders()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 显示过期时间配置
const showExpireConfig = async () => {
  try {
    const res = await getSuspendedOrderExpireMinutes()
    if (res.code === 200) {
      expireMinutes.value = res.data || 60
    }
  } catch { /* 静默 */ }
  expireConfigVisible.value = true
}

// 保存过期时间配置
const saveExpireConfig = async () => {
  try {
    const res = await updateSuspendedOrderExpireMinutes(expireMinutes.value)
    if (res.code === 200) {
      ElMessage.success('配置已保存')
      expireConfigVisible.value = false
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 显示打印配置
const showPrintConfig = () => {
  printConfigVisible.value = true
}

// 打印小票
const printReceipt = (orderNo) => {
  const printWindow = window.open('', '_blank', 'width=320,height=600')
  const now = new Date()
  const dateStr = now.toLocaleString('zh-CN')
  
  // 生成商品明细HTML
  const itemsHtml = cartItems.value.map(item => `
    <tr>
      <td colspan="4" style="text-align:left;padding:2px 0;border-bottom:1px dashed #ccc;">
        ${item.genericName || item.tradeName}
      </td>
    </tr>
    <tr>
      <td style="font-size:11px;color:#666;">${item.specification || '-'}</td>
      <td style="font-size:11px;color:#666;">${item.batchNo || '-'}</td>
      <td style="text-align:right;">${item.quantity}</td>
      <td style="text-align:right;">¥${(item.quantity * item.retailPrice).toFixed(2)}</td>
    </tr>
    <tr>
      <td colspan="4" style="font-size:10px;color:#999;padding-bottom:4px;">
        ${(item.manufacturer || '').substring(0, 20)}${item.manufacturer?.length > 20 ? '...' : ''} | 单价:¥${item.retailPrice.toFixed(2)}
      </td>
    </tr>
  `).join('')

  const html = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <title>销售小票</title>
      <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { 
          font-family: 'Microsoft YaHei', sans-serif; 
          width: 80mm; 
          padding: 5mm;
          font-size: 12px;
        }
        .header { text-align: center; margin-bottom: 10px; }
        .shop-name { font-size: 16px; font-weight: bold; }
        .divider { border-top: 1px dashed #000; margin: 8px 0; }
        .info-row { display: flex; justify-content: space-between; margin: 3px 0; }
        .items-table { width: 100%; border-collapse: collapse; margin: 8px 0; }
        .items-table td { padding: 2px 0; vertical-align: top; }
        .total-section { margin-top: 10px; }
        .total-row { display: flex; justify-content: space-between; margin: 4px 0; }
        .total-amount { font-size: 18px; font-weight: bold; }
        .footer { text-align: center; margin-top: 15px; font-size: 11px; color: #666; }
        @media print {
          body { width: 80mm; }
          @page { size: 80mm auto; margin: 0; }
        }
      </style>
    </head>
    <body>
      <div class="header">
        <div class="shop-name">云界智慧药房</div>
        <div style="font-size:11px;color:#666;margin-top:4px;">销售小票</div>
      </div>
      
      <div class="divider"></div>
      
      <div class="info-row">
        <span>单号:</span>
        <span>${orderNo || '-'}</span>
      </div>
      <div class="info-row">
        <span>时间:</span>
        <span>${dateStr}</span>
      </div>
      <div class="info-row">
        <span>收银员:</span>
        <span>药房管理员</span>
      </div>
      ${currentMember.value ? `
      <div class="info-row">
        <span>会员:</span>
        <span>${currentMember.value.name} (${currentMember.value.phone})</span>
      </div>
      ` : ''}
      
      <div class="divider"></div>
      
      <table class="items-table">
        <thead>
          <tr style="font-weight:bold;border-bottom:1px solid #000;">
            <td>规格</td>
            <td>批号</td>
            <td style="text-align:right;">数量</td>
            <td style="text-align:right;">金额</td>
          </tr>
        </thead>
        <tbody>
          ${itemsHtml}
        </tbody>
      </table>
      
      <div class="divider"></div>
      
      <div class="total-section">
        <div class="total-row">
          <span>商品数量:</span>
          <span>${cartItems.value.reduce((sum, item) => sum + item.quantity, 0)} 件</span>
        </div>
        <div class="total-row">
          <span>商品金额:</span>
          <span>¥${totalAmount.value.toFixed(2)}</span>
        </div>
        ${discountAmount.value > 0 ? `
        <div class="total-row">
          <span>优惠金额:</span>
          <span>-¥${discountAmount.value.toFixed(2)}</span>
        </div>
        ` : ''}
        <div class="total-row">
          <span>支付方式:</span>
          <span>${getPaymentLabel(paymentMethod.value)}</span>
        </div>
        ${paymentMethod.value === 'CASH' && cashReceived.value > 0 ? `
        <div class="total-row">
          <span>收取现金:</span>
          <span>¥${cashReceived.value.toFixed(2)}</span>
        </div>
        <div class="total-row">
          <span>找零:</span>
          <span>¥${changeAmount.value.toFixed(2)}</span>
        </div>
        ` : ''}
        <div class="total-row total-amount">
          <span>实付金额:</span>
          <span style="color:#e6500a;">¥${payAmount.value.toFixed(2)}</span>
        </div>
      </div>
      
      <div class="divider"></div>
      
      <div class="footer">
        <p>感谢您的光临！</p>
        <p style="margin-top:5px;">如有问题请保留此小票</p>
      </div>
      
      <script>
        window.onload = function() {
          window.print();
        }
      <\/script>
    </body>
    </html>
  `
  
  printWindow.document.write(html)
  printWindow.document.close()
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 解析挂单商品名称列表
const parseItemNames = (itemsJson) => {
  try {
    const items = JSON.parse(itemsJson || '[]')
    return items.map(item => item.genericName || item.tradeName).join('、')
  } catch {
    return '-'
  }
}

// 选中挂单记录
const handleSuspendedSelect = (row) => {
  // 可用于未来扩展，如显示详细商品列表
}

// 结算
const handleCheckout = async () => {
  try {
    const orderData = {
      saleOrder: {
        memberId: currentMember.value?.id,
        totalAmount: totalAmount.value,
        discountAmount: discountAmount.value + (totalAmount.value * (1 - memberDiscount.value)),
        payAmount: payAmount.value,
        payMethod: paymentMethod.value,
        storeId: 1
      },
      details: cartItems.value.map(item => ({
        drugId: item.id,
        drugName: item.genericName,
        specification: item.specification,
        quantity: item.quantity,
        unitPrice: item.retailPrice,
        discount: 0,
        amount: item.quantity * item.retailPrice
      }))
    }

    const res = await createSaleOrder(orderData)
    if (res.code === 200) {
      const orderNo = res.data?.orderNo || ''
      
      // 打印小票（在重置状态前打印）
      if (enablePrint.value) {
        printReceipt(orderNo)
      }
      
      await ElMessageBox.alert(
        `<div style="text-align:center;">
          <p style="font-size:18px; font-weight:600; margin-bottom:8px;">结算成功</p>
          <p>订单号: ${orderNo}</p>
          <p>实付金额: <span style="color:#ff6b00; font-size:24px; font-weight:700;">¥${payAmount.value.toFixed(2)}</span></p>
          <p style="margin-top:12px; color:#909399; font-size:12px;">支付方式: ${getPaymentLabel(paymentMethod.value)}</p>
        </div>`,
        '收银完成',
        { dangerouslyUseHTMLString: true, confirmButtonText: '完成', center: true }
      )
      // 重置状态
      cartItems.value = []
      discountAmount.value = 0
      paymentMethod.value = ''
      cashReceived.value = 0
      memberPhone.value = ''
      currentMember.value = null
      searchKeyword.value = ''
    }
  } catch (error) {
    ElMessage.error('结算失败：' + error.message)
  }
}

const getPaymentLabel = (method) => {
  const labels = {
    'CASH': '现金',
    'WECHAT': '微信',
    'ALIPAY': '支付宝',
    'MEDICAL_INSURANCE': '医保'
  }
  return labels[method] || method
}

// 键盘快捷键
const handleKeyDown = (e) => {
  // F8 结算
  if (e.key === 'F8') {
    e.preventDefault()
    if (cartItems.value.length > 0 && paymentMethod.value) {
      handleCheckout()
    }
  }
  // F9 挂单
  if (e.key === 'F9') {
    e.preventDefault()
    if (cartItems.value.length > 0) {
      handleHangUp()
    }
  }
  // F10 取单
  if (e.key === 'F10') {
    e.preventDefault()
    showRetrieveDialog()
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeyDown)
  loadMemberLevels()
  loadSuspendedCount()
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
})

// 加载会员等级列表
const loadMemberLevels = async () => {
  try {
    const res = await getMemberLevelList()
    if (res.code === 200) { memberLevels.value = res.data || [] }
  } catch { /* 静默 */ }
}
</script>

<style scoped lang="scss">
.pos-layout {
  display: flex;
  gap: 16px;
  height: calc(100vh - 120px);
  padding: 16px;

  .pos-left {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12px;
    min-width: 0;
  }

  .pos-right {
    width: 360px;
    flex-shrink: 0;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
    font-size: 15px;
  }

  .cart-card {
    flex: 1;
    min-height: 0;
    
    .empty-cart {
      padding: 20px 0;
      text-align: center;
    }

    .amount-text {
      color: #ff6b00;
      font-weight: 600;
    }
  }

  .search-card {
    flex-shrink: 0;

    .search-input {
      :deep(input) {
        ime-mode: disabled;
      }
    }

    .drug-list {
      .no-result {
        text-align: center;
        color: #909399;
        padding: 20px;
      }
    }
  }

  .payment-card {
    height: 100%;
    display: flex;
    flex-direction: column;

    :deep(.el-card__body) {
      flex: 1;
      display: flex;
      flex-direction: column;
      padding: 16px;
    }
  }

  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
    padding-left: 8px;
    border-left: 3px solid #409eff;
  }

  .member-section {
    margin-bottom: 20px;
    position: relative;

    .member-search-list {
      position: absolute;
      top: 34px;
      left: 0;
      right: 0;
      z-index: 100;
      background: #fff;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
      max-height: 240px;
      overflow-y: auto;

      .member-search-item {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 10px 12px;
        cursor: pointer;
        border-bottom: 1px solid #f0f0f0;
        font-size: 13px;

        &:last-child { border-bottom: none; }
        &:hover { background: #ecf5ff; }

        .name { font-weight: 600; color: #303133; min-width: 60px; }
        .phone { color: #909399; }
        .points-tag { margin-left: auto; color: #ff6b00; font-size: 12px; }
      }
    }

    .member-info {
      margin-top: 12px;
      padding: 12px;
      background-color: #f0f9ff;
      border-radius: 6px;

      .member-row {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 6px;

        &:last-child {
          margin-bottom: 0;
        }

        .label {
          color: #666;
          font-size: 13px;
        }

        .value {
          font-weight: 500;

          &.points {
            color: #ff6b00;
          }
        }
      }
    }
  }

  .settlement-section {
    margin-bottom: 20px;
    padding: 12px;
    background: #fafafa;
    border-radius: 6px;

    .summary-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      font-size: 14px;

      .label {
        color: #666;
      }

      .value {
        font-weight: 500;

        &.amount {
          color: #333;
          font-size: 16px;
        }
      }

      &.discount {
        .value {
          color: #67c23a;
        }
      }

      &.total {
        border-top: 1px dashed #ddd;
        margin-top: 8px;
        padding-top: 12px;

        .label {
          font-size: 16px;
          font-weight: 600;
          color: #333;
        }

        .pay-amount {
          font-size: 28px;
          color: #ff6b00;
          font-weight: 700;
        }
      }
    }
  }

  .payment-methods {
    margin-bottom: 20px;

    :deep(.el-radio-group) {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .el-radio-button {
        flex: 1;
        min-width: calc(50% - 4px);

        .el-radio-button__inner {
          width: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 6px;
          padding: 12px 8px;
        }
      }
    }
  }

  .cash-change-section {
    background: #f5f7fa;
    padding: 12px;
    border-radius: 6px;
    margin-bottom: 16px;

    .summary-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        color: #606266;
        font-size: 14px;
      }

      .change-amount {
        color: #67c23a;
        font-weight: 600;
        font-size: 16px;
      }

      .change-error {
        color: #f56c6c;
        font-weight: 600;
        font-size: 16px;
      }
    }
  }

  .action-buttons {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;

    .checkout-btn {
      flex: 1;
      height: 48px;
      font-size: 16px;
    }

    .el-button:not(.checkout-btn) {
      min-width: 70px;
      height: 48px;
    }
  }

  .shortcut-tips {
    display: flex;
    justify-content: center;
    gap: 16px;
    font-size: 12px;
    color: #909399;
    margin-top: auto;
    padding-top: 12px;
    border-top: 1px solid #eee;
  }

  .suspend-badge {
    margin-left: 4px;
    :deep(.el-badge__content) {
      top: -4px;
    }
  }
}

.config-tip {
  font-size: 12px;
  color: #909399;
}

:deep(.el-table__row) {
  cursor: pointer;
}

:deep(.el-input-number--small) {
  width: 90px;
}
</style>
