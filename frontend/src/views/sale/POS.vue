<template>
  <div class="pos-layout">
    <!-- 左侧：购物车+药品搜索 -->
    <div class="pos-left">
      <!-- 购物车区域（上部） -->
      <el-card shadow="always" class="cart-card">
        <template #header>
          <div class="card-header">
            <span>购物车</span>
            <div style="display:flex;align-items:center;gap:4px">
              <el-popover v-model:visible="showColumnSettings" placement="bottom-end" :width="240" trigger="click">
                <template #reference>
                  <el-button size="small" text>
                    <el-icon><Setting /></el-icon>
                  </el-button>
                </template>
                <div class="column-settings">
                  <div class="column-settings-title">列排序设置</div>
                  <div v-for="(key, idx) in columnOrder" :key="key" class="column-settings-item">
                    <span>{{ CART_COLUMN_DEFS.find(c => c.key === key)?.label }}</span>
                    <span class="column-settings-btns">
                      <el-button size="small" text :disabled="idx === 0" @click="moveColumnUp(idx)">
                        <el-icon><Top /></el-icon>
                      </el-button>
                      <el-button size="small" text :disabled="idx === columnOrder.length - 1" @click="moveColumnDown(idx)">
                        <el-icon><Bottom /></el-icon>
                      </el-button>
                    </span>
                  </div>
                  <el-button size="small" text type="primary" @click="resetColumnOrder" style="margin-top:8px">恢复默认</el-button>
                </div>
              </el-popover>
              <el-button type="danger" size="small" text @click="clearCart">清空</el-button>
            </div>
          </div>
        </template>
        
        <div class="cart-scroll-body">
        <!-- 普通药品表格 -->
        <el-table
          v-if="normalCartItems.length > 0"
          :key="tableKey"
          :data="normalCartItems"
          style="width: 100%"
          max-height="280"
          size="small"
          stripe
          show-summary
          :summary-method="getSummaries"
          @row-contextmenu="showCartContextMenu"
        >
          <el-table-column
            v-for="col in sortedColumns"
            :key="col.key"
            :prop="col.prop"
            :label="col.label"
            :width="col.width"
            :min-width="col.minWidth"
            :align="col.align"
            :show-overflow-tooltip="col.showOverflowTooltip"
          >
            <template v-if="col.custom" #default="{ row, $index }">
              <el-input v-if="col.key === 'traceCode'" v-model="row.traceCode" size="small" placeholder="扫码" clearable />
              <el-input-number v-else-if="col.key === 'quantity'" v-model="row.quantity" :min="1" :max="row.stock" size="small" controls-position="right" @change="updateCart" />
              <template v-else-if="col.key === 'retailPrice'">
                <template v-if="currentMember && row.memberPrice && row.memberPrice > 0">
                  <span class="member-price-text">¥{{ row.memberPrice?.toFixed(2) }}</span>
                  <span class="original-price-text">¥{{ row.retailPrice?.toFixed(2) }}</span>
                </template>
                <template v-else>
                  <el-input-number v-if="row.allowPriceAdjust !== false" v-model="row.retailPrice" :min="0.01" :precision="4" size="small" controls-position="right" style="width: 100%" @change="updateCart" />
                  <span v-else>¥{{ row.retailPrice?.toFixed(2) }}</span>
                </template>
              </template>
              <span v-else-if="col.key === 'amount'" class="amount-text">¥{{ (row.quantity * getEffectivePrice(row)).toFixed(2) }}</span>
              <template v-else-if="col.key === 'medicalInsurance'">
                <el-tag v-if="row.medicalInsurance === '甲类'" type="success" size="small">甲</el-tag>
                <el-tag v-else-if="row.medicalInsurance === '乙类'" type="warning" size="small">乙</el-tag>
                <span v-else>-</span>
              </template>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="50" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="danger" size="small" text @click="removeFromCart(cartItems.indexOf(row))">
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 中药处方区 -->
        <div v-if="herbCartItems.length > 0" class="herb-prescription-header">
          <span class="herb-prescription-title">中药处方</span>
          <div class="herb-summary-info">
            <span class="herb-summary-text">每剂{{ herbTotalGram.toFixed(1) }}g</span>
            <span class="herb-summary-text">{{ herbDoseCount }}副{{ (herbTotalGram * herbDoseCount).toFixed(1) }}g</span>
            <span class="herb-summary-amount">¥{{ herbTotalAmount.toFixed(2) }}</span>
          </div>
          <div class="herb-dose-control">
            <span class="herb-dose-label">副数</span>
            <el-input-number v-model="herbDoseCount" :min="1" :max="99" size="small" controls-position="right" />
            <span class="herb-dose-unit">副</span>
          </div>
        </div>
        <el-table
          v-if="herbCartItems.length > 0"
          :data="herbCartItems"
          style="width: 100%"
          :max-height="180"
          size="small"
          stripe
          @row-contextmenu="showCartContextMenu"
        >
          <el-table-column prop="genericName" label="药名" min-width="140" show-overflow-tooltip />
          <el-table-column label="每剂(g)" width="110" align="center">
            <template #default="{ row }">
              <el-input-number v-model="row.dosePerGram" :min="0.5" :max="999" :precision="1" :step="1" size="small" controls-position="right" />
            </template>
          </el-table-column>
          <el-table-column label="单价(元/g)" width="110" align="right">
            <template #default="{ row }">
              <el-input-number v-if="row.allowPriceAdjust !== false" v-model="row.retailPrice" :min="0.01" :precision="4" size="small" controls-position="right" style="width: 100%" @change="updateCart" />
              <span v-else style="color:#8c919f;">¥{{ row.retailPrice?.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="小计" width="100" align="right">
            <template #default="{ row }">
              <span class="amount-text">¥{{ (row.dosePerGram * herbDoseCount * getEffectivePrice(row)).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="stock" label="库存" width="60" align="center" />
          <el-table-column label="" width="50" align="center">
            <template #default="{ row }">
              <el-button type="danger" size="small" text @click="removeFromCart(cartItems.indexOf(row))">
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="cartItems.length === 0" class="empty-cart">
          <el-empty description="购物车为空" :image-size="60" />
        </div>
        </div>
      </el-card>

      <!-- 药品搜索区域（下部） -->
      <el-card shadow="always" class="search-card">
        <template #header>
          <div class="card-header">
            <span>商品搜索</span>
            <span
              class="herb-toggle"
              :class="{ active: herbOnly }"
              @click="toggleHerbOnly"
            >中药饮片</span>
            <el-checkbox v-model="showZeroStock" size="small" style="margin-left: auto;">显示零库存</el-checkbox>
          </div>
        </template>
        
        <div class="search-row">
          <el-input
            ref="searchInputRef"
            v-model="searchKeyword"
            placeholder="商品名称/拼音/条码/批准文号/追溯码"
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
        </div>

        <div class="drug-list" v-loading="loading">
          <el-table
            ref="drugTableRef"
            :data="drugList"
            style="width: 100%"
            max-height="220"
            size="small"
            stripe
            @row-click="addToCart"
            highlight-current-row
            :current-row-key="selectedDrugId"
            row-key="id"
          >
            <el-table-column prop="genericName" label="商品名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="specification" label="规格" width="100" />
            <el-table-column prop="manufacturer" label="生产厂家" min-width="150" show-overflow-tooltip />
            <el-table-column prop="unit" label="单位" width="45" align="center" />
            <el-table-column prop="batchNo" label="批号" width="110" show-overflow-tooltip />
            <el-table-column prop="expireDate" label="到期时间" width="92" />
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
      <el-card shadow="always" class="payment-card">
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
            @keydown.enter.prevent="handleMemberEnter"
            @keydown.up.prevent="handleMemberArrowUp"
            @keydown.down.prevent="handleMemberArrowDown"
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
              v-for="(item, idx) in memberSearchList" :key="item.id"
              class="member-search-item"
              :class="{ active: idx === memberSelectedIndex }"
              @click="selectMember(item)"
            >
              <span class="name">{{ item.name }}</span>
              <span class="phone">{{ item.phone }}</span>
              <span class="points-tag">{{ item.points || 0 }}积分</span>
            </div>
          </div>
          <div v-if="currentMember" class="member-info" @contextmenu.prevent="showMemberContextMenu($event)">
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

          <!-- 会员右键菜单 -->
          </div>

        <!-- 结算明细 -->
        <div class="section-title" @contextmenu.prevent="showSettlementContextMenu($event)">结算明细</div>
        <div class="settlement-section" @contextmenu.prevent="showSettlementContextMenu($event)">
          <div class="summary-row">
            <span class="label">商品数量：</span>
            <span class="value">{{ cartItems.reduce((sum, item) => sum + item.quantity, 0) }} 件</span>
          </div>
          <div class="summary-row">
            <span class="label">商品金额：</span>
            <span class="value amount">¥{{ totalAmount.toFixed(2) }}</span>
          </div>
          <div v-if="currentMember && memberPriceSaving > 0" class="summary-row discount">
            <span class="label">会员价优惠：</span>
            <span class="value">-¥{{ memberPriceSaving.toFixed(2) }}</span>
          </div>
          <div class="summary-row discount">
            <span class="label">
              整单折扣<template v-if="currentMember && memberDiscount < 1">（{{ (memberDiscount * 10).toFixed(1) }}折）</template>
              <el-switch
                :model-value="enableWholeOrderDiscount"
                size="small"
                style="margin-left: 4px; vertical-align: middle;"
                @change="toggleWholeOrderDiscount"
              />
            </span>
            <span class="value">
              <template v-if="!enableWholeOrderDiscount">未启用</template>
              <template v-else-if="currentMember && memberDiscount < 1">-¥{{ wholeOrderDiscountSaving.toFixed(2) }}</template>
              <template v-else>¥0.00</template>
            </span>
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
          <div style="display: inline-block;" @contextmenu.prevent="showPrintConfig">
          <el-button
            type="primary"
            size="large"
            :disabled="cartItems.length === 0 || !paymentMethod"
            @click="handleCheckout"
            class="checkout-btn"
          >
            <el-icon><ShoppingCart /></el-icon>
            结算
          </el-button>
          </div>
          <div style="display: inline-block;" @contextmenu.prevent="showExpireConfig">
          <el-button 
            size="large" 
            @click="handleHangUp" 
            :disabled="cartItems.length === 0"
          >
            挂单
          </el-button>
          </div>
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
      width="640px"
    >
      <el-form label-width="100px" size="default">
        <el-form-item label="结算自动打印">
          <el-switch v-model="enablePrint" active-text="开启" inactive-text="关闭" />
        </el-form-item>

        <el-divider content-position="left">基本设置</el-divider>

        <el-form-item label="纸张宽度">
          <el-radio-group v-model="receiptPaperWidth">
            <el-radio-button value="58">58mm</el-radio-button>
            <el-radio-button value="80">80mm</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="小票店名">
          <el-input v-model="receiptShopName" :placeholder="companyName || '留空则使用企业名称'" clearable />
        </el-form-item>

        <el-form-item label="页脚文本">
          <el-input v-model="receiptFooter" type="textarea" :rows="2" placeholder="感谢您的光临！" />
        </el-form-item>

        <el-divider content-position="left">打印字段</el-divider>

        <template v-for="(group, groupKey) in RECEIPT_FIELD_LABELS" :key="groupKey">
          <el-form-item :label="group.label">
            <div style="display: flex; flex-wrap: wrap; gap: 8px;">
              <el-checkbox
                v-for="(fieldLabel, fieldKey) in group.fields"
                :key="fieldKey"
                v-model="receiptFields[groupKey][fieldKey]"
              >{{ fieldLabel }}</el-checkbox>
            </div>
          </el-form-item>
        </template>

        <el-form-item>
          <span style="color: #909399; font-size: 12px;">* 实付金额、商品数量和金额列始终显示</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="resetReceiptConfig">恢复默认</el-button>
        <el-button @click="printConfigVisible = false">取消</el-button>
        <el-button type="primary" @click="saveReceiptConfig">保存</el-button>
      </template>
    </el-dialog>

    <!-- 实名登记对话框 -->
    <el-dialog v-model="realNameDialogVisible" title="实名登记" width="500px" @close="cancelRealName">
      <el-alert type="warning" :closable="false" style="margin-bottom:16px">
        <template #title>
          以下药品需要实名登记：
          <el-tag v-for="d in realNameDrugs" :key="d.id" size="small" style="margin:2px">
            {{ d.genericName }}
          </el-tag>
        </template>
      </el-alert>

      <el-form :model="realNameForm" :rules="realNameRules" ref="realNameFormRef" label-width="90px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="realNameForm.name" placeholder="请输入购买者姓名" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="realNameForm.idCard" maxlength="18" placeholder="请输入18位身份证号" @blur="parseIdCard" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-input v-model="realNameForm.gender" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄">
              <el-input v-model="realNameForm.age" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="realNameForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="家庭住址">
          <el-input v-model="realNameForm.address" placeholder="请输入家庭住址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelRealName">取消</el-button>
        <el-button type="primary" @click="submitRealName">确认登记</el-button>
      </template>
    </el-dialog>

    <!-- 右键菜单（放在最外层避免被 overflow 截断） -->
    <Teleport to="body">
      <div v-if="memberContextMenu.visible" class="pos-context-menu" :style="{ top: memberContextMenu.y + 'px', left: memberContextMenu.x + 'px' }">
        <div class="context-menu-item" @click="goMemberPage('purchase-history')">购药记录</div>
        <div class="context-menu-item" @click="goMemberPage('reminder')">用药提醒</div>
        <div class="context-menu-item" @click="goMemberPage('health')">健康画像</div>
        <div class="context-menu-item" @click="goMemberPage('chronic-disease')">慢病管理</div>
      </div>
      <div v-if="settlementContextMenu.visible" class="pos-context-menu" :style="{ top: settlementContextMenu.y + 'px', left: settlementContextMenu.x + 'px' }">
        <div class="context-menu-item" @click="goReconciliation">日终对账</div>
      </div>
      <div v-if="cartContextMenu.visible" class="pos-context-menu" :style="{ top: cartContextMenu.y + 'px', left: cartContextMenu.x + 'px' }">
        <div class="context-menu-item context-menu-info">进货价：¥{{ cartContextMenu.costPrice }}</div>
        <div class="context-menu-item" @click="goEditDrug">药品列表</div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { onBeforeRouteLeave, useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, User, Delete, Wallet, ChatDotRound, CreditCard, FirstAidKit, ShoppingCart, Setting, Top, Bottom } from '@element-plus/icons-vue'
import { getDrugPage, getCategoryTree } from '@/api/drug'
import { searchMembers, getMemberLevelList, getMember } from '@/api/member'
import { createSaleOrder, createSuspendedOrder, getSuspendedOrderList, retrieveSuspendedOrder, deleteSuspendedOrder, getSuspendedOrderExpireMinutes, updateSuspendedOrderExpireMinutes } from '@/api/sale'
import { getConfigByGroup, setConfigValue, getConfigValue, getStoreList } from '@/api/system'
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()

// 搜索和药品列表
const searchKeyword = ref('')
const drugList = ref([])
const loading = ref(false)
const searchInputRef = ref(null)
const drugTableRef = ref(null)
const selectedIndex = ref(-1)
const selectedDrugId = ref(null)
const showZeroStock = ref(false)


// 分类快捷标签
// 分类快捷 - 中药饮片
const herbOnly = ref(false)
const herbCategoryIds = ref([])

// 购物车列配置
const CART_COLUMN_DEFS = [
  { key: 'genericName', label: '商品名称', prop: 'genericName', minWidth: 180, showOverflowTooltip: true },
  { key: 'specification', label: '规格', prop: 'specification', width: 100 },
  { key: 'manufacturer', label: '生产厂家', prop: 'manufacturer', minWidth: 150, showOverflowTooltip: true },
  { key: 'unit', label: '单位', prop: 'unit', width: 45, align: 'center' },
  { key: 'batchNo', label: '批号', prop: 'batchNo', width: 110, showOverflowTooltip: true },
  { key: 'expireDate', label: '到期时间', prop: 'expireDate', width: 92 },
  { key: 'traceCode', label: '追溯码', width: 130, custom: true },
  { key: 'quantity', label: '数量', width: 95, align: 'center', custom: true },
  { key: 'retailPrice', label: '零售价', width: 105, align: 'right', custom: true },
  { key: 'amount', label: '金额', width: 85, align: 'right', custom: true },
  { key: 'stock', label: '库存', prop: 'stock', width: 60, align: 'center' },
  { key: 'medicalInsurance', label: '医保', prop: 'medicalInsurance', width: 55, align: 'center', custom: true },
]

const COLUMN_ORDER_KEY = 'pos_cart_column_order'
const columnOrder = ref([])
const tableKey = ref(0)
const showColumnSettings = ref(false)

const sortedColumns = computed(() => {
  if (!columnOrder.value.length) return CART_COLUMN_DEFS
  const map = Object.fromEntries(CART_COLUMN_DEFS.map(c => [c.key, c]))
  return columnOrder.value.map(key => map[key]).filter(Boolean)
})

const loadColumnOrder = () => {
  try {
    const saved = localStorage.getItem(COLUMN_ORDER_KEY)
    if (saved) {
      const keys = JSON.parse(saved)
      const validKeys = CART_COLUMN_DEFS.map(c => c.key)
      const filtered = keys.filter(k => validKeys.includes(k))
      const missing = validKeys.filter(k => !filtered.includes(k))
      columnOrder.value = [...filtered, ...missing]
    } else {
      columnOrder.value = CART_COLUMN_DEFS.map(c => c.key)
    }
  } catch {
    columnOrder.value = CART_COLUMN_DEFS.map(c => c.key)
  }
}

const saveColumnOrder = () => {
  localStorage.setItem(COLUMN_ORDER_KEY, JSON.stringify(columnOrder.value))
  tableKey.value++
}

const moveColumnUp = (idx) => {
  if (idx <= 0) return
  const arr = [...columnOrder.value]
  ;[arr[idx - 1], arr[idx]] = [arr[idx], arr[idx - 1]]
  columnOrder.value = arr
  saveColumnOrder()
}

const moveColumnDown = (idx) => {
  if (idx >= columnOrder.value.length - 1) return
  const arr = [...columnOrder.value]
  ;[arr[idx], arr[idx + 1]] = [arr[idx + 1], arr[idx]]
  columnOrder.value = arr
  saveColumnOrder()
}

const resetColumnOrder = () => {
  columnOrder.value = CART_COLUMN_DEFS.map(c => c.key)
  saveColumnOrder()
}

// 购物车
const cartItems = ref([])

// 中药处方副数
const herbDoseCount = ref(1)

// 购物车分组
const herbCartItems = computed(() => cartItems.value.filter(item => item.isHerb))
const normalCartItems = computed(() => cartItems.value.filter(item => !item.isHerb))
const herbTotalGram = computed(() => herbCartItems.value.reduce((s, i) => s + (i.dosePerGram || 0), 0))
const herbTotalAmount = computed(() => herbCartItems.value.reduce((s, i) => s + (i.dosePerGram || 0) * herbDoseCount.value * getEffectivePrice(i), 0))

// 会员信息
const memberPhone = ref('')
const currentMember = ref(null)
const memberLevels = ref([])
const memberSearchList = ref([])
const memberSelectedIndex = ref(-1)

// 支付信息
const discountAmount = ref(0)
const paymentMethod = ref('CASH')
const cashReceived = ref(0)

// 整单折扣开关（持久化到 localStorage）
const WHOLE_DISCOUNT_KEY = 'pos_whole_order_discount_enabled'
const enableWholeOrderDiscount = ref(localStorage.getItem(WHOLE_DISCOUNT_KEY) !== 'false')
const toggleWholeOrderDiscount = (val) => {
  enableWholeOrderDiscount.value = val
  localStorage.setItem(WHOLE_DISCOUNT_KEY, val ? 'true' : 'false')
}

// 挂单相关
const retrieveDialogVisible = ref(false)
const suspendedOrders = ref([])
const suspendedLoading = ref(false)
const suspendedCount = ref(0)
const expireConfigVisible = ref(false)
const expireMinutes = ref(60)

// 打印相关
const printConfigVisible = ref(false)
const enablePrint = ref(false)
const receiptPaperWidth = ref('58')
const receiptShopName = ref('')
const receiptFooter = ref('感谢您的光临！\n如有问题请保留此小票\n药品为特殊商品，无质量问题概不退换')
const companyName = ref('')
const tenantName = ref('')
const storeAddress = ref('')
const storePhone = ref('')
const effectiveStoreId = ref(null)  // 实际有效的门店ID

// 小票字段默认配置
const DEFAULT_RECEIPT_FIELDS = {
  header: { shopName: true, tenantName: true, subtitle: true },
  orderInfo: { orderNo: true, dateTime: true, cashier: true },
  memberInfo: { memberName: true, memberPhone: true, memberPoints: true },
  itemDetail: { drugName: true, specification: true, batchNo: true, manufacturer: true, unitPrice: true },
  summary: { itemCount: true, totalAmount: true, memberDiscount: true, wholeDiscount: true, manualDiscount: true, payMethod: true, cashInfo: true },
  footer: { footerText: true }
}

// 字段分组标签映射（用于UI渲染）
const RECEIPT_FIELD_LABELS = {
  header: { label: '头部信息', fields: { shopName: '店名', tenantName: '租户名称', subtitle: '副标题' } },
  orderInfo: { label: '订单信息', fields: { orderNo: '单号', dateTime: '日期时间', cashier: '收银员' } },
  memberInfo: { label: '会员信息', fields: { memberName: '会员名称', memberPhone: '会员电话', memberPoints: '会员积分' } },
  itemDetail: { label: '商品明细列', fields: { drugName: '商品名', specification: '规格', batchNo: '批号', manufacturer: '生产厂家', unitPrice: '单价' } },
  summary: { label: '汇总区域', fields: { itemCount: '商品数量', totalAmount: '商品金额', memberDiscount: '会员价优惠', wholeDiscount: '整单折扣', manualDiscount: '手动优惠', payMethod: '支付方式', cashInfo: '现金/找零' } },
  footer: { label: '页脚', fields: { footerText: '页脚文本' } }
}

const receiptFields = ref(JSON.parse(JSON.stringify(DEFAULT_RECEIPT_FIELDS)))

// 实名登记相关
const realNameDialogVisible = ref(false)
const realNameFormRef = ref(null)
const realNameDrugs = ref([])
const realNameForm = reactive({ name: '', idCard: '', age: '', gender: '', phone: '', address: '' })
const realNameRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' },
           { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}
let pendingCheckoutResolve = null

// 计算属性

// 获取单品有效价格：有会员且有会员价时用会员价，否则用零售价
const getEffectivePrice = (item) => {
  if (currentMember.value && item.memberPrice && item.memberPrice > 0) {
    return item.memberPrice
  }
  return item.retailPrice
}

// 原始零售总额（未应用任何折扣）
const totalAmount = computed(() => {
  const normalTotal = normalCartItems.value.reduce(
    (sum, item) => sum + item.quantity * item.retailPrice,
    0
  )
  const herbTotal = herbCartItems.value.reduce(
    (sum, item) => sum + item.dosePerGram * herbDoseCount.value * item.retailPrice,
    0
  )
  return normalTotal + herbTotal
})

// 会员价后总额（会员价优先于整单折扣）
const afterMemberPriceTotal = computed(() => {
  const normalTotal = normalCartItems.value.reduce(
    (sum, item) => sum + item.quantity * getEffectivePrice(item),
    0
  )
  const herbTotal = herbCartItems.value.reduce(
    (sum, item) => sum + item.dosePerGram * herbDoseCount.value * getEffectivePrice(item),
    0
  )
  return normalTotal + herbTotal
})

// 会员价节省金额
const memberPriceSaving = computed(() => {
  return totalAmount.value - afterMemberPriceTotal.value
})

// 会员等级折扣
const memberDiscount = computed(() => {
  if (!currentMember.value || !currentMember.value.levelId) return 1
  const level = memberLevels.value.find(l => l.id === currentMember.value.levelId)
  if (!level || !level.discount) return 1
  // discount 存的是百分比值（如 95 表示 95折），需转为系数
  return level.discount > 1 ? level.discount / 100 : level.discount
})

// 整单折扣节省金额
const wholeOrderDiscountSaving = computed(() => {
  if (!enableWholeOrderDiscount.value || memberDiscount.value >= 1) return 0
  return afterMemberPriceTotal.value * (1 - memberDiscount.value)
})

const payAmount = computed(() => {
  let amount = afterMemberPriceTotal.value
  // 整单折扣（仅启用时生效）
  if (enableWholeOrderDiscount.value && memberDiscount.value < 1) {
    amount = amount * memberDiscount.value
  }
  return Math.max(0, amount - discountAmount.value)
})

// 找零金额
const changeAmount = computed(() => {
  return cashReceived.value - payAmount.value
})

// 搜索药品
const handleSearch = async () => {
  if (!searchKeyword.value.trim() && !herbOnly.value) {
    drugList.value = []
    selectedIndex.value = -1
    selectedDrugId.value = null
    return
  }

  try {
    loading.value = true
    const params = {
      current: 1,
      size: 20,
      showZeroStock: showZeroStock.value
    }
    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value
    }
    if (herbOnly.value && herbCategoryIds.value.length > 0) {
      params.categoryIds = herbCategoryIds.value.join(',')
    }
    const res = await getDrugPage(params)
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

// 切换中药饮片筛选
const toggleHerbOnly = () => {
  herbOnly.value = !herbOnly.value
  handleSearch()
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

  // 中药饮片走处方模式（通过isHerb字段、分类ID或herbType判断）
  const isDrugHerb = drug.isHerb || (drug.categoryId && herbCategoryIds.value.includes(drug.categoryId)) || (drug.herbType && drug.herbType.trim() !== '')
  if (isDrugHerb) {
    const existing = cartItems.value.find((item) => item.id === drug.id && item.isHerb)
    if (existing) {
      ElMessage.warning('该药材已在处方中')
      return
    }
    let defaultDose = 10
    if (drug.dosageMin && drug.dosageMax) {
      defaultDose =
        Math.round(((Number(drug.dosageMin) + Number(drug.dosageMax)) / 2) * 10) / 10
    } else if (drug.dosageMin) {
      defaultDose = Number(drug.dosageMin)
    } else if (drug.dosageMax) {
      defaultDose = Number(drug.dosageMax)
    }
    cartItems.value.push({
      ...drug,
      isHerb: true,
      dosePerGram: defaultDose,
      quantity: 1,
      stock: stock,
      batchNo: drug.batchNo || '-',
      expireDate: drug.expireDate || '-',
      traceCode: ''
    })
  } else {
    const existingItem = cartItems.value.find((item) => item.id === drug.id && !item.isHerb)
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
        expireDate: drug.expireDate || '-',
        traceCode: ''
      })
    }
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
      sums[index] = '¥' + data.reduce((sum, item) => sum + item.quantity * getEffectivePrice(item), 0).toFixed(2)
    } else {
      sums[index] = ''
    }
  })
  return sums
}

