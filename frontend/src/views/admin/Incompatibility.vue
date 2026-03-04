<template>
  <div class="incompatibility-manage">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>配伍禁忌管理</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon> 新增
          </el-button>
        </div>
      </template>

      <el-alert type="info" :closable="false" style="margin-bottom: 16px;">
        配伍禁忌数据为全局公共数据，修改后将影响所有租户的处方校验。
      </el-alert>

      <el-form :inline="true" :model="query">
        <el-form-item label="药材名称">
          <el-input v-model="query.herbName" placeholder="请输入药材名称" clearable />
        </el-form-item>
        <el-form-item label="禁忌类型">
          <el-select v-model="query.type" placeholder="全部" clearable style="width: 120px;">
            <el-option label="十八反" value="eighteen" />
            <el-option label="十九畏" value="nineteen" />
            <el-option label="妊娠禁忌" value="pregnancy" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="herb1Name" label="药材1" width="120" />
        <el-table-column prop="herb2Name" label="药材2" width="120" />
        <el-table-column prop="type" label="禁忌类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)" size="small">{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="严重程度" width="100">
          <template #default="{ row }">
            <el-tag :type="row.level === 'high' ? 'danger' : (row.level === 'medium' ? 'warning' : 'info')" size="small">
              {{ row.level === 'high' ? '严重' : (row.level === 'medium' ? '中等' : '轻微') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="editItem(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="deleteItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end;"
        @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = ref({ pageNum: 1, pageSize: 10, herbName: '', type: '' })

function getTypeLabel(type) {
  const map = { eighteen: '十八反', nineteen: '十九畏', pregnancy: '妊娠禁忌', other: '其他' }
  return map[type] || type
}

function getTypeTag(type) {
  const map = { eighteen: 'danger', nineteen: 'warning', pregnancy: 'info', other: '' }
  return map[type] || ''
}

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/herb-incompatibility/page', { params: query.value })
    if (res.code === 200) {
      list.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } finally {
    loading.value = false
  }
}

function showAddDialog() { ElMessage.info('新增配伍禁忌功能开发中') }
function editItem(row) { ElMessage.info('编辑功能开发中') }
async function deleteItem(row) {
  try {
    await ElMessageBox.confirm(`确定删除此配伍禁忌吗?`, '提示', { type: 'warning' })
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {}
}

onMounted(loadData)
</script>
