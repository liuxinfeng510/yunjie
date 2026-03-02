<template>
  <div class="pos-layout">
    <!-- 左侧药品搜索区 -->
    <div class="pos-left">
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span>药品搜索</span>
          </div>
        </template>
        
        <el-input
          v-model="searchKeyword"
          placeholder="扫描条形码或输入药品名称/拼音码"
          clearable
          size="large"
          @input="handleSearch"
          @keyup.enter="handleQuickAdd"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <div class="drug-list" v-loading="loading">
          <el-table
            :data="drugList"
            style="width: 100%; margin-top: 16px"
            height="calc(100vh - 240px)"
            @row-click="addToCart"
            highlight-current-row
          >
            <el-table-column prop="drugName" label="药品名称" min-width="120" />
            <el-table-column prop="specification" label="规格" width="100" />
            <el-table-column prop="manufacturer" label="生产厂家" min-width="120" show-overflow-tooltip />
            <el-table-column prop="retailPrice" label="零售价" width="80" align="right">
              <template #default="{ row }">
                ¥{{ row.retailPrice?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="stock" label="库存" width="70" align="right" />
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click.stop="addToCart(row)">
                  加入
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>

    <!-- 右侧购物车 -->
    <div class="pos-right">
      <el-card shadow="never" style="height: 100%">
        <template #header>
          <div class="card-header">
            <span>购物车</span>
            <el-button type="danger" size="small" text @click="clearCart">清空</el-button>
          </div>
        </template>

        <!-- 会员信息 -->
        <div class="member-section">
          <el-input
            v-model="memberPhone"
            placeholder="输入会员手机号"
            clearable
            @blur="searchMember"
            @keyup.enter="searchMember"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
          <div v-if="currentMember" class="member-info">
            <el-tag type="success">{{ currentMember.name }}</el-tag>
            <span class="member-points">积分: {{ currentMember.points }}</span>
            <span class="member-level">{{ currentMember.levelName }}</span>
          </div>
        </div>

        <!-- 购物车列表 -->
        <div class="cart-items">
          <el-scrollbar height="calc(100vh - 480px)">
            <div v-if="cartItems.length === 0" class="empty-cart">
              <el-empty description="购物车为空" :image-size="80" />
            </div>
            <div v-else>
              <div v-for="(item, index) in cartItems" :key="index" class="cart-item">
                <div class="item-info">
                  <div class="item-name">{{ item.drugName }}</div>
                  <div class="item-spec">{{ item.specification }}</div>
                </div>
                <div class="item-controls">
                  <el-input-number
                    v-model="item.quantity"
                    :min="1"
                    :max="item.stock"
                    size="small"
                    @change="updateCart"
                  />
                </div>
                <div class="item-price">
                  <div class="unit-price">¥{{ item.retailPrice?.toFixed(2) }}</div>
                  <div class="subtotal">¥{{ (item.quantity * item.retailPrice).toFixed(2) }}</div>
                </div>
                <div class="item-actions">
                  <el-button type="danger" size="small" text @click="removeFromCart(index)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </el-scrollbar>
        </div>

        <!-- 结算信息 -->
        <div class="settlement-section">
          <div class="summary-item">
            <span>总金额：</span>
            <span class="amount">¥{{ totalAmount.toFixed(2) }}</span>
          </div>
          <div class="summary-item">
            <span>优惠：</span>
            <el-input-number
              v-model="discountAmount"
              :min="0"
              :max="totalAmount"
              :precision="2"
              size="small"
              style="width: 120px"
            />
          </div>
          <div class="summary-item total">
            <span>实付金额：</span>
            <span class="pay-amount">¥{{ payAmount.toFixed(2) }}</span>
          </div>

          <el-select v-model="paymentMethod" placeholder="选择支付方式" style="width: 100%; margin-top: 12px">
            <el-option label="现金" value="CASH" />
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="医保" value="MEDICAL_INSURANCE" />
          </el-select>

          <el-button
            type="primary"
            size="large"
            style="width: 100%; margin-top: 16px"
            :disabled="cartItems.length === 0 || !paymentMethod"
            @click="handleCheckout"
          >
            结算 (F8)
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, User, Delete } from '@element-plus/icons-vue'
import { getDrugPage } from '@/api/drug'
import { getMemberPage } from '@/api/member'
import { createSaleOrder } from '@/api/sale'

// 搜索和药品列表
const searchKeyword = ref('')
const drugList = ref([])
const loading = ref(false)

// 购物车
const cartItems = ref([])

// 会员信息
const memberPhone = ref('')
const currentMember = ref(null)

// 支付信息
const discountAmount = ref(0)
const paymentMethod = ref('')

// 计算属性
const totalAmount = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.quantity * item.retailPrice, 0)
})

const payAmount = computed(() => {
  return Math.max(0, totalAmount.value - discountAmount.value)
})