const getHerbSummaries = (param) => {
  const { columns } = param
  const sums = []
  const totalGram = herbCartItems.value.reduce((s, i) => s + i.dosePerGram, 0)
  const totalAmount = herbCartItems.value.reduce((s, i) => s + i.dosePerGram * herbDoseCount.value * getEffectivePrice(i), 0)
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = `每剂${totalGram.toFixed(1)}g`
      return
    }
    if (column.label === '每剂(g)') {
      sums[index] = `${herbDoseCount.value}副${(totalGram * herbDoseCount.value).toFixed(1)}g`
    } else if (column.label === '小计') {
      sums[index] = '¥' + totalAmount.toFixed(2)
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
    memberSelectedIndex.value = -1
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
        memberSelectedIndex.value = 0
        currentMember.value = null
      }
    } else {
      currentMember.value = null
      memberSearchList.value = []
      memberSelectedIndex.value = -1
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
  memberSelectedIndex.value = -1
  ElMessage.success('已选择会员：' + member.name)
}

// 清除会员
const clearMember = () => {
  currentMember.value = null
  memberPhone.value = ''
  memberSearchList.value = []
  memberSelectedIndex.value = -1
}

// 会员右键菜单
const router = useRouter()
const route = useRoute()
const memberContextMenu = reactive({ visible: false, x: 0, y: 0 })

