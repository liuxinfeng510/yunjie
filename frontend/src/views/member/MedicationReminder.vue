<template>
  <div class="medication-reminder">
    <div v-if="fromPos" class="back-bar" style="margin-bottom: 12px;">
      <el-button type="primary" plain size="small" @click="router.push({ path: '/sale/pos', query: { memberId: route.query.memberId } })">&larr; 返回收银台</el-button>
    </div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用药提醒管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增提醒
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="会员">
          <el-input v-model="queryForm.memberName" placeholder="请输入会员姓名/手机号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="active" />
            <el-option label="暂停" value="paused" />
            <el-option label="已结束" value="ended" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="memberName" label="会员姓名" width="100" />
        <el-table-column prop="memberPhone" label="手机号" width="120" />
        <el-table-column prop="drugName" label="商品名称" width="150" />
        <el-table-column prop="dosage" label="用法用量" width="150" />
        <el-table-column prop="frequency" label="提醒频率" width="100">
          <template #default="{ row }">{{ getFrequencyName(row.frequency) }}</template>
        </el-table-column>
        <el-table-column prop="reminderTimes" label="提醒时间" width="150">
          <template #default="{ row }">
            <el-tag v-for="t in row.reminderTimes" :key="t" size="small" style="margin: 2px;">{{ t }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="110" />
        <el-table-column prop="endDate" label="结束日期" width="110" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'active'" type="warning" link @click="handlePause(row)">暂停</el-button>
            <el-button v-else-if="row.status === 'paused'" type="success" link @click="handleResume(row)">恢复</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="会员" prop="memberId">
          <el-select v-model="form.memberId" placeholder="请选择会员" filterable remote :remote-method="searchMember" style="width: 100%;">
            <el-option v-for="m in memberList" :key="m.id" :label="`${m.name} - ${m.phone}`" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="药品" prop="drugId">
          <el-select v-model="form.drugId" placeholder="请选择药品" filterable style="width: 100%;">
            <el-option v-for="d in drugList" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="用法用量" prop="dosage">
          <el-input v-model="form.dosage" placeholder="如：每次1片，每日3次" />
        </el-form-item>
        <el-form-item label="提醒频率" prop="frequency">
          <el-select v-model="form.frequency" placeholder="请选择" style="width: 100%;">
            <el-option label="每天" value="daily" />
            <el-option label="隔天" value="alternate" />
            <el-option label="每周" value="weekly" />
          </el-select>
        </el-form-item>
        <el-form-item label="提醒时间" prop="reminderTimes">
          <el-select v-model="form.reminderTimes" multiple placeholder="请选择提醒时间" style="width: 100%;">
            <el-option label="08:00" value="08:00" />
            <el-option label="12:00" value="12:00" />
            <el-option label="18:00" value="18:00" />
            <el-option label="21:00" value="21:00" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="form.startDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker v-model="form.endDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getReminderPage, createReminder, updateReminder, deleteReminder, pauseReminder, resumeReminder } from '@/api/health'
import { getDrugList } from '@/api/drug'
import { searchMembers } from '@/api/member'

const route = useRoute()
const router = useRouter()
const fromPos = computed(() => route.query.from === 'pos')

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const memberList = ref([])
const drugList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const queryForm = reactive({ memberName: '', status: '' })
const pagination = reactive({ current: 1, size: 20, total: 0 })

const form = reactive({
  id: null,
  memberId: null,
  drugId: null,
  dosage: '',
  frequency: 'daily',
  reminderTimes: [],
  startDate: '',
  endDate: '',
  remark: ''
})

const rules = {
  memberId: [{ required: true, message: '请选择会员', trigger: 'change' }],
  drugId: [{ required: true, message: '请选择药品', trigger: 'change' }],
  frequency: [{ required: true, message: '请选择提醒频率', trigger: 'change' }],
  reminderTimes: [{ required: true, message: '请选择提醒时间', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }]
}

const frequencyMap = { daily: '每天', alternate: '隔天', weekly: '每周' }
const statusMap = { active: '启用', paused: '暂停', ended: '已结束' }
const statusColorMap = { active: 'success', paused: 'warning', ended: 'info' }

const getFrequencyName = (f) => frequencyMap[f] || f
const getStatusName = (s) => statusMap[s] || s
const getStatusColor = (s) => statusColorMap[s] || 'info'

const loadData = async () => {
  loading.value = true
  try {
    const res = await getReminderPage({ current: pagination.current, size: pagination.size, ...queryForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadDrugList = async () => {
  try {
    const res = await getDrugList()
    drugList.value = res.data
  } catch (error) {
    console.error('加载药品列表失败', error)
  }
}

const searchMember = async (query) => {
  if (query) {
    try {
      const res = await searchMembers(query)
      memberList.value = res.data
    } catch (error) {
      console.error('搜索会员失败', error)
    }
  }
}

const resetQuery = () => {
  queryForm.memberName = ''
  queryForm.status = ''
  pagination.current = 1
  loadData()
}

const resetForm = () => {
  Object.assign(form, { id: null, memberId: null, drugId: null, dosage: '', frequency: 'daily', reminderTimes: [], startDate: '', endDate: '', remark: '' })
}

const handleAdd = () => { resetForm(); dialogTitle.value = '新增用药提醒'; dialogVisible.value = true }
const handleEdit = (row) => { resetForm(); Object.assign(form, row); dialogTitle.value = '编辑用药提醒'; dialogVisible.value = true }

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (form.id) { await updateReminder(form.id, form); ElMessage.success('更新成功') }
    else { await createReminder(form); ElMessage.success('创建成功') }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const handlePause = async (row) => { await pauseReminder(row.id); ElMessage.success('已暂停'); loadData() }
const handleResume = async (row) => { await resumeReminder(row.id); ElMessage.success('已恢复'); loadData() }
const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该提醒吗？', '提示', { type: 'warning' })
  await deleteReminder(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => { loadData(); loadDrugList() })
</script>

<style scoped>
.medication-reminder { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
