<template>
  <div class="knowledge-manage">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>中药知识库</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon> 新增
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query">
        <el-form-item label="中药名称">
          <el-input v-model="query.name" placeholder="请输入中药名称" clearable />
        </el-form-item>
        <el-form-item label="药性">
          <el-select v-model="query.nature" placeholder="全部" clearable style="width: 100px;">
            <el-option label="寒" value="寒" />
            <el-option label="热" value="热" />
            <el-option label="温" value="温" />
            <el-option label="凉" value="凉" />
            <el-option label="平" value="平" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="herbs" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="中药名称" width="120" />
        <el-table-column prop="pinyin" label="拼音" width="100" />
        <el-table-column prop="nature" label="药性" width="60" />
        <el-table-column prop="flavor" label="药味" width="100" />
        <el-table-column prop="meridian" label="归经" width="150" />
        <el-table-column prop="efficacy" label="功效" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="editHerb(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="deleteHerb(row)">删除</el-button>
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
const herbs = ref([])
const total = ref(0)
const query = ref({ pageNum: 1, pageSize: 10, name: '', nature: '' })

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/herb-knowledge/page', { params: query.value })
    if (res.code === 200) {
      herbs.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } finally {
    loading.value = false
  }
}

function showAddDialog() { ElMessage.info('新增中药知识功能开发中') }
function editHerb(row) { ElMessage.info('编辑功能开发中') }
async function deleteHerb(row) {
  try {
    await ElMessageBox.confirm(`确定删除"${row.name}"吗?`, '提示', { type: 'warning' })
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {}
}

onMounted(loadData)
</script>
