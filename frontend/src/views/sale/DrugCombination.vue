<template>
  <div class="drug-combination">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>药品组合推荐</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增组合</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="主药品"><el-input v-model="queryForm.mainDrugName" placeholder="请输入主商品名称" clearable /></el-form-item>
        <el-form-item label="推荐类型">
          <el-select v-model="queryForm.type" placeholder="请选择" clearable>
            <el-option label="联合用药" value="combination" />
            <el-option label="替代药品" value="alternative" />
            <el-option label="辅助用药" value="auxiliary" />
            <el-option label="搭配销售" value="bundle" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="mainDrugName" label="主药品" min-width="150" />
        <el-table-column prop="recommendDrugName" label="推荐药品" min-width="150" />
        <el-table-column prop="type" label="推荐类型" width="100">
          <template #default="{ row }"><el-tag :type="getTypeColor(row.type)">{{ getTypeName(row.type) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="reason" label="推荐理由" min-width="200" show-overflow-tooltip />
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }"><el-rate v-model="row.priority" disabled :max="3" /></template>
        </el-table-column>
        <el-table-column prop="usageCount" label="推荐次数" width="90" />
        <el-table-column prop="acceptRate" label="采纳率" width="80">
          <template #default="{ row }">{{ row.acceptRate }}%</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }"><el-tag :type="row.status === 'active' ? 'success' : 'info'">{{ row.status === 'active' ? '启用' : '停用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'active'" type="warning" link @click="handleDisable(row)">停用</el-button>
            <el-button v-else type="success" link @click="handleEnable(row)">启用</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total" :page-sizes="[20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" @size-change="loadData" @current-change="loadData" style="margin-top: 16px; justify-content: flex-end;" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="主药品" prop="mainDrugId">
          <el-select v-model="form.mainDrugId" placeholder="请选择主药品" filterable style="width: 100%;">
            <el-option v-for="d in drugList" :key="d.id" :label="`${d.name} - ${d.specification}`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="推荐药品" prop="recommendDrugId">
          <el-select v-model="form.recommendDrugId" placeholder="请选择推荐药品" filterable style="width: 100%;">
            <el-option v-for="d in drugList" :key="d.id" :label="`${d.name} - ${d.specification}`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="推荐类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择" style="width: 100%;">
            <el-option label="联合用药" value="combination" />
            <el-option label="替代药品" value="alternative" />
            <el-option label="辅助用药" value="auxiliary" />
            <el-option label="搭配销售" value="bundle" />
          </el-select>
        </el-form-item>
        <el-form-item label="推荐理由" prop="reason"><el-input v-model="form.reason" type="textarea" :rows="2" placeholder="请输入推荐理由" /></el-form-item>
        <el-form-item label="优先级" prop="priority"><el-rate v-model="form.priority" :max="3" /></el-form-item>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getCombinationPage, createCombination, updateCombination, deleteCombination, enableCombination, disableCombination } from '@/api/sale'
import { getDrugList } from '@/api/drug'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const drugList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const queryForm = reactive({ mainDrugName: '', type: '' })
const pagination = reactive({ current: 1, size: 20, total: 0 })
const form = reactive({ id: null, mainDrugId: null, recommendDrugId: null, type: '', reason: '', priority: 2 })
const rules = { mainDrugId: [{ required: true, message: '请选择主药品', trigger: 'change' }], recommendDrugId: [{ required: true, message: '请选择推荐药品', trigger: 'change' }], type: [{ required: true, message: '请选择推荐类型', trigger: 'change' }] }

const typeMap = { combination: '联合用药', alternative: '替代药品', auxiliary: '辅助用药', bundle: '搭配销售' }
const typeColorMap = { combination: 'primary', alternative: 'success', auxiliary: 'warning', bundle: 'info' }
const getTypeName = (t) => typeMap[t] || t
const getTypeColor = (t) => typeColorMap[t] || ''

const loadData = async () => { loading.value = true; try { const res = await getCombinationPage({ current: pagination.current, size: pagination.size, ...queryForm }); tableData.value = res.data.records; pagination.total = res.data.total } catch (e) { ElMessage.error('加载失败') } finally { loading.value = false } }
const loadDrugList = async () => { try { const res = await getDrugList(); drugList.value = res.data } catch (e) { console.error(e) } }
const resetQuery = () => { queryForm.mainDrugName = ''; queryForm.type = ''; pagination.current = 1; loadData() }
const handleAdd = () => { Object.assign(form, { id: null, mainDrugId: null, recommendDrugId: null, type: '', reason: '', priority: 2 }); dialogTitle.value = '新增药品组合'; dialogVisible.value = true }
const handleEdit = (row) => { Object.assign(form, row); dialogTitle.value = '编辑药品组合'; dialogVisible.value = true }
const handleSubmit = async () => { const valid = await formRef.value.validate().catch(() => false); if (!valid) return; if (form.mainDrugId === form.recommendDrugId) { ElMessage.warning('主药品和推荐药品不能相同'); return }; submitting.value = true; try { if (form.id) { await updateCombination(form.id, form) } else { await createCombination(form) }; ElMessage.success('保存成功'); dialogVisible.value = false; loadData() } catch (e) { ElMessage.error('操作失败') } finally { submitting.value = false } }
const handleEnable = async (row) => { await enableCombination(row.id); ElMessage.success('已启用'); loadData() }
const handleDisable = async (row) => { await disableCombination(row.id); ElMessage.success('已停用'); loadData() }
const handleDelete = async (row) => { await ElMessageBox.confirm('确定删除该组合推荐？', '提示'); await deleteCombination(row.id); ElMessage.success('删除成功'); loadData() }

onMounted(() => { loadData(); loadDrugList() })
</script>

<style scoped>
.drug-combination { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
