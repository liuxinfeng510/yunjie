<template>
  <el-container class="layout">
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="layout-sidebar" :class="{ collapsed: isCollapsed }">
      <div class="logo">
        <span v-if="!isCollapsed">YF 药房管理</span>
        <span v-else>YF</span>
      </div>
      <el-menu :default-active="activeMenu" :collapse="isCollapsed" :collapse-transition="false"
        background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff" router>
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
          <el-menu-item index="/herb">中药饮片</el-menu-item>
          <el-menu-item index="/herb/cabinet">斗谱管理</el-menu-item>
          <el-menu-item index="/herb/gsp">中药GSP</el-menu-item>
          <el-menu-item index="/herb/prescription">中药处方</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="inventory-menu">
          <template #title>
            <el-icon><Box /></el-icon>
            <span>库存管理</span>
          </template>
          <el-menu-item index="/inventory">库存查询</el-menu-item>
          <el-menu-item index="/inventory/stock-in">入库管理</el-menu-item>
          <el-menu-item index="/inventory/warnings">库存预警</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="sale-menu">
          <template #title>
            <el-icon><ShoppingCart /></el-icon>
            <span>销售管理</span>
          </template>
          <el-menu-item index="/sale/pos">收银台</el-menu-item>
          <el-menu-item index="/sale/orders">销售记录</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/member">
          <el-icon><User /></el-icon>
          <template #title>会员管理</template>
        </el-menu-item>

        <el-sub-menu index="gsp-menu">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>GSP合规</span>
          </template>
          <el-menu-item index="/gsp/acceptance">验收记录</el-menu-item>
          <el-menu-item index="/gsp/maintenance">养护记录</el-menu-item>
          <el-menu-item index="/gsp/temperature">温湿度监控</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="system-menu">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/config">系统配置</el-menu-item>
          <el-menu-item index="/system/store">门店管理</el-menu-item>
          <el-menu-item index="/system/migration">数据迁移</el-menu-item>
        </el-sub-menu>
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

function handleCommand(cmd) {
  if (cmd === 'logout') {
    authStore.logout()
    router.push('/login')
  }
}
</script>
