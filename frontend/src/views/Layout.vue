<template>
  <el-container class="layout">
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="layout-sidebar" :class="{ collapsed: isCollapsed }">
      <div class="logo">
        <span v-if="!isCollapsed">{{ isSuperAdmin ? '云界管理平台' : '云界智慧药房' }}</span>
        <span v-else>云界</span>
      </div>
      <el-menu :default-active="activeMenu" :collapse="isCollapsed" :collapse-transition="false"
        background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff" router>
        
        <!-- 超级管理员菜单 (tenant_id=0) -->
        <template v-if="isSuperAdmin">
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataBoard /></el-icon>
            <template #title>控制台</template>
          </el-menu-item>

          <el-sub-menu index="tenant-menu">
            <template #title>
              <el-icon><OfficeBuilding /></el-icon>
              <span>租户管理</span>
            </template>
            <el-menu-item index="/admin/tenant">租户列表</el-menu-item>
            <el-menu-item index="/admin/tenant/create">开通租户</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="platform-menu">
            <template #title>
              <el-icon><Monitor /></el-icon>
              <span>平台运营</span>
            </template>
            <el-menu-item index="/admin/audit-log">操作日志</el-menu-item>
            <el-menu-item index="/admin/statistics">统计分析</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="knowledge-menu">
            <template #title>
              <el-icon><Reading /></el-icon>
              <span>知识库</span>
            </template>
            <el-menu-item index="/admin/drug-knowledge">药品知识</el-menu-item>
            <el-menu-item index="/admin/herb-knowledge">中药知识</el-menu-item>
            <el-menu-item index="/admin/incompatibility">配伍禁忌</el-menu-item>
            <el-menu-item index="/admin/manufacturer">生产企业</el-menu-item>
          </el-sub-menu>
        </template>

        <!-- 租户用户菜单 (tenant_id>0) -->
        <template v-else>
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页</template>
          </el-menu-item>

          <el-sub-menu index="drug-menu">
            <template #title>
              <el-icon><FirstAidKit /></el-icon>
              <span>药品管理</span>
            </template>
            <el-menu-item index="/drug">药品列表</el-menu-item>
            <el-menu-item index="/drug/category">药品分类</el-menu-item>
            <el-menu-item index="/drug/supplier">供应商管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="herb-menu">
            <template #title>
              <el-icon><Cherry /></el-icon>
              <span>中药管理</span>
            </template>
            <el-menu-item index="/herb/cabinet">斗谱管理</el-menu-item>
            <el-menu-item index="/herb/gsp">中药GSP</el-menu-item>
            <el-menu-item index="/herb/prescription">中药处方</el-menu-item>
            <el-menu-item index="/herb/incompatibility">配伍禁忌</el-menu-item>
            <el-menu-item index="/herb/scale-device">电子秤设备</el-menu-item>
            <el-menu-item index="/herb/fill-log">装斗记录</el-menu-item>
            <el-menu-item index="/herb/clean-log">清斗记录</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="inventory-menu">
            <template #title>
              <el-icon><Box /></el-icon>
              <span>库存管理</span>
            </template>
            <el-menu-item index="/inventory">库存查询</el-menu-item>
            <el-menu-item index="/inventory/stock-in">入库管理</el-menu-item>
            <el-menu-item index="/inventory/stock-out">出库管理</el-menu-item>
            <el-menu-item index="/inventory/stock-check">盘点管理</el-menu-item>
            <el-menu-item index="/inventory/batch">批次管理</el-menu-item>
            <el-menu-item index="/inventory/trace-code">追溯码管理</el-menu-item>
            <el-menu-item index="/inventory/warnings">库存预警</el-menu-item>
            <el-menu-item index="/inventory/expiry">近效期预警</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="sale-menu">
            <template #title>
              <el-icon><ShoppingCart /></el-icon>
              <span>销售管理</span>
            </template>
            <el-menu-item index="/sale/pos">收银台</el-menu-item>
            <el-menu-item index="/sale/orders">销售记录</el-menu-item>
            <el-menu-item index="/sale/refund">退货管理</el-menu-item>
            <el-menu-item index="/sale/out-of-stock">缺货登记</el-menu-item>
            <el-menu-item index="/sale/promotion">促销管理</el-menu-item>
            <el-menu-item index="/sale/combination">组合推荐</el-menu-item>
            <el-menu-item index="/sale/reconciliation">日终对账</el-menu-item>
            <el-menu-item index="/sale/profit">利润查询</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="member-menu">
            <template #title>
              <el-icon><User /></el-icon>
              <span>会员管理</span>
            </template>
            <el-menu-item index="/member">会员列表</el-menu-item>
            <el-menu-item index="/member/level">等级管理</el-menu-item>
            <el-menu-item index="/member/health">健康画像</el-menu-item>
            <el-menu-item index="/member/chronic-disease">慢病管理</el-menu-item>
            <el-menu-item index="/member/reminder">用药提醒</el-menu-item>
            <el-menu-item index="/member/purchase-history">购药记录</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="first-marketing-menu">
            <template #title>
              <el-icon><Stamp /></el-icon>
              <span>首营管理</span>
            </template>
            <el-menu-item index="/first-marketing/supplier">首营企业</el-menu-item>
            <el-menu-item index="/first-marketing/drug">首营品种</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="business-history-menu">
            <template #title>
              <el-icon><Notebook /></el-icon>
              <span>经营历程</span>
            </template>
            <el-menu-item index="/business-history/purchase">进货查询</el-menu-item>
            <el-menu-item index="/business-history/sales">销售查询</el-menu-item>
            <el-menu-item index="/business-history/inventory">存货查询</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="gsp-menu">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>GSP合规</span>
            </template>
            <el-menu-item index="/gsp/acceptance">验收记录</el-menu-item>
            <el-menu-item index="/gsp/maintenance">养护记录</el-menu-item>
            <el-menu-item index="/gsp/temperature">温湿度监控</el-menu-item>
            <el-menu-item index="/gsp/defective">不良品管理</el-menu-item>
            <el-menu-item index="/gsp/near-expiry-sale">近效期催销</el-menu-item>
            <el-menu-item index="/gsp/equipment">养护设备</el-menu-item>
            <el-menu-item index="/gsp/destruction">药品销毁</el-menu-item>
            <el-menu-item index="/gsp/training">员工培训</el-menu-item>
            <el-menu-item index="/gsp/report">GSP报表</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="system-menu">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统设置</span>
            </template>
            <el-menu-item index="/system/config">系统配置</el-menu-item>
            <el-menu-item index="/system/store">门店管理</el-menu-item>
            <el-menu-item index="/system/staff">员工管理</el-menu-item>
            <el-menu-item index="/system/role">角色权限</el-menu-item>
            <el-menu-item index="/system/quick-setup">快速部署</el-menu-item>
            <el-menu-item index="/system/migration">数据迁移</el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>

    <el-container class="layout-main">
      <el-header class="layout-header">
        <div style="display: flex; align-items: center;">
          <el-icon style="cursor:pointer; font-size:20px;" @click="isCollapsed = !isCollapsed">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/" style="margin-left: 16px;">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div style="display: flex; align-items: center; gap: 16px;">
          <span v-if="user?.tenantName" style="color: #606266; font-size: 13px; border-right: 1px solid #dcdfe6; padding-right: 16px;">{{ user.tenantName }}</span>
          <span>{{ user?.realName || user?.username }}</span>
          <el-dropdown @command="handleCommand">
            <el-avatar :size="32" style="cursor:pointer;">
              {{ (user?.realName || 'U').charAt(0) }}
            </el-avatar>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isCollapsed = ref(false)
const user = computed(() => authStore.user)
const activeMenu = computed(() => route.path)
// 超级管理员判断 (tenant_id=0)
const isSuperAdmin = computed(() => user.value?.tenantId === 0)

function handleCommand(cmd) {
  if (cmd === 'logout') {
    authStore.logout()
    router.push('/login')
  }
}
</script>