const showMemberContextMenu = (e) => {
  memberContextMenu.x = e.clientX
  memberContextMenu.y = e.clientY
  memberContextMenu.visible = true
}

const hideMemberContextMenu = () => {
  memberContextMenu.visible = false
}

const goMemberPage = (page) => {
  hideMemberContextMenu()
  if (!currentMember.value) return
  const memberId = currentMember.value.id
  const memberName = currentMember.value.name
  const pathMap = {
    'purchase-history': '/member/purchase-history',
    'reminder': '/member/reminder',
    'health': '/member/health',
    'chronic-disease': '/member/chronic-disease'
  }
  router.push({ path: pathMap[page], query: { memberId, memberName, from: 'pos' } })
}

// 结算区右键菜单
const settlementContextMenu = reactive({ visible: false, x: 0, y: 0 })

const showSettlementContextMenu = (e) => {
  settlementContextMenu.x = e.clientX
  settlementContextMenu.y = e.clientY
  settlementContextMenu.visible = true
}

const hideSettlementContextMenu = () => {
  settlementContextMenu.visible = false
}

const goReconciliation = () => {
  hideSettlementContextMenu()
  router.push({ path: '/sale/reconciliation', query: { from: 'pos' } })
}

// 购物车右键菜单
const cartContextMenu = reactive({ visible: false, x: 0, y: 0, costPrice: '--', drugId: null })

