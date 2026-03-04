<template>
  <div class="knowledge-manage">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>药品知识库</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon> 新增
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query">
        <el-form-item label="商品名称">
          <el-input v-model="query.name" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.category" placeholder="全部" clearable style="width: 120px;">
            <el-option label="处方药" value="prescription" />
            <el-option label="OTC" value="otc" />
            <el-option label="中成药" value="chinese_patent" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="drugs" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="商品名称" min-width="150" />
        <el-table-column prop="commonName" label="通用名" width="150" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="manufacturer" label="生产厂家" width="150" />
        <el-table-column prop="indications" label="适应症" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="editDrug(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="deleteDrug(row)">删除</el-button>
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
const drugs = ref([])
const total = ref(0)
const query = ref({ pageNum: 1, pageSize: 10, name: '', category: '' })

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/drug-knowledge/page', { params: query.value })
    if (res.code === 200) {
      drugs.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } finally {
    loading.value = false
  }
}

function showAddDialog() {
  ElMessage.info('新增药品知识功能开发中')
}

function editDrug(row) {
  ElMessage.info('编辑功能开发中')
}

async function deleteDrug(row) {
  try {
    await ElMessageBox.confirm(`确定删除"${row.name}"吗?`, '提示', { type: 'warning' })
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {}
}

onMounted(loadData)
</script>
