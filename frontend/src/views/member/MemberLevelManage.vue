<template>
  <div class="member-level-container">
    <el-card shadow="never">
      <div class="table-header">
        <h3 style="margin:0;">会员等级配置</h3>
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增等级</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" stripe border style="width:100%;">
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="name" label="等级名称" width="150" />
        <el-table-column label="最低消费金额" width="150" align="right">
          <template #default="{ row }">¥{{ row.minAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="折扣率" width="120" align="center">
          <template #default="{ row }">
            <el-tag>{{ (row.discount * 10).toFixed(1) }}折</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="积分倍率" width="120" align="center">
          <template #default="{ row }">{{ row.pointsRate }}倍</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑会员等级' : '新增会员等级'" width="500px" @close="handleDialogClose">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="等级名称" prop="name">
          <el-input v-model="formData.name" placeholder="如：普通会员、银卡、金卡" />
        </el-form-item>
        <el-form-item label="最低消费金额" prop="minAmount">
          <el-input-number v-model="formData.minAmount" :min="0" :precision="2" style="width:100%;" placeholder="累计消费达到此金额自动升级" />
        </el-form-item>
        <el-form-item label="折扣率" prop="discount">
          <el-input-number v-model="formData.discount" :min="0.01" :max="1" :step="0.05" :precision="2" style="width:100%;" />
          <div style="color:#909399; font-size:12px; margin-top:4px;">
            例: 0.95 = 9.5折, 0.88 = 8.8折, 1.00 = 不打折
          </div>
        </el-form-item>
        <el-form-item label="积分倍率" prop="pointsRate">
          <el-input-number v-model="formData.pointsRate" :min="1" :max="10" :step="0.5" :precision="1" style="width:100%;" />
          <div style="color:#909399; font-size:12px; margin-top:4px;">
            消费1元获得积分 = 基础积分 x 倍率
          </div>
        </el-form-item>
        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" style="width:100%;" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getMemberLevelList,
  createMemberLevel,
  updateMemberLevel,
  deleteMemberLevel
} from '@/api/member'

const loading = ref(false)
const tableData = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)
const submitLoading = ref(false)

const formData = reactive({
  name: '',
  minAmount: 0,
  discount: 1.00,
  pointsRate: 1.0,
  sortOrder: 0
})

const formRules = {
  name: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
  minAmount: [{ required: true, message: '请输入最低消费金额', trigger: 'blur' }],
  discount: [{ required: true, message: '请输入折扣率', trigger: 'blur' }],
  pointsRate: [{ required: true, message: '请输入积分倍率', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMemberLevelList()
    if (res.code === 200) { tableData.value = res.data || [] }
  } catch { ElMessage.error('获取数据失败') } finally { loading.value = false }
}

const handleAdd = () => {
  isEdit.value = false
  editId.value = null
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  Object.assign(formData, {
    name: row.name,
    minAmount: row.minAmount,
    discount: row.discount,
    pointsRate: row.pointsRate,
    sortOrder: row.sortOrder
  })
  dialogVisible.value = true
}

const handleDialogClose = () => {
  Object.assign(formData, { name: '', minAmount: 0, discount: 1.00, pointsRate: 1.0, sortOrder: 0 })
  formRef.value?.clearValidate()
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value) {
        const res = await updateMemberLevel(editId.value, formData)
        if (res.code === 200) { ElMessage.success('更新成功'); dialogVisible.value = false; fetchData() }
      } else {
        const res = await createMemberLevel(formData)
        if (res.code === 200) { ElMessage.success('创建成功'); dialogVisible.value = false; fetchData() }
      }
    } catch { ElMessage.error('操作失败') } finally { submitLoading.value = false }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除等级"${row.name}"吗？`, '提示', { type: 'warning' })
    const res = await deleteMemberLevel(row.id)
    if (res.code === 200) { ElMessage.success('删除成功'); fetchData() }
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

onMounted(() => { fetchData() })
</script>

<style scoped lang="scss">
.member-level-container {
  padding: 20px;
  .table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }
}
</style>
