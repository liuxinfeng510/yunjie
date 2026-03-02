<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>门店管理</span>
          <el-button type="primary" @click="openDialog()">新增门店</el-button>
        </div>
      </template>

      <el-table :data="storeList" v-loading="loading" border stripe>
        <el-table-column prop="code" label="门店编码" width="100" />
        <el-table-column prop="name" label="门店名称" min-width="150" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'headquarter' ? 'primary' : 'info'" size="small">
              {{ row.type === 'headquarter' ? '总部' : '分店' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="200">
          <template #default="{ row }">
            {{ [row.province, row.city, row.district, row.address].filter(Boolean).join('') }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="licenseNo" label="营业执照号" width="180" />
        <el-table-column prop="gspCertNo" label="GSP证书号" width="160" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
              {{ row.status === 'active' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除该门店?" @confirm="handleDelete(row.id)"
              v-if="row.type !== 'headquarter'">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination style="margin-top: 16px; justify-content: flex-end;" background
        layout="total, sizes, prev, pager, next" :total="total" v-model:current-page="query.current"
        v-model:page-size="query.size" @change="loadData" />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editingStore ? '编辑门店' : '新增门店'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="门店名称" required>
          <el-input v-model="form.name" placeholder="请输入门店名称" />
        </el-form-item>
        <el-form-item label="门店编码">
          <el-input v-model="form.code" placeholder="门店编码（唯一标识）" />
        </el-form-item>
        <el-form-item label="门店类型">
          <el-select v-model="form.type" placeholder="请选择">
            <el-option label="总部" value="headquarter" />
            <el-option label="分店" value="store" />
          </el-select>
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="form.province" placeholder="省" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="form.city" placeholder="市" />
        </el-form-item>
        <el-form-item label="区/县">
          <el-input v-model="form.district" placeholder="区/县" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="form.address" placeholder="详细地址" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="门店电话" />
        </el-form-item>
        <el-form-item label="营业执照号">
          <el-input v-model="form.licenseNo" placeholder="营业执照号" />
        </el-form-item>
        <el-form-item label="GSP证书号">
          <el-input v-model="form.gspCertNo" placeholder="GSP证书编号" />
        </el-form-item>
        <el-form-item label="独立仓库">
          <el-switch v-model="form.hasWarehouse" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="正常" value="active" />
            <el-option label="停用" value="disabled" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getStorePage, createStore, updateStore, deleteStore } from '@/api/system'

const loading = ref(false)
const storeList = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 20 })
const dialogVisible = ref(false)
const editingStore = ref(null)
const submitLoading = ref(false)

const form = reactive({
  name: '', code: '', type: 'store',
  province: '', city: '', district: '', address: '',
  phone: '', licenseNo: '', gspCertNo: '',
  hasWarehouse: false, status: 'active'
})

onMounted(() => loadData())

async function loadData() {
  loading.value = true
  try {
    const res = await getStorePage(query)
    storeList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    ElMessage.error('加载门店列表失败')
  } finally {
    loading.value = false
  }
}

function openDialog(store) {
  editingStore.value = store || null
  if (store) {
    Object.assign(form, store)
  } else {
    Object.assign(form, {
      name: '', code: '', type: 'store',
      province: '', city: '', district: '', address: '',
      phone: '', licenseNo: '', gspCertNo: '',
      hasWarehouse: false, status: 'active'
    })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.name) {
    ElMessage.warning('请填写门店名称')
    return
  }
  submitLoading.value = true
  try {
    if (editingStore.value) {
      await updateStore(editingStore.value.id, form)
      ElMessage.success('更新成功')
    } else {
      await createStore(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteStore(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '删除失败')
  }
}
</script>
