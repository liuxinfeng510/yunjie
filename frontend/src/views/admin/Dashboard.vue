<template>
  <div class="admin-dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff;">
              <el-icon :size="28"><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.tenantCount }}</div>
              <div class="stat-label">租户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a;">
              <el-icon :size="28"><Check /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.activeTenants }}</div>
              <div class="stat-label">活跃租户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c;">
              <el-icon :size="28"><Shop /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.storeCount }}</div>
              <div class="stat-label">门店总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c;">
              <el-icon :size="28"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.userCount }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>最近开通租户</span>
          </template>
          <el-table :data="recentTenants" stripe>
            <el-table-column prop="name" label="租户名称" />
            <el-table-column prop="contactName" label="联系人" width="100" />
            <el-table-column prop="businessMode" label="模式" width="100">
              <template #default="{ row }">
                <el-tag :type="row.businessMode === 'chain' ? 'primary' : 'success'" size="small">
                  {{ row.businessMode === 'chain' ? '连锁' : '单店' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="开通时间" width="160" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>系统公告</span>
          </template>
          <el-timeline>
            <el-timeline-item timestamp="2026-03-01" placement="top">
              <el-card>
                <h4>系统升级通知</h4>
                <p>云界智慧药房系统已升级至V2.0版本，新增中药配伍禁忌校验功能。</p>
              </el-card>
            </el-timeline-item>
            <el-timeline-item timestamp="2026-02-15" placement="top">
              <el-card>
                <h4>GSP合规模块上线</h4>
                <p>新增GSP合规管理模块，支持药品验收、养护、温湿度监控等功能。</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const stats = ref({
  tenantCount: 0,
  activeTenants: 0,
  storeCount: 0,
  userCount: 0
})

const recentTenants = ref([])

onMounted(async () => {
  try {
    const res = await request.get('/tenant/statistics')
    if (res.code === 200) {
      stats.value = res.data
    }
    const listRes = await request.get('/tenant/page', { params: { pageNum: 1, pageSize: 5 } })
    if (listRes.code === 200) {
      recentTenants.value = listRes.data.records || []
    }
  } catch (e) {
    console.error('Failed to load dashboard data', e)
  }
})
</script>

<style scoped>
.admin-dashboard {
  padding: 10px;
}
.stat-card {
  height: 120px;
}
.stat-content {
  display: flex;
  align-items: center;
  height: 80px;
}
.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 16px;
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}
</style>
