import { createRouter, createWebHistory } from 'vue-router'

const Layout = () => import('@/views/Layout.vue')

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      // 药品管理
      {
        path: 'drug',
        name: 'DrugList',
        component: () => import('@/views/drug/DrugList.vue'),
        meta: { title: '药品管理', icon: 'FirstAidKit' }
      },
      {
        path: 'drug/category',
        name: 'DrugCategory',
        component: () => import('@/views/drug/CategoryManage.vue'),
        meta: { title: '药品分类' }
      },
      {
        path: 'drug/supplier',
        name: 'SupplierList',
        component: () => import('@/views/drug/SupplierList.vue'),
        meta: { title: '供应商管理' }
      },
      // 中药管理
      {
        path: 'herb',
        name: 'HerbList',
        component: () => import('@/views/herb/HerbList.vue'),
        meta: { title: '中药饮片', icon: 'Cherry' }
      },
      {
        path: 'herb/cabinet',
        name: 'HerbCabinet',
        component: () => import('@/views/herb/CabinetManage.vue'),
        meta: { title: '斗谱管理' }
      },
      {
        path: 'herb/gsp',
        name: 'HerbGsp',
        component: () => import('@/views/herb/GspRecords.vue'),
        meta: { title: '中药GSP' }
      },
      {
        path: 'herb/prescription',
        name: 'HerbPrescription',
        component: () => import('@/views/herb/PrescriptionList.vue'),
        meta: { title: '中药处方' }
      },
      {
        path: 'herb/weighing',
        name: 'SmartWeighing',
        component: () => import('@/views/herb/SmartWeighing.vue'),
        meta: { title: '智能称重' }
      },
      // 库存管理
      {
        path: 'inventory',
        name: 'InventoryList',
        component: () => import('@/views/inventory/InventoryList.vue'),
        meta: { title: '库存查询', icon: 'Box' }
      },
      {
        path: 'inventory/stock-in',
        name: 'StockInList',
        component: () => import('@/views/inventory/StockInList.vue'),
        meta: { title: '入库管理' }
      },
      {
        path: 'inventory/stock-out',
        name: 'StockOutList',
        component: () => import('@/views/inventory/StockOutList.vue'),
        meta: { title: '出库管理' }
      },
      {
        path: 'inventory/stock-check',
        name: 'StockCheckList',
        component: () => import('@/views/inventory/StockCheckList.vue'),
        meta: { title: '盘点管理' }
      },
      {
        path: 'inventory/warnings',
        name: 'InventoryWarnings',
        component: () => import('@/views/inventory/Warnings.vue'),
        meta: { title: '库存预警' }
      },
      {
        path: 'inventory/expiry',
        name: 'ExpiryWarning',
        component: () => import('@/views/inventory/ExpiryWarning.vue'),
        meta: { title: '效期预警' }
      },
      // 销售管理
      {
        path: 'sale/pos',
        name: 'POS',
        component: () => import('@/views/sale/POS.vue'),
        meta: { title: '收银台', icon: 'ShoppingCart' }
      },
      {
        path: 'sale/orders',
        name: 'SaleOrders',
        component: () => import('@/views/sale/OrderList.vue'),
        meta: { title: '销售记录' }
      },
      {
        path: 'sale/refund',
        name: 'RefundList',
        component: () => import('@/views/sale/RefundList.vue'),
        meta: { title: '退货管理' }
      },
      // 会员管理
      {
        path: 'member',
        name: 'MemberList',
        component: () => import('@/views/member/MemberList.vue'),
        meta: { title: '会员管理', icon: 'User' }
      },
      {
        path: 'member/level',
        name: 'MemberLevel',
        component: () => import('@/views/member/MemberLevelManage.vue'),
        meta: { title: '等级管理' }
      },
      {
        path: 'member/health',
        name: 'MemberHealth',
        component: () => import('@/views/member/HealthProfile.vue'),
        meta: { title: '健康画像' }
      },
      {
        path: 'member/chronic-disease',
        name: 'ChronicDisease',
        component: () => import('@/views/member/ChronicDisease.vue'),
        meta: { title: '慢病管理' }
      },
      // GSP合规
      {
        path: 'gsp/acceptance',
        name: 'GspAcceptance',
        component: () => import('@/views/gsp/AcceptanceList.vue'),
        meta: { title: '验收记录', icon: 'Document' }
      },
      {
        path: 'gsp/maintenance',
        name: 'GspMaintenance',
        component: () => import('@/views/gsp/MaintenanceList.vue'),
        meta: { title: '养护记录' }
      },
      {
        path: 'gsp/temperature',
        name: 'GspTemperature',
        component: () => import('@/views/gsp/TemperatureLog.vue'),
        meta: { title: '温湿度监控' }
      },
      {
        path: 'gsp/compliance',
        name: 'GspCompliance',
        component: () => import('@/views/gsp/ComplianceCheck.vue'),
        meta: { title: '合规检查' }
      },
      // 系统管理
      {
        path: 'system/config',
        name: 'SystemConfig',
        component: () => import('@/views/system/ConfigManage.vue'),
        meta: { title: '系统配置', icon: 'Setting' }
      },
      {
        path: 'system/store',
        name: 'StoreManage',
        component: () => import('@/views/system/StoreManage.vue'),
        meta: { title: '门店管理' }
      },
      {
        path: 'system/migration',
        name: 'DataMigration',
        component: () => import('@/views/system/DataMigration.vue'),
        meta: { title: '数据迁移' }
      },
      {
        path: 'system/device',
        name: 'DeviceManage',
        component: () => import('@/views/system/DeviceManage.vue'),
        meta: { title: '设备管理' }
      },
      // AI智能
      {
        path: 'ai/assistant',
        name: 'AiAssistant',
        component: () => import('@/views/ai/AiAssistant.vue'),
        meta: { title: 'AI助手', icon: 'MagicStick' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || 'YF药房'} - YF药房管理系统`
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