// 搜索药品
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    drugList.value = []
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
      drugList.value = res.data.records || []
    }
  } catch (error) {
    ElMessage.error('搜索失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 快速添加（回车键）
const handleQuickAdd = () => {
  if (drugList.value.length === 1) {
    addToCart(drugList.value[0])
  } else if (drugList.value.length > 1) {
    ElMessage.warning('搜索结果不唯一，请点击选择')
  }
}

// 添加到购物车
const addToCart = (drug) => {
  if (drug.stock <= 0) {
    ElMessage.warning('库存不足')
    return
  }

  const existingItem = cartItems.value.find(item => item.id === drug.id)
  if (existingItem) {
    if (existingItem.quantity < drug.stock) {
      existingItem.quantity++
      updateCart()
    } else {
      ElMessage.warning('库存不足')
    }
  } else {
    cartItems.value.push({
      ...drug,
      quantity: 1
    })
  }

  searchKeyword.value = ''
  drugList.value = []
}

// 从购物车移除
const removeFromCart = (index) => {
  cartItems.value.splice(index, 1)
  updateCart()
}

// 清空购物车
const clearCart = () => {
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

// 搜索会员
const searchMember = async () => {
  if (!memberPhone.value.trim()) {
    currentMember.value = null
    return
  }

  try {
    const res = await getMemberPage({
      phone: memberPhone.value,
      current: 1,
      size: 1
    })
    if (res.code === 200 && res.data.records.length > 0) {
      currentMember.value = res.data.records[0]
      ElMessage.success('会员信息加载成功')
    } else {
      currentMember.value = null
      ElMessage.warning('未找到该会员')
    }
  } catch (error) {
    ElMessage.error('查询会员失败：' + error.message)
  }
}

// 结算
const handleCheckout = async () => {
  try {
    const orderData = {
      memberId: currentMember.value?.id,
      items: cartItems.value.map(item => ({
        drugId: item.id,
        drugName: item.drugName,
        specification: item.specification,
        quantity: item.quantity,
        unitPrice: item.retailPrice,
        discount: 0,
        amount: item.quantity * item.retailPrice
      })),
      totalAmount: totalAmount.value,
      discountAmount: discountAmount.value,
      payAmount: payAmount.value,
      paymentMethod: paymentMethod.value
    }

    const res = await createSaleOrder(orderData)
    if (res.code === 200) {
      ElMessage.success('结算成功')
      // 重置状态
      cartItems.value = []
      discountAmount.value = 0
      paymentMethod.value = ''
      memberPhone.value = ''
      currentMember.value = null
      searchKeyword.value = ''
    }
  } catch (error) {
    ElMessage.error('结算失败：' + error.message)
  }
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
}

onMounted(() => {
  window.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
})
</script>

<style scoped lang="scss">
.pos-layout {
  display: flex;
  gap: 16px;
  height: calc(100vh - 120px);
  padding: 16px;

  .pos-left {
    flex: 1;
    overflow: hidden;
  }

  .pos-right {
    width: 420px;
    overflow: hidden;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
  }

  .drug-list {
    margin-top: 16px;
  }

  .member-section {
    margin-bottom: 16px;

    .member-info {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-top: 8px;
      padding: 8px;
      background-color: #f0f9ff;
      border-radius: 4px;

      .member-points {
        color: #ff6b00;
        font-weight: 600;
      }

      .member-level {
        color: #666;
        font-size: 12px;
      }
    }
  }

  .cart-items {
    border-top: 1px solid #eee;
    border-bottom: 1px solid #eee;
    margin-bottom: 16px;

    .empty-cart {
      padding: 40px 0;
      text-align: center;
    }

    .cart-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 8px;
      border-bottom: 1px solid #f5f5f5;

      &:last-child {
        border-bottom: none;
      }

      .item-info {
        flex: 1;
        min-width: 0;

        .item-name {
          font-size: 14px;
          font-weight: 500;
          color: #333;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .item-spec {
          font-size: 12px;
          color: #999;
          margin-top: 4px;
        }
      }

      .item-controls {
        width: 100px;
      }

      .item-price {
        width: 80px;
        text-align: right;

        .unit-price {
          font-size: 12px;
          color: #999;
        }

        .subtotal {
          font-size: 14px;
          font-weight: 600;
          color: #ff6b00;
          margin-top: 4px;
        }
      }

      .item-actions {
        width: 40px;
      }
    }
  }

  .settlement-section {
    .summary-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      font-size: 14px;

      &.total {
        border-top: 2px solid #eee;
        margin-top: 8px;
        padding-top: 12px;
        font-size: 16px;
        font-weight: 600;
      }

      .amount {
        font-size: 16px;
        color: #333;
      }

      .pay-amount {
        font-size: 24px;
        color: #ff6b00;
        font-weight: 700;
      }
    }
  }
}

:deep(.el-table__row) {
  cursor: pointer;
}
</style>
