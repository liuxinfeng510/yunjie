<template>
  <div class="staff-training">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>员工培训管理</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增培训</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="培训主题"><el-input v-model="queryForm.title" placeholder="请输入培训主题" clearable @keydown.enter.prevent="loadData" @keydown.up.prevent="handleArrowUp" @keydown.down.prevent="handleArrowDown" /></el-form-item>
        <el-form-item label="培训类型">
          <el-select v-model="queryForm.type" placeholder="请选择" clearable>
            <el-option label="GSP法规" value="gsp" />
            <el-option label="药品知识" value="drug" />
            <el-option label="操作技能" value="skill" />
            <el-option label="安全培训" value="safety" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="计划中" value="planned" />
            <el-option label="进行中" value="ongoing" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="title" label="培训主题" min-width="150" />
        <el-table-column prop="type" label="培训类型" width="100">
          <template #default="{ row }"><el-tag>{{ getTypeName(row.type) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="trainer" label="培训讲师" width="100" />
        <el-table-column prop="trainingDate" label="培训日期" width="110" />
        <el-table-column prop="duration" label="时长(小时)" width="100" />
        <el-table-column prop="participantCount" label="参训人数" width="100" />
        <el-table-column prop="passRate" label="考核通过率" width="110">
          <template #default="{ row }">
            <span v-if="row.passRate !== null" :style="{ color: row.passRate >= 80 ? '#67c23a' : '#f56c6c' }">{{ row.passRate }}%</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }"><el-tag :type="getStatusColor(row.status)">{{ getStatusName(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'completed'" type="success" link @click="handleExportRecord(row)">导出记录</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total" :page-sizes="[20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" @size-change="loadData" @current-change="loadData" style="margin-top: 16px; justify-content: flex-end;" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="培训主题" prop="title"><el-input v-model="form.title" placeholder="请输入培训主题" /></el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="培训类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择" style="width: 100%;">
                <el-option label="GSP法规" value="gsp" /><el-option label="药品知识" value="drug" /><el-option label="操作技能" value="skill" /><el-option label="安全培训" value="safety" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="培训讲师" prop="trainer"><el-input v-model="form.trainer" placeholder="请输入讲师姓名" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="培训日期" prop="trainingDate"><el-date-picker v-model="form.trainingDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%;" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="时长(小时)" prop="duration"><el-input-number v-model="form.duration" :min="0.5" :max="24" :step="0.5" style="width: 100%;" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="培训内容" prop="content"><el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入培训内容" /></el-form-item>
        <el-form-item label="参训人员" prop="participants">
          <el-select v-model="form.participants" multiple placeholder="请选择参训人员" style="width: 100%;">
            <el-option v-for="s in staffList" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getTrainingPage, createTraining, updateTraining } from '@/api/gsp'
import { useTableKeyboardNav } from '@/composables/useTableKeyboardNav'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const { tableRef, handleArrowUp, handleArrowDown, selectFirstRow } = useTableKeyboardNav(tableData)
const staffList = ref([{ id: 1, name: '张三' }, { id: 2, name: '李四' }, { id: 3, name: '王五' }])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const queryForm = reactive({ title: '', type: '', status: '' })
const pagination = reactive({ current: 1, size: 20, total: 0 })
const form = reactive({ id: null, title: '', type: '', trainer: '', trainingDate: '', duration: 2, content: '', participants: [] })
const rules = { title: [{ required: true, message: '请输入培训主题', trigger: 'blur' }], type: [{ required: true, message: '请选择培训类型', trigger: 'change' }], trainingDate: [{ required: true, message: '请选择培训日期', trigger: 'change' }] }

const typeMap = { gsp: 'GSP法规', drug: '药品知识', skill: '操作技能', safety: '安全培训' }
const statusMap = { planned: '计划中', ongoing: '进行中', completed: '已完成', cancelled: '已取消' }
const statusColorMap = { planned: 'info', ongoing: 'primary', completed: 'success', cancelled: 'danger' }
const getTypeName = (t) => typeMap[t] || t
const getStatusName = (s) => statusMap[s] || s
const getStatusColor = (s) => statusColorMap[s] || 'info'

const loadData = async () => { loading.value = true; try { const res = await getTrainingPage({ current: pagination.current, size: pagination.size, ...queryForm }); tableData.value = res.data.records; pagination.total = res.data.total; selectFirstRow() } catch (e) { ElMessage.error('加载失败') } finally { loading.value = false } }
const resetQuery = () => { queryForm.title = ''; queryForm.type = ''; queryForm.status = ''; pagination.current = 1; loadData() }
const handleAdd = () => { Object.assign(form, { id: null, title: '', type: '', trainer: '', trainingDate: '', duration: 2, content: '', participants: [] }); dialogTitle.value = '新增培训'; dialogVisible.value = true }
const handleEdit = (row) => { Object.assign(form, row); dialogTitle.value = '编辑培训'; dialogVisible.value = true }
const handleSubmit = async () => { const valid = await formRef.value.validate().catch(() => false); if (!valid) return; submitting.value = true; try { if (form.id) { await updateTraining(form.id, form) } else { await createTraining(form) }; ElMessage.success('保存成功'); dialogVisible.value = false; loadData() } catch (e) { ElMessage.error('操作失败') } finally { submitting.value = false } }
const handleDetail = (row) => ElMessage.info('详情功能开发中')
const handleExportRecord = (row) => ElMessage.info('导出功能开发中')

onMounted(() => loadData())
</script>

<style scoped>
.staff-training { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