const showCartContextMenu = (row, column, event) => {
  event.preventDefault()
  cartContextMenu.x = event.clientX
  cartContextMenu.y = event.clientY
  cartContextMenu.costPrice = row.purchasePrice != null ? String(row.purchasePrice) : (row.costPrice != null ? String(row.costPrice) : '--')
  cartContextMenu.drugId = row.drugId || row.id
  cartContextMenu.visible = true
}

const hideCartContextMenu = () => {
  cartContextMenu.visible = false
}

const goEditDrug = () => {
  const drugId = cartContextMenu.drugId
  hideCartContextMenu()
  router.push({ path: '/drug', query: { editId: drugId, from: 'pos' } })
}

const hideAllContextMenus = () => {
  hideMemberContextMenu()
  hideSettlementContextMenu()
  hideCartContextMenu()
}

onMounted(() => {
  document.addEventListener('click', hideAllContextMenus)
})
onUnmounted(() => {
  document.removeEventListener('click', hideAllContextMenus)
})

// 会员列表键盘导航
const handleMemberEnter = (e) => {
  if (e.isComposing) return
  if (memberSearchList.value.length > 0 && memberSelectedIndex.value >= 0) {
    selectMember(memberSearchList.value[memberSelectedIndex.value])
  } else {
    searchMember()
  }
}
const handleMemberArrowUp = (e) => {
  if (e.isComposing) return
  if (memberSearchList.value.length === 0) return
  if (memberSelectedIndex.value > 0) {
    memberSelectedIndex.value--
    scrollMemberItemIntoView()
  }
}
const handleMemberArrowDown = (e) => {
  if (e.isComposing) return
  if (memberSearchList.value.length === 0) return
  if (memberSelectedIndex.value < memberSearchList.value.length - 1) {
    memberSelectedIndex.value++
    scrollMemberItemIntoView()
  }
}
const scrollMemberItemIntoView = () => {
  nextTick(() => {
    const activeItem = document.querySelector('.member-search-item.active')
    if (activeItem) {
      activeItem.scrollIntoView({ block: 'nearest' })
    }
  })
}

