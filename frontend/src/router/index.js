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
      // 超级管理员路由 (tenant_id=0)
      {
        path: 'admin/dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '控制台', requireSuperAdmin: true }
      },
      {
        path: 'admin/tenant',
        name: 'AdminTenantList',
        component: () => import('@/views/admin/TenantList.vue'),
        meta: { title: '租户列表', requireSuperAdmin: true }
      },
      {
        path: 'admin/tenant/create',
        name: 'AdminTenantCreate',
        component: () => import('@/views/admin/TenantCreate.vue'),
        meta: { title: '开通租户', requireSuperAdmin: true }
      },
      {
        path: 'admin/audit-log',
        name: 'AdminAuditLog',
        component: () => import('@/views/admin/AuditLog.vue'),
        meta: { title: '操作日志', requireSuperAdmin: true }
      },
      {
        path: 'admin/statistics',
        name: 'AdminStatistics',
        component: () => import('@/views/admin/Statistics.vue'),
        meta: { title: '统计分析', requireSuperAdmin: true }
      },
      {
        path: 'admin/drug-knowledge',
        name: 'AdminDrugKnowledge',
        component: () => import('@/views/admin/DrugKnowledge.vue'),
        meta: { title: '药品知识', requireSuperAdmin: true }
      },
      {
        path: 'admin/herb-knowledge',
        name: 'AdminHerbKnowledge',
        component: () => import('@/views/admin/HerbKnowledge.vue'),
        meta: { title: '中药知识', requireSuperAdmin: true }
      },
      {
        path: 'admin/incompatibility',
        name: 'AdminIncompatibility',
        component: () => import('@/views/admin/Incompatibility.vue'),
        meta: { title: '配伍禁忌', requireSuperAdmin: true }
      },
      {
        path: 'admin/manufacturer',
        name: 'AdminManufacturer',
        component: () => import('@/views/admin/ManufacturerManage.vue'),
        meta: { title: '生产企业', requireSuperAdmin: true }
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
      {
        path: 'herb/incompatibility',
        name: 'HerbIncompatibility',
        component: () => import('@/views/herb/Incompatibility.vue'),
        meta: { title: '配伍禁忌' }
      },
      {
        path: 'herb/scale-device',
        name: 'ScaleDevice',
        component: () => import('@/views/herb/ScaleDevice.vue'),
        meta: { title: '电子秤设备' }
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
      {
        path: 'inventory/batch',
        name: 'BatchManage',
        component: () => import('@/views/inventory/BatchManage.vue'),
        meta: { title: '批次管理' }
      },
      {
        path: 'inventory/trace-code',
        name: 'TraceCode',
        component: () => import('@/views/inventory/TraceCode.vue'),
        meta: { title: '追溯码管理' }
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
      {
        path: 'sale/out-of-stock',
        name: 'OutOfStock',
        component: () => import('@/views/sale/OutOfStock.vue'),
        meta: { title: '缺货登记' }
      },
      {
        path: 'sale/promotion',
        name: 'Promotion',
        component: () => import('@/views/sale/Promotion.vue'),
        meta: { title: '促销管理' }
      },
      {
        path: 'sale/combination',
        name: 'DrugCombination',
        component: () => import('@/views/sale/DrugCombination.vue'),
        meta: { title: '组合推荐' }
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
      {
        path: 'member/reminder',
        name: 'MedicationReminder',
        component: () => import('@/views/member/MedicationReminder.vue'),
        meta: { title: '用药提醒' }
      },
      // 首营管理
      {
        path: 'first-marketing/supplier',
        name: 'FirstMarketingSupplier',
        component: () => import('@/views/first-marketing/FirstMarketingSupplierList.vue'),
        meta: { title: '首营企业' }
      },
      {
        path: 'first-marketing/drug',
        name: 'FirstMarketingDrug',
        component: () => import('@/views/first-marketing/FirstMarketingDrugList.vue'),
        meta: { title: '首营品种' }
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
      {
        path: 'gsp/defective',
        name: 'DefectiveDrug',
        component: () => import('@/views/gsp/DefectiveDrug.vue'),
        meta: { title: '不良品管理' }
      },
      {
        path: 'gsp/destruction',
        name: 'DrugDestruction',
        component: () => import('@/views/gsp/DrugDestruction.vue'),
        meta: { title: '药品销毁' }
      },
      {
        path: 'gsp/training',
        name: 'StaffTraining',
        component: () => import('@/views/gsp/StaffTraining.vue'),
        meta: { title: '员工培训' }
      },
      {
        path: 'gsp/report',
        name: 'GspReport',
        component: () => import('@/views/gsp/GspReport.vue'),
        meta: { title: 'GSP报表' }
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
        path: 'system/staff',
        name: 'StaffManage',
        component: () => import('@/views/system/StaffManage.vue'),
        meta: { title: '员工管理' }
      },
      {
        path: 'system/role',
        name: 'RoleManage',
        component: () => import('@/views/system/RoleManage.vue'),
        meta: { title: '角色权限' }
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
      {
        path: 'system/tenant',
        name: 'TenantManage',
        component: () => import('@/views/system/TenantManage.vue'),
        meta: { title: '租户管理' }
      },
      {
        path: 'system/quick-setup',
        name: 'QuickSetup',
        component: () => import('@/views/system/QuickSetup.vue'),
        meta: { title: '快速部署' }
      },
      {
        path: 'system/audit-log',
        name: 'AuditLog',
        component: () => import('@/views/system/AuditLog.vue'),
        meta: { title: '审计日志' }
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
  const userStr = localStorage.getItem('user')
  
  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }
  
  if (token && userStr) {
    const user = JSON.parse(userStr)
    const isSuperAdmin = user.tenantId === 0
    
    // 超级管理员登录后重定向到管理控制台
    if (to.path === '/dashboard' && isSuperAdmin) {
      next('/admin/dashboard')
      return
    }
    
    // 普通租户不能访问超级管理员页面
    if (to.meta.requireSuperAdmin && !isSuperAdmin) {
      next('/dashboard')
      return
    }
    
    // 超级管理员访问业务页面时重定向到管理控制台
    if (isSuperAdmin && !to.path.startsWith('/admin') && to.path !== '/login') {
      next('/admin/dashboard')
      return
    }
  }
  
  next()
})

export default router