// 挂单
const handleHangUp = async () => {
  try {
    const orderData = {
      memberId: currentMember.value?.id,
      memberName: currentMember.value?.name,
      items: JSON.stringify({ _herbDoseCount: herbDoseCount.value, items: cartItems.value }),
      totalAmount: totalAmount.value
    }
    
    const res = await createSuspendedOrder(orderData)
    if (res.code === 200) {
      ElMessage.success(`挂单成功，单号: ${res.data.orderNo}`)
      // 清空购物车
      cartItems.value = []
      herbDoseCount.value = 1
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
      // 恢复购物车（兼容新旧格式）
      const parsed = JSON.parse(res.data.items || '[]')
      if (parsed._herbDoseCount !== undefined && Array.isArray(parsed.items)) {
        herbDoseCount.value = parsed._herbDoseCount || 1
        cartItems.value = parsed.items
      } else if (Array.isArray(parsed)) {
        herbDoseCount.value = 1
        cartItems.value = parsed
      } else {
        herbDoseCount.value = 1
        cartItems.value = []
      }
      
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

// 加载小票打印配置
const loadReceiptConfig = async () => {
  try {
    const res = await getConfigByGroup('sale')
    if (res.code === 200 && res.data) {
      const configs = {}
      const dataArr = Array.isArray(res.data) ? res.data : []
      dataArr.forEach(item => { configs[item.configKey] = item.configValue })
      
      if (configs['sale.receipt_printer'] !== undefined) {
        enablePrint.value = configs['sale.receipt_printer'] === 'true'
      }
      if (configs['sale.receipt_paper_width']) {
        receiptPaperWidth.value = configs['sale.receipt_paper_width']
      }
      if (configs['sale.receipt_shop_name'] !== undefined) {
        receiptShopName.value = configs['sale.receipt_shop_name']
      }
      if (configs['sale.receipt_footer'] !== undefined) {
        receiptFooter.value = configs['sale.receipt_footer']
      }
      if (configs['sale.receipt_fields']) {
        try {
          receiptFields.value = JSON.parse(configs['sale.receipt_fields'])
        } catch (e) {
          receiptFields.value = JSON.parse(JSON.stringify(DEFAULT_RECEIPT_FIELDS))
        }
      }
    }
    // 获取企业名称作为店名回退
    try {
      const nameRes = await getConfigValue('system.company_name')
      if (nameRes.code === 200 && nameRes.data) {
        companyName.value = nameRes.data
      }
    } catch (e) { /* ignore */ }
    // 获取租户名称
    tenantName.value = authStore.user?.tenantName || ''
    // 获取门店地址和电话，同时确定有效的门店ID
    try {
      const storeRes = await getStoreList()
      if (storeRes.code === 200 && storeRes.data?.length > 0) {
        const stores = storeRes.data
        storeAddress.value = stores[0].address || ''
        storePhone.value = stores[0].phone || ''
        // 优先用户所属门店，若不在列表中则取第一个可用门店
        const userStoreId = authStore.user?.storeId
        const matched = userStoreId ? stores.find(s => s.id === userStoreId) : null
        effectiveStoreId.value = matched ? matched.id : stores[0].id
      }
    } catch (e) { /* ignore */ }
  } catch (error) {
    console.warn('加载打印配置失败', error)
  }
}

// 保存小票打印配置
const saveReceiptConfig = async () => {
  try {
    await Promise.all([
      setConfigValue({ group: 'sale', key: 'sale.receipt_printer', value: enablePrint.value ? 'true' : 'false', valueType: 'boolean', description: '是否启用小票打印' }),
      setConfigValue({ group: 'sale', key: 'sale.receipt_paper_width', value: receiptPaperWidth.value, valueType: 'string', description: '小票纸张宽度(58/80mm)' }),
      setConfigValue({ group: 'sale', key: 'sale.receipt_shop_name', value: receiptShopName.value, valueType: 'string', description: '小票店名' }),
      setConfigValue({ group: 'sale', key: 'sale.receipt_footer', value: receiptFooter.value, valueType: 'string', description: '小票页脚文本' }),
      setConfigValue({ group: 'sale', key: 'sale.receipt_fields', value: JSON.stringify(receiptFields.value), valueType: 'json', description: '小票打印字段配置' })
    ])
    ElMessage.success('打印配置已保存')
    printConfigVisible.value = false
  } catch (error) {
    ElMessage.error('保存打印配置失败')
  }
}

// 恢复默认打印配置
const resetReceiptConfig = () => {
  receiptPaperWidth.value = '58'
  receiptShopName.value = ''
  receiptFooter.value = '感谢您的光临！\n如有问题请保留此小票\n药品为特殊商品，无质量问题概不退换'
  receiptFields.value = JSON.parse(JSON.stringify(DEFAULT_RECEIPT_FIELDS))
}

// 打印小票
// ========== 小票打印三层架构 ==========

// 第一层：收集小票数据
const buildReceiptData = (orderNo) => {
  const now = new Date()
  const dateStr = now.toLocaleString('zh-CN')
  const shopName = receiptShopName.value || companyName.value || '药房'
  const cashierName = authStore.user?.realName || '收银员'
  
  return {
    shopName,
    tenantName: tenantName.value || '',
    storeAddress: storeAddress.value || '',
    storePhone: storePhone.value || '',
    subtitle: '销售小票',
    orderNo: orderNo || '-',
    dateTime: dateStr,
    cashierName,
    member: currentMember.value ? {
      name: currentMember.value.name,
      phone: currentMember.value.phone,
      points: currentMember.value.points || 0
    } : null,
    items: normalCartItems.value.map(item => {
      const ep = getEffectivePrice(item)
      return {
        name: item.genericName || item.tradeName,
        specification: item.specification || '-',
        batchNo: item.batchNo || '-',
        quantity: item.quantity,
        amount: (item.quantity * ep).toFixed(2),
        manufacturer: item.manufacturer || '',
        unitPrice: ep.toFixed(2)
      }
    }),
    itemCount: normalCartItems.value.reduce((sum, item) => sum + item.quantity, 0),
    totalAmount: totalAmount.value.toFixed(2),
    memberPriceSaving: memberPriceSaving.value,
    wholeOrderDiscountSaving: wholeOrderDiscountSaving.value,
    memberDiscountRate: memberDiscount.value,
    enableWholeOrderDiscount: enableWholeOrderDiscount.value,
    discountAmount: discountAmount.value,
    paymentLabel: getPaymentLabel(paymentMethod.value),
    paymentMethod: paymentMethod.value,
    cashReceived: cashReceived.value,
    changeAmount: changeAmount.value,
    payAmount: payAmount.value.toFixed(2),
    footerText: receiptFooter.value || '感谢您的光临！\n如有问题请保留此小票\n药品为特殊商品，无质量问题概不退换'
  }
}

// 第二层：根据配置生成 HTML
const generateReceiptHtml = (data, fields, paperWidth) => {
  const is58 = paperWidth === '58'
  const bodyWidth = is58 ? '58mm' : '80mm'
  const bodyPadding = is58 ? '3mm' : '5mm'
  const baseFontSize = is58 ? '11px' : '12px'
  const shopNameSize = is58 ? '14px' : '16px'
  const tenantNameSize = is58 ? '11px' : '12px'
  const totalFontSize = is58 ? '18px' : '22px'
  const mfgMaxLen = is58 ? 12 : 20

  // -- 头部 --
  let headerHtml = ''
  if (fields.header?.shopName) {
    headerHtml += `<div class="shop-name" style="font-size:${shopNameSize};">${data.shopName}</div>`
  }
  if (fields.header?.tenantName && data.tenantName && data.tenantName !== data.shopName && !data.shopName.includes(data.tenantName) && !data.tenantName.includes(data.shopName)) {
    headerHtml += `<div style="font-size:${tenantNameSize};color:#444;margin-top:2px;">${data.tenantName}</div>`
  }
  if (data.storeAddress) {
    headerHtml += `<div style="font-size:${is58 ? '10px' : '11px'};color:#333;margin-top:2px;">地址：${data.storeAddress}</div>`
  }
  if (data.storePhone) {
    headerHtml += `<div style="font-size:${is58 ? '10px' : '11px'};color:#333;margin-top:1px;">电话：${data.storePhone}</div>`
  }

  // -- 订单信息 --
  let orderInfoHtml = ''
  if (fields.orderInfo?.orderNo) {
    orderInfoHtml += `<div class="info-row"><span>单号:</span><span>${data.orderNo}</span></div>`
  }
  if (fields.orderInfo?.dateTime) {
    orderInfoHtml += `<div class="info-row"><span>时间:</span><span>${data.dateTime}</span></div>`
  }
  if (fields.orderInfo?.cashier) {
    orderInfoHtml += `<div class="info-row"><span>收银员:</span><span>${data.cashierName}</span></div>`
  }

  // -- 会员信息 --
  let memberInfoHtml = ''
  if (data.member) {
    const parts = []
    if (fields.memberInfo?.memberName) parts.push(data.member.name)
    if (fields.memberInfo?.memberPhone) parts.push(data.member.phone)
    if (parts.length > 0) {
      memberInfoHtml = `<div class="info-row"><span>会员:</span><span>${parts.join(' ')}</span></div>`
    }
    if (fields.memberInfo?.memberPoints !== false) {
      memberInfoHtml += `<div class="info-row"><span>积分余额:</span><span>${data.member.points} 分</span></div>`
    }
  }

  // -- 商品明细 --
  // 动态表头列
  const showSpec = fields.itemDetail?.specification !== false
  const showBatch = fields.itemDetail?.batchNo !== false
  const showMfg = fields.itemDetail?.manufacturer !== false
  const showUnitPrice = fields.itemDetail?.unitPrice !== false

  let theadCols = ''
  if (showSpec) theadCols += '<td>规格</td>'
  if (showBatch) theadCols += '<td>批号</td>'
  theadCols += '<td style="text-align:right;">数量</td><td style="text-align:right;">金额</td>'

  const colCount = (showSpec ? 1 : 0) + (showBatch ? 1 : 0) + 2

  const itemsHtml = data.items.map(item => {
    let rows = ''
    // 商品名称行
    if (fields.itemDetail?.drugName !== false) {
      rows += `<tr><td colspan="${colCount}" style="text-align:left;padding:2px 0;border-bottom:1px dashed #ccc;">${item.name}</td></tr>`
    }
    // 明细数据行
    let detailCols = ''
    if (showSpec) detailCols += `<td style="font-size:${baseFontSize};color:#000;">${item.specification}</td>`
    if (showBatch) detailCols += `<td style="font-size:${baseFontSize};color:#000;">${item.batchNo}</td>`
    detailCols += `<td style="text-align:right;">${item.quantity}</td>`
    detailCols += `<td style="text-align:right;">¥${item.amount}</td>`
    rows += `<tr>${detailCols}</tr>`
    // 厂家/单价行
    const extraParts = []
    if (showMfg && item.manufacturer) {
      const mfg = item.manufacturer.length > mfgMaxLen ? item.manufacturer.substring(0, mfgMaxLen) + '...' : item.manufacturer
      extraParts.push(mfg)
    }
    if (showUnitPrice) extraParts.push('单价:¥' + item.unitPrice)
    if (extraParts.length > 0) {
      rows += `<tr><td colspan="${colCount}" style="font-size:${is58 ? '10px' : '11px'};color:#000;padding-bottom:4px;">${extraParts.join(' | ')}</td></tr>`
    }
    return rows
  }).join('')

  // -- 汇总区域 --
  let summaryHtml = ''
  if (fields.summary?.itemCount !== false) {
    summaryHtml += `<div class="total-row"><span>商品数量:</span><span>${data.itemCount} 件</span></div>`
  }
  if (fields.summary?.totalAmount !== false) {
    summaryHtml += `<div class="total-row"><span>商品金额:</span><span>¥${data.totalAmount}</span></div>`
  }
  if (fields.summary?.memberDiscount !== false && data.memberPriceSaving > 0) {
    summaryHtml += `<div class="total-row"><span>会员价优惠:</span><span>-¥${data.memberPriceSaving.toFixed(2)}</span></div>`
  }
  if (fields.summary?.wholeDiscount !== false && data.enableWholeOrderDiscount && data.wholeOrderDiscountSaving > 0) {
    summaryHtml += `<div class="total-row"><span>整单折扣(${(data.memberDiscountRate * 10).toFixed(1)}折):</span><span>-¥${data.wholeOrderDiscountSaving.toFixed(2)}</span></div>`
  }
  if (fields.summary?.manualDiscount !== false && data.discountAmount > 0) {
    summaryHtml += `<div class="total-row"><span>手动优惠:</span><span>-¥${data.discountAmount.toFixed(2)}</span></div>`
  }
  if (fields.summary?.payMethod !== false) {
    summaryHtml += `<div class="total-row"><span>支付方式:</span><span>${data.paymentLabel}</span></div>`
  }
  if (fields.summary?.cashInfo !== false && data.paymentMethod === 'CASH' && data.cashReceived > 0) {
    summaryHtml += `<div class="total-row"><span>收取现金:</span><span>¥${data.cashReceived.toFixed(2)}</span></div>`
    summaryHtml += `<div class="total-row"><span>找零:</span><span>¥${data.changeAmount.toFixed(2)}</span></div>`
  }
  // 实付金额始终显示
  summaryHtml += `<div class="total-row total-amount"><span>实付金额:</span><span style="color:#000;font-weight:900;">¥${data.payAmount}</span></div>`

  // -- 页脚 --
  let footerHtml = ''
  if (fields.footer?.footerText !== false) {
    const lines = data.footerText.split(/\\n|\n/).map(l => `<p>${l}</p>`).join('')
    footerHtml = `<div class="footer">${lines}</div>`
  }

  return `<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>销售小票</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body { font-family: 'Microsoft YaHei', 'SimHei', sans-serif; width: 100%; max-width: ${bodyWidth}; padding: ${bodyPadding}; font-size: ${baseFontSize}; color: #000; margin: 0 auto; }
    .header { text-align: center; margin-bottom: 10px; }
    .shop-name { font-weight: bold; }
    .divider { border-top: 1px dashed #000; margin: 8px 0; }
    .info-row { display: flex; justify-content: space-between; margin: 3px 0; }
    .items-table { width: 100%; border-collapse: collapse; margin: 8px 0; table-layout: fixed; }
    .items-table td { padding: 2px 0; vertical-align: top; word-break: break-all; }
    .total-section { margin-top: 10px; }
    .total-row { display: flex; justify-content: space-between; margin: 4px 0; }
    .total-amount { font-size: ${totalFontSize}; font-weight: bold; }
    .footer { text-align: center; margin-top: 15px; font-size: ${is58 ? '11px' : '12px'}; color: #000; }
    .footer p { margin-top: 3px; }
    @page { size: ${bodyWidth} auto; margin: 0mm; }
    @media print { body { width: 100%; max-width: none; margin: 0; padding: ${bodyPadding}; } }
  </style>
</head>
<body>
  ${headerHtml ? `<div class="header">${headerHtml}</div>` : ''}
  <div class="divider"></div>
  ${orderInfoHtml}${memberInfoHtml}
  <div class="divider"></div>
  <table class="items-table">
    <thead><tr style="font-weight:bold;border-bottom:1px solid #000;">${theadCols}</tr></thead>
    <tbody>${itemsHtml}</tbody>
  </table>
  <div class="divider"></div>
  <div class="total-section">${summaryHtml}</div>
  <div class="divider"></div>
  ${footerHtml}
</body>
</html>`
}

// 第三层：通过隐藏 iframe 执行打印（不打开新窗口）
const printViaIframe = (html) => {
  // 清理上一次遗留的打印 iframe，避免空白页
  document.querySelectorAll('iframe[data-print-frame]').forEach(el => {
    if (el.parentNode) el.parentNode.removeChild(el)
  })

  const iframe = document.createElement('iframe')
  iframe.setAttribute('data-print-frame', '1')
  iframe.style.cssText = 'position:fixed;left:-9999px;top:-9999px;width:80mm;height:1px;border:none;visibility:hidden;'
  document.body.appendChild(iframe)

  iframe.onload = () => {
    setTimeout(() => {
      try {
        iframe.contentWindow.focus()
        iframe.contentWindow.print()
      } catch (e) { /* ignore */ }
    }, 100)
  }

  iframe.contentWindow.onafterprint = () => {
    if (iframe.parentNode) document.body.removeChild(iframe)
  }

  const doc = iframe.contentDocument || iframe.contentWindow.document
  doc.open()
  doc.write(html)
  doc.close()

  // 兜底：3秒后自动移除 iframe
  setTimeout(() => {
    if (iframe.parentNode) document.body.removeChild(iframe)
  }, 3000)
}

const printReceipt = (orderNo) => {
  const data = buildReceiptData(orderNo)
  const html = generateReceiptHtml(data, receiptFields.value, receiptPaperWidth.value)
  printViaIframe(html)
}

// ========== 中药处方笺打印 ==========

const buildHerbReceiptData = (orderNo) => {
  const now = new Date()
  const dateStr = now.toLocaleString('zh-CN')
  const shopName = receiptShopName.value || companyName.value || '药房'
  const cashierName = authStore.user?.realName || '收银员'
  const dc = herbDoseCount.value

  const items = herbCartItems.value.map((item) => {
    const ep = getEffectivePrice(item)
    const totalGram = Math.round(item.dosePerGram * dc * 10) / 10
    return {
      name: item.genericName || item.tradeName,
      dosePerGram: item.dosePerGram,
      totalGram,
      unitPrice: ep,
      amount: (item.dosePerGram * dc * ep).toFixed(2)
    }
  })

  const perDoseTotalWeight = herbCartItems.value.reduce((s, i) => s + i.dosePerGram, 0)
  const totalWeight = perDoseTotalWeight * dc
  const herbTotal = herbCartItems.value.reduce(
    (s, i) => s + i.dosePerGram * dc * getEffectivePrice(i),
    0
  )

  return {
    shopName,
    tenantName: tenantName.value || '',
    storeAddress: storeAddress.value || '',
    storePhone: storePhone.value || '',
    subtitle: '中药处方笺',
    orderNo: orderNo || '-',
    dateTime: dateStr,
    cashierName,
    member: currentMember.value
      ? { name: currentMember.value.name, phone: currentMember.value.phone, points: currentMember.value.points || 0 }
      : null,
    doseCount: dc,
    items,
    perDoseTotalWeight: Math.round(perDoseTotalWeight * 10) / 10,
    totalWeight: Math.round(totalWeight * 10) / 10,
    herbTotal: herbTotal.toFixed(2),
    payAmount: payAmount.value.toFixed(2),
    paymentLabel: getPaymentLabel(paymentMethod.value),
    paymentMethod: paymentMethod.value,
    cashReceived: cashReceived.value,
    changeAmount: changeAmount.value,
    discountAmount: discountAmount.value,
    footerText: receiptFooter.value || '感谢您的光临！\n如有问题请保留此小票\n药品为特殊商品，无质量问题概不退换'
  }
}

const generateHerbReceiptHtml = (data, paperWidth) => {
  const is58 = paperWidth === '58'
  const bodyWidth = is58 ? '58mm' : '80mm'
  const bodyPadding = is58 ? '3mm' : '5mm'
  const baseFontSize = is58 ? '12px' : '13px'
  const shopNameSize = is58 ? '14px' : '16px'
  const tenantNameSize = is58 ? '11px' : '12px'
  const totalFontSize = is58 ? '16px' : '20px'

  // 表头 — 与普通小票一致
  const fields = receiptFields.value
  let headerHtml = ''
  if (fields.header?.shopName !== false) {
    headerHtml += `<div class="shop-name" style="font-size:${shopNameSize};">${data.shopName}</div>`
  }
  if (fields.header?.tenantName !== false && data.tenantName && data.tenantName !== data.shopName && !data.shopName.includes(data.tenantName) && !data.tenantName.includes(data.shopName)) {
    headerHtml += `<div style="font-size:${tenantNameSize};color:#444;margin-top:2px;">${data.tenantName}</div>`
  }
  if (data.storeAddress) {
    headerHtml += `<div style="font-size:${is58 ? '10px' : '11px'};color:#333;margin-top:2px;">地址：${data.storeAddress}</div>`
  }
  if (data.storePhone) {
    headerHtml += `<div style="font-size:${is58 ? '10px' : '11px'};color:#333;margin-top:1px;">电话：${data.storePhone}</div>`
  }
  headerHtml += `<div style="font-size:${is58 ? '14px' : '16px'};font-weight:bold;margin-top:4px;">中药处方笺</div>`

  // 订单信息 — 与普通小票一致的 flex 布局
  let orderInfoHtml = ''
  if (fields.orderInfo?.orderNo !== false) {
    orderInfoHtml += `<div class="info-row"><span>单号:</span><span>${data.orderNo}</span></div>`
  }
  if (fields.orderInfo?.dateTime !== false) {
    orderInfoHtml += `<div class="info-row"><span>时间:</span><span>${data.dateTime}</span></div>`
  }
  if (fields.orderInfo?.cashier !== false) {
    orderInfoHtml += `<div class="info-row"><span>收银员:</span><span>${data.cashierName}</span></div>`
  }

  // 会员信息 — 与普通小票一致
  let memberInfoHtml = ''
  if (data.member) {
    const parts = []
    if (fields.memberInfo?.memberName !== false) parts.push(data.member.name)
    if (fields.memberInfo?.memberPhone !== false) parts.push(data.member.phone)
    if (parts.length > 0) {
      memberInfoHtml += `<div class="info-row"><span>会员:</span><span>${parts.join(' ')}</span></div>`
    }
    if (fields.memberInfo?.memberPoints !== false) {
      memberInfoHtml += `<div class="info-row"><span>积分余额:</span><span>${data.member.points} 分</span></div>`
    }
  }

  // 明细列：药名、克/剂、总重(g)、单价、小计
  const itemsHtml = data.items
    .map(
      (item) => `
    <tr>
      <td style="padding:3px 2px;">${item.name}</td>
      <td style="text-align:right;padding:3px 2px;">${item.dosePerGram}</td>
      <td style="text-align:right;padding:3px 2px;">${item.totalGram}</td>
      <td style="text-align:right;padding:3px 2px;">${item.unitPrice.toFixed(2)}</td>
      <td style="text-align:right;padding:3px 2px;">${item.amount}</td>
    </tr>`
    )
    .join('')

  // 合计区
  let summaryHtml = ''
  summaryHtml += `<div class="sum-row"><span>每剂总重:</span><span>${data.perDoseTotalWeight}g</span></div>`
  summaryHtml += `<div class="sum-row"><span>${data.doseCount}副总重:</span><span>${data.totalWeight}g</span></div>`
  summaryHtml += `<div class="sum-row"><span>商品金额:</span><span>¥${data.herbTotal}</span></div>`
  if (data.discountAmount > 0) {
    summaryHtml += `<div class="sum-row"><span>优惠:</span><span>-¥${data.discountAmount.toFixed(2)}</span></div>`
  }
  summaryHtml += `<div class="sum-row"><span>支付方式:</span><span>${data.paymentLabel}</span></div>`
  if (data.paymentMethod === 'CASH' && data.cashReceived > 0) {
    summaryHtml += `<div class="sum-row"><span>收取现金:</span><span>¥${data.cashReceived.toFixed(2)}</span></div>`
    summaryHtml += `<div class="sum-row"><span>找零:</span><span>¥${data.changeAmount.toFixed(2)}</span></div>`
  }
  summaryHtml += `<div class="sum-row total-amount"><span>实付金额:</span><span>¥${data.payAmount}</span></div>`

  return `<!DOCTYPE html><html><head><meta charset="utf-8">
<style>
  @page { size: ${bodyWidth} auto; margin: 0mm; }
  * { margin: 0; padding: 0; box-sizing: border-box; }
  body { font-family: 'Microsoft YaHei','SimHei',sans-serif; font-size: ${baseFontSize}; color: #000; margin: 0 auto; padding: ${bodyPadding}; width: 100%; max-width: ${bodyWidth}; -webkit-print-color-adjust: exact; }
  .header { text-align: center; margin-bottom: 10px; }
  .shop-name { font-weight: bold; }
  .divider { border-top: 1px dashed #000; margin: 8px 0; }
  .info-row { display: flex; justify-content: space-between; margin: 3px 0; }
  .dose-label { text-align: center; font-size: ${is58 ? '15px' : '17px'}; font-weight: 900; padding: 6px 0; }
  .items-table { width: 100%; border-collapse: collapse; font-size: ${is58 ? '11px' : '12px'}; }
  .items-table th, .items-table td { padding: 3px 2px; }
  .items-table th { border-bottom: 1px solid #000; text-align: left; font-weight: 700; }
  .items-table th:nth-child(n+2), .items-table td:nth-child(n+2) { text-align: right; }
  .sum-row { display: flex; justify-content: space-between; margin: 3px 0; font-size: ${baseFontSize}; }
  .total-amount { font-size: ${totalFontSize}; font-weight: 900; }
  .footer { text-align: center; margin-top: 15px; font-size: ${is58 ? '11px' : '12px'}; color: #000; }
  .footer p { margin-top: 3px; }
  @media print { body { width: 100%; max-width: none; margin: 0; padding: ${bodyPadding}; } }
</style></head><body>
<div class="header">${headerHtml}</div>
<div class="divider"></div>
${orderInfoHtml}${memberInfoHtml}
<div class="divider"></div>
<div class="dose-label">${data.doseCount} 副</div>
<div class="divider"></div>
<table class="items-table">
  <tr><th>药名</th><th>克/剂</th><th>总重</th><th>单价</th><th>小计</th></tr>
  ${itemsHtml}
</table>
<div class="divider"></div>
${summaryHtml}
<div class="divider"></div>
<div class="footer">${data.footerText.split(/\\n|\n/).map(l => '<p>' + l + '</p>').join('')}</div>
</body></html>`
}

const printHerbReceipt = (orderNo) => {
  const data = buildHerbReceiptData(orderNo)
  const html = generateHerbReceiptHtml(data, receiptPaperWidth.value)
  printViaIframe(html)
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
    const parsed = JSON.parse(itemsJson || '[]')
    const items = Array.isArray(parsed) ? parsed : (parsed.items || [])
    return items.map((item) => item.genericName || item.tradeName).join('、')
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
    // 检查实名登记
    const rnDrugs = cartItems.value.filter(item => item.requireRealName)
    if (rnDrugs.length > 0) {
      realNameDrugs.value = rnDrugs
      Object.assign(realNameForm, { name: '', idCard: '', age: '', gender: '', phone: '', address: '' })
      realNameDialogVisible.value = true
      const confirmed = await new Promise(resolve => { pendingCheckoutResolve = resolve })
      if (!confirmed) return
    }

    const orderData = {
      saleOrder: {
        memberId: currentMember.value?.id,
        cashierId: authStore.user?.userId,
        totalAmount: totalAmount.value,
        discountAmount:
          memberPriceSaving.value + wholeOrderDiscountSaving.value + discountAmount.value,
        payAmount: payAmount.value,
        payMethod: paymentMethod.value,
        storeId: effectiveStoreId.value || authStore.user?.storeId || 1,
        herbDoseCount: herbCartItems.value.length > 0 ? herbDoseCount.value : null,
        remark:
          rnDrugs.length > 0
            ? '实名登记: ' + JSON.stringify(realNameForm)
            : undefined
      },
      details: cartItems.value.map((item) => {
        if (item.isHerb) {
          const ep = getEffectivePrice(item)
          const totalGram = item.dosePerGram * herbDoseCount.value
          return {
            drugId: item.id,
            drugName: item.genericName,
            specification: item.specification,
            quantity: totalGram,
            unit: 'g',
            unitPrice: ep,
            discount: 0,
            amount: totalGram * ep,
            batchId: item.batchId || null,
            batchNo: item.batchNo || null,
            isHerb: true,
            dosePerGram: item.dosePerGram,
            doseCount: herbDoseCount.value
          }
        }
        return {
          drugId: item.id,
          drugName: item.genericName,
          specification: item.specification,
          quantity: item.quantity,
          unitPrice: getEffectivePrice(item),
          discount: 0,
          amount: item.quantity * getEffectivePrice(item),
          batchId: item.batchId || null,
          batchNo: item.batchNo || null
        }
      })
    }

    const res = await createSaleOrder(orderData)
    if (res.code === 200) {
      const orderNo = res.data?.orderNo || ''
      
      // 打印小票（始终自动打印）— 先生成 HTML 再延迟打印，避免清空购物车后数据丢失
      let receiptHtml = null
      let herbReceiptHtml = null
      if (normalCartItems.value.length > 0) {
        const data = buildReceiptData(orderNo)
        receiptHtml = generateReceiptHtml(data, receiptFields.value, receiptPaperWidth.value)
      }
      if (herbCartItems.value.length > 0) {
        const data = buildHerbReceiptData(orderNo)
        herbReceiptHtml = generateHerbReceiptHtml(data, receiptPaperWidth.value)
      }

      if (receiptHtml) {
        printViaIframe(receiptHtml)
      }
      if (herbReceiptHtml) {
        setTimeout(() => printViaIframe(herbReceiptHtml), 500)
      }
      
      ElMessage.success(`结算成功，订单号: ${orderNo}`)
      // 重置状态
      cartItems.value = []
      herbDoseCount.value = 1
      discountAmount.value = 0
      paymentMethod.value = 'CASH'
      cashReceived.value = 0
      memberPhone.value = ''
      currentMember.value = null
      searchKeyword.value = ''
    }
  } catch (error) {
    ElMessage.error('结算失败：' + error.message)
  }
}

// 从身份证号解析性别和年龄
const parseIdCard = () => {
  const id = realNameForm.idCard
  if (!id || id.length !== 18) return
  // 出生日期: 第7-14位
  const birthYear = parseInt(id.substring(6, 10))
  const birthMonth = parseInt(id.substring(10, 12))
  const birthDay = parseInt(id.substring(12, 14))
  if (birthYear > 1900 && birthYear < 2100) {
    const now = new Date()
    let age = now.getFullYear() - birthYear
    if (now.getMonth() + 1 < birthMonth || (now.getMonth() + 1 === birthMonth && now.getDate() < birthDay)) {
      age--
    }
    realNameForm.age = age + '岁'
  }
  // 性别: 第17位，奇数=男，偶数=女
  const genderCode = parseInt(id.charAt(16))
  realNameForm.gender = genderCode % 2 === 1 ? '男' : '女'
}

// 提交实名登记
const submitRealName = async () => {
  if (!realNameFormRef.value) return
  await realNameFormRef.value.validate((valid) => {
    if (valid) {
      realNameDialogVisible.value = false
      pendingCheckoutResolve?.(true)
      pendingCheckoutResolve = null
    }
  })
}

// 取消实名登记
const cancelRealName = () => {
  realNameDialogVisible.value = false
  pendingCheckoutResolve?.(false)
  pendingCheckoutResolve = null
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

onMounted(async () => {
  window.addEventListener('keydown', handleKeyDown)
  loadMemberLevels()
  loadSuspendedCount()
  loadCategories()
  loadColumnOrder()
  loadReceiptConfig()

  // 从会员页面返回时恢复会员
  if (route.query.memberId) {
    try {
      const res = await getMember(route.query.memberId)
      if (res.code === 200 && res.data) {
        selectMember(res.data)
      }
    } catch (e) { /* ignore */ }
  }
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
})

// 离开页面时，购物车有商品则提示挂单
onBeforeRouteLeave((to, from, next) => {
  if (cartItems.value.length > 0) {
    ElMessageBox.confirm(
      '购物车中还有商品，是否挂单保存？',
      '提示',
      {
        confirmButtonText: '挂单',
        cancelButtonText: '放弃',
        type: 'warning',
      }
    ).then(() => {
      handleHangUp().then(() => next()).catch(() => next())
    }).catch(() => {
      next()
    })
  } else {
    next()
  }
})

// 加载会员等级列表
const loadMemberLevels = async () => {
  try {
    const res = await getMemberLevelList()
    if (res.code === 200) { memberLevels.value = res.data || [] }
  } catch { /* 静默 */ }
}

// 加载药品分类 - 识别中药饮片相关分类ID（含子分类）
const HERB_KEYWORDS = ['中药饮片', '中草药', '中药']
const collectChildIds = (node) => {
  const ids = [node.id]
  if (node.children && node.children.length > 0) {
    for (const child of node.children) {
      ids.push(...collectChildIds(child))
    }
  }
  return ids
}
const matchHerbRecursive = (nodes) => {
  const ids = []
  for (const node of nodes) {
    if (HERB_KEYWORDS.some(kw => node.name.includes(kw))) {
      ids.push(...collectChildIds(node))
    } else if (node.children && node.children.length > 0) {
      ids.push(...matchHerbRecursive(node.children))
    }
  }
  return ids
}
const loadCategories = async () => {
  try {
    const res = await getCategoryTree()
    if (res.code === 200) {
      const all = res.data || []
      // 递归匹配含"中药饮片/中草药/中药"关键词的分类（含所有层级），并收集其所有子分类ID
      const ids = matchHerbRecursive(all)
      herbCategoryIds.value = ids.length > 0 ? ids : []
    }
  } catch { /* 静默 */ }
}
</script>

<style scoped lang="scss">
.pos-layout {
  display: flex;
  gap: 20px;
  height: calc(100vh - 120px);
  padding: 20px;
  background: linear-gradient(135deg, #e9eef5 0%, #dfe6ef 100%);

  .pos-left {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 16px;
    min-width: 0;
  }

  .pos-right {
    width: 380px;
    flex-shrink: 0;
  }

  // === 卡片通用样式 ===
  .cart-card,
  .search-card,
  .payment-card {
    background: #ffffff;
    border-radius: 12px;
    border: none;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06), 0 0 1px rgba(0, 0, 0, 0.08);
    overflow: hidden;

    :deep(.el-card__header) {
      background: linear-gradient(135deg, #f6f8fb 0%, #eef2f7 100%);
      border-bottom: 1px solid #e8ecf1;
      padding: 16px 24px;
    }

    :deep(.el-card__body) {
      background: #ffffff;
      padding: 20px;
    }
  }

  .card-header {
    display: flex;
    align-items: center;
    font-weight: 700;
    font-size: 16px;
    color: #1a2332;
    gap: 12px;
    letter-spacing: 0.5px;
  }

  .herb-toggle {
    display: inline-block;
    padding: 4px 14px;
    font-size: 13px;
    font-weight: 500;
    color: #606a78;
    background: #eef1f6;
    border-radius: 16px;
    cursor: pointer;
    transition: all 0.25s ease;
    user-select: none;

    &:hover {
      color: #409eff;
      background: #e0edfc;
    }

    &.active {
      color: #fff;
      background: linear-gradient(135deg, #409eff, #2d7cd6);
      box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
    }
  }

  // === 表格通用样式 ===
  :deep(.el-table) {
    border-radius: 8px;
    overflow: hidden;
    --el-table-border-color: #ebeef5;
    --el-table-row-hover-bg-color: #e8f2fd;
    --el-table-current-row-bg-color: #d4e8fa;
    background: transparent;
    font-size: 14px;
  }

  :deep(.el-table__header th) {
    background: #f0f3f8 !important;
    color: #3a4a5e;
    font-weight: 600;
    font-size: 14px;
    border-bottom: 2px solid #dce0e8 !important;
    padding: 10px 0;
  }

  :deep(.el-table__body td) {
    padding: 8px 0;
    font-size: 14px;
  }

  :deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
    background: #f8f9fb;
  }

  :deep(.el-table__body tr.current-row > td.el-table__cell) {
    background-color: #d4e8fa !important;
  }
  :deep(.el-table__body tr.hover-row > td.el-table__cell) {
    background-color: #e8f2fd !important;
  }

  // === 购物车卡片 ===
  .cart-card {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;

    :deep(.el-card__header) {
      flex-shrink: 0;
    }

    :deep(.el-card__body) {
      flex: 1;
      height: 0;
      padding: 0 !important;
      overflow: hidden;
    }

    .cart-scroll-body {
      height: 100%;
      overflow-y: auto;
      padding: 20px;
    }

    .empty-cart {
      padding: 30px 0;
      text-align: center;
    }

    .amount-text {
      color: #e85d04;
      font-weight: 700;
      font-size: 14px;
    }

    .member-price-text {
      color: #e6500a;
      font-weight: 700;
      font-size: 14px;
    }

    .original-price-text {
      text-decoration: line-through;
      color: #b0b4bf;
      font-size: 12px;
      margin-left: 4px;
    }
  }

  // === 中药处方头部 ===
  .herb-prescription-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 16px;
    margin-top: 12px;
    background: linear-gradient(135deg, #e8f5e9, #dcedc8);
    border: 1.5px solid #c8e6c9;
    border-bottom: none;
    border-radius: 10px 10px 0 0;
  }

  .herb-prescription-title {
    font-size: 15px;
    font-weight: 700;
    color: #2e7d32;
  }

  .herb-summary-info {
    display: flex;
    align-items: center;
    gap: 16px;
    flex: 1;
    justify-content: center;

    .herb-summary-text {
      font-size: 14px;
      color: #2d3748;
      font-weight: 600;
    }

    .herb-summary-amount {
      font-size: 15px;
      color: #e53e3e;
      font-weight: 700;
    }
  }

  .herb-dose-control {
    display: flex;
    align-items: center;
    gap: 8px;

    .herb-dose-label {
      font-size: 14px;
      color: #4a5568;
      font-weight: 600;
    }

    .herb-dose-unit {
      font-size: 14px;
      color: #4a5568;
    }

    :deep(.el-input-number) {
      width: 90px;
    }
  }

  // === 搜索卡片 ===
  .search-card {
    flex-shrink: 0;

    .search-row {
      display: flex;
      align-items: center;

      .search-input {
        flex: 1;

        :deep(.el-input__wrapper) {
          box-shadow: 0 0 0 1.5px #dce0e8 inset;
          background: #fafbfc;
          border-radius: 10px;
          padding: 4px 12px;
          font-size: 15px;

          &:focus-within {
            box-shadow: 0 0 0 2px #409eff inset, 0 0 0 4px rgba(64, 158, 255, 0.1);
            background: #fff;
          }
        }

        :deep(.el-input__inner) {
          font-size: 15px;
          height: 36px;
        }
      }
    }

    .drug-list {
      margin-top: 12px;

      .no-result {
        text-align: center;
        color: #8c919f;
        padding: 24px;
        font-size: 14px;
      }
    }
  }

  // === 右侧结算面板 ===
  .payment-card {
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    :deep(.el-card__body) {
      flex: 1;
      display: flex;
      flex-direction: column;
      padding: 20px;
      overflow-y: auto;
      min-height: 0;
    }
  }

  .section-title {
    font-size: 15px;
    font-weight: 700;
    color: #1a2332;
    margin: 6px 0 12px 0;
    padding: 4px 0 4px 12px;
    border-left: 4px solid #409eff;
    letter-spacing: 0.5px;
  }

  // === 会员信息区 ===
  .member-section {
    margin-bottom: 18px;
    position: relative;

    :deep(.el-input__wrapper) {
      font-size: 14px;
    }

    :deep(.el-input__inner) {
      font-size: 14px;
    }

    .member-search-list {
      position: absolute;
      top: 36px;
      left: 0;
      right: 0;
      z-index: 100;
      background: #fff;
      border: 1px solid #dce0e8;
      border-radius: 10px;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      max-height: 260px;
      overflow-y: auto;

      .member-search-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 16px;
        cursor: pointer;
        border-bottom: 1px solid #f2f4f7;
        font-size: 14px;
        transition: background 0.15s;

        &:last-child { border-bottom: none; }
        &:hover, &.active { background: #e6f0fa; }

        .name { font-weight: 600; color: #1a2332; min-width: 60px; }
        .phone { color: #8c919f; }
        .points-tag { margin-left: auto; color: #e85d04; font-size: 13px; font-weight: 500; }
      }
    }

    .member-info {
      margin-top: 14px;
      padding: 14px 16px;
      background: linear-gradient(135deg, #e8f4fd 0%, #dbeefa 100%);
      border: 1px solid #c4ddf3;
      border-radius: 10px;
      cursor: context-menu;

      .member-row {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        &:last-child { margin-bottom: 0; }

        .label {
          color: #5a6577;
          font-size: 14px;
        }

        .value {
          font-weight: 600;
          font-size: 14px;

          &.points {
            color: #e85d04;
            font-size: 16px;
          }
        }
      }
    }

    .member-context-menu {
      position: fixed;
      z-index: 9999;
      background: #fff;
      border: 1px solid #dcdfe6;
      border-radius: 8px;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
      padding: 6px 0;
      min-width: 130px;

      .context-menu-item {
        padding: 10px 18px;
        font-size: 14px;
        color: #303133;
        cursor: pointer;
        white-space: nowrap;

        &:hover {
          background: #ecf5ff;
          color: #409eff;
        }
      }
    }
  }

  // === 结算明细区 ===
  .settlement-section {
    margin-bottom: 18px;
    padding: 16px;
    background: linear-gradient(135deg, #f4f6fa 0%, #edf1f7 100%);
    border: 1px solid #e0e5ed;
    border-radius: 12px;

    .summary-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 9px 0;
      font-size: 15px;

      .label {
        color: #4a5568;
        font-size: 15px;
      }

      .value {
        font-weight: 600;
        font-size: 15px;

        &.amount {
          color: #1a2332;
          font-size: 18px;
          font-weight: 700;
        }
      }

      &.discount {
        .value {
          color: #52c41a;
        }
      }

      &.total {
        border-top: 2px dashed #cdd3dc;
        margin-top: 10px;
        padding-top: 14px;

        .label {
          font-size: 17px;
          font-weight: 700;
          color: #1a2332;
        }

        .pay-amount {
          font-size: 32px;
          color: #e85d04;
          font-weight: 800;
          letter-spacing: -0.5px;
        }
      }
    }
  }

  // === 支付方式 ===
  .payment-methods {
    margin-bottom: 18px;

    :deep(.el-radio-group) {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;

      .el-radio-button {
        flex: 1;
        min-width: calc(50% - 5px);

        .el-radio-button__inner {
          width: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          padding: 14px 10px;
          border-radius: 10px !important;
          border: 1.5px solid #dce0e8 !important;
          background: #f7f8fa;
          font-size: 15px;
          font-weight: 500;
          transition: all 0.25s ease;
        }

        &.is-active .el-radio-button__inner {
          background: linear-gradient(135deg, #409eff, #2d7cd6);
          color: #fff;
          border-color: #409eff !important;
          box-shadow: 0 4px 12px rgba(64, 158, 255, 0.25);
        }
      }
    }
  }

  // === 现金找零区 ===
  .cash-change-section {
    background: linear-gradient(135deg, #f4f8f2 0%, #eaf1e6 100%);
    border: 1px solid #d4e2cf;
    padding: 14px 16px;
    border-radius: 12px;
    margin-bottom: 18px;

    .summary-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;

      &:last-child { margin-bottom: 0; }

      .label {
        color: #4a5568;
        font-size: 15px;
      }

      .change-amount {
        color: #52c41a;
        font-weight: 700;
        font-size: 18px;
      }

      .change-error {
        color: #f5222d;
        font-weight: 700;
        font-size: 18px;
      }
    }
  }

  // === 操作按钮 ===
  .action-buttons {
    display: flex;
    gap: 12px;
    margin-bottom: 18px;

    .checkout-btn {
      flex: 1;
      height: 52px;
      font-size: 17px;
      font-weight: 600;
      background: linear-gradient(135deg, #409eff 0%, #2d7cd6 100%);
      border: none;
      border-radius: 12px;
      box-shadow: 0 4px 14px rgba(64, 158, 255, 0.3);
      transition: all 0.25s ease;
      letter-spacing: 1px;

      &:hover:not(:disabled) {
        box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
        transform: translateY(-1px);
      }
    }

    .el-button:not(.checkout-btn) {
      min-width: 76px;
      height: 52px;
      border-radius: 12px;
      border: 1.5px solid #dce0e8;
      font-size: 15px;
      font-weight: 500;
    }
  }

  // === 快捷键提示 ===
  .shortcut-tips {
    display: flex;
    justify-content: center;
    gap: 12px;
    font-size: 13px;
    color: #8c919f;
    margin-top: auto;
    padding: 10px 14px;
    background: #f4f6fa;
    border-radius: 8px;

    span {
      background: #e4e8ee;
      padding: 3px 10px;
      border-radius: 6px;
      font-size: 12px;
      letter-spacing: 0.5px;
      font-weight: 500;
    }
  }

  .suspend-badge {
    margin-left: 4px;
    :deep(.el-badge__content) {
      top: -4px;
    }
  }
}

.config-tip {
  font-size: 13px;
  color: #8c919f;
}

:deep(.el-table__row) {
  cursor: pointer;
}

:deep(.el-input-number--small) {
  width: 100px;
}

.column-settings {
  .column-settings-title {
    font-weight: 700;
    margin-bottom: 10px;
    font-size: 14px;
    color: #1a2332;
  }
  .column-settings-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 4px 0;
    font-size: 14px;
    color: #4a5568;
    .column-settings-btns {
      display: flex;
      gap: 0;
    }
  }
}
</style>

<style lang="scss">
.pos-context-menu {
  position: fixed;
  z-index: 9999;
  background: #fff;
  border: 1px solid #e0e5ed;
  border-radius: 10px;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.15);
  padding: 6px 0;
  min-width: 140px;

  .context-menu-item {
    padding: 10px 20px;
    font-size: 14px;
    color: #303133;
    cursor: pointer;
    white-space: nowrap;
    transition: all 0.15s;

    &:hover {
      background: #ecf5ff;
      color: #409eff;
    }

    &.context-menu-info {
      color: #e85d04;
      font-weight: 700;
      cursor: default;
      border-bottom: 1px solid #f0f2f5;
      font-size: 15px;

      &:hover {
        background: transparent;
        color: #e85d04;
      }
    }
  }
}
</style>
