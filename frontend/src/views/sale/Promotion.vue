<template>
  <div class="promotion">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>促销活动管理</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新建活动</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="活动名称"><el-input v-model="queryForm.name" placeholder="请输入活动名称" clearable /></el-form-item>
        <el-form-item label="活动类型">
          <el-select v-model="queryForm.type" placeholder="请选择" clearable>
            <el-option label="满减" value="reduction" />
            <el-option label="折扣" value="discount" />
            <el-option label="赠品" value="gift" />
            <el-option label="组合" value="bundle" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="未开始" value="pending" />
            <el-option label="进行中" value="active" />
            <el-option label="已暂停" value="paused" />
            <el-option label="已结束" value="ended" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="name" label="活动名称" min-width="150" />
        <el-table-column prop="type" label="活动类型" width="90">
          <template #default="{ row }"><el-tag>{{ getTypeName(row.type) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="description" label="活动规则" min-width="200" show-overflow-tooltip />
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="endTime" label="结束时间" width="170" />
        <el-table-column prop="usageCount" label="使用次数" width="90" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }"><el-tag :type="getStatusColor(row.status)">{{ getStatusName(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'pending'" type="success" link @click="handleActivate(row)">启动</el-button>
            <el-button v-if="row.status === 'active'" type="warning" link @click="handlePause(row)">暂停</el-button>
            <el-button v-if="row.status === 'paused'" type="success" link @click="handleResume(row)">恢复</el-button>
            <el-button type="danger" link @click="handleEnd(row)">结束</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total" :page-sizes="[20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" @size-change="loadData" @current-change="loadData" style="margin-top: 16px; justify-content: flex-end;" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="活动名称" prop="name"><el-input v-model="form.name" placeholder="请输入活动名称" /></el-form-item>
        <el-form-item label="活动类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择" style="width: 100%;" @change="handleTypeChange">
            <el-option label="满减" value="reduction" />
            <el-option label="折扣" value="discount" />
            <el-option label="赠品" value="gift" />
            <el-option label="组合优惠" value="bundle" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动时间" prop="timeRange">
          <el-date-picker v-model="form.timeRange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%;" />
        </el-form-item>

        <!-- 满减规则 -->
        <template v-if="form.type === 'reduction'">
          <el-form-item label="满足金额" prop="conditionAmount"><el-input-number v-model="form.conditionAmount" :min="0" :precision="2" style="width: 200px;" /><span style="margin-left: 10px;">元</span></el-form-item>
          <el-form-item label="减免金额" prop="reductionAmount"><el-input-number v-model="form.reductionAmount" :min="0" :precision="2" style="width: 200px;" /><span style="margin-left: 10px;">元</span></el-form-item>
        </template>

        <!-- 折扣规则 -->
        <template v-if="form.type === 'discount'">
          <el-form-item label="折扣率" prop="discountRate"><el-input-number v-model="form.discountRate" :min="1" :max="99" style="width: 200px;" /><span style="margin-left: 10px;">%（如85表示8.5折）</span></el-form-item>
        </template>

        <!-- 赠品规则 -->
        <template v-if="form.type === 'gift'">
          <el-form-item label="满足金额" prop="conditionAmount"><el-input-number v-model="form.conditionAmount" :min="0" :precision="2" style="width: 200px;" /><span style="margin-left: 10px;">元</span></el-form-item>
          <el-form-item label="赠品" prop="giftDrugId">
            <el-select v-model="form.giftDrugId" placeholder="请选择赠品" filterable style="width: 100%;">
              <el-option v-for="d in drugList" :key="d.id" :label="d.name" :value="d.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="赠品数量" prop="giftQuantity"><el-input-number v-model="form.giftQuantity" :min="1" style="width: 200px;" /></el-form-item>
        </template>

        <el-form-item label="适用范围" prop="scope">
          <el-radio-group v-model="form.scope">
            <el-radio label="all">全部商品</el-radio>
            <el-radio label="category">指定分类</el-radio>
            <el-radio label="drug">指定药品</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="活动说明" prop="description"><el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入活动说明" /></el-form-item>
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
import { getPromotionPage, createPromotion, updatePromotion, activatePromotion, pausePromotion, endPromotion } from '@/api/sale'
import { getDrugList } from '@/api/drug'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const drugList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const queryForm = reactive({ name: '', type: '', status: '' })
const pagination = reactive({ current: 1, size: 20, total: 0 })
const form = reactive({ id: null, name: '', type: '', timeRange: [], conditionAmount: 100, reductionAmount: 10, discountRate: 90, giftDrugId: null, giftQuantity: 1, scope: 'all', description: '' })
const rules = { name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }], type: [{ required: true, message: '请选择活动类型', trigger: 'change' }], timeRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }] }

const typeMap = { reduction: '满减', discount: '折扣', gift: '赠品', bundle: '组合' }
const statusMap = { pending: '未开始', active: '进行中', paused: '已暂停', ended: '已结束' }
const statusColorMap = { pending: 'info', active: 'success', paused: 'warning', ended: 'danger' }
const getTypeName = (t) => typeMap[t] || t
const getStatusName = (s) => statusMap[s] || s
const getStatusColor = (s) => statusColorMap[s] || 'info'

const loadData = async () => { loading.value = true; try { const res = await getPromotionPage({ current: pagination.current, size: pagination.size, ...queryForm }); tableData.value = res.data.records; pagination.total = res.data.total } catch (e) { ElMessage.error('加载失败') } finally { loading.value = false } }
const loadDrugList = async () => { try { const res = await getDrugList(); drugList.value = res.data } catch (e) { console.error(e) } }
const resetQuery = () => { queryForm.name = ''; queryForm.type = ''; queryForm.status = ''; pagination.current = 1; loadData() }
const handleTypeChange = () => { form.conditionAmount = 100; form.reductionAmount = 10; form.discountRate = 90; form.giftDrugId = null; form.giftQuantity = 1 }
const handleAdd = () => { Object.assign(form, { id: null, name: '', type: '', timeRange: [], conditionAmount: 100, reductionAmount: 10, discountRate: 90, giftDrugId: null, giftQuantity: 1, scope: 'all', description: '' }); dialogTitle.value = '新建促销活动'; dialogVisible.value = true }
const handleEdit = (row) => { Object.assign(form, row); dialogTitle.value = '编辑促销活动'; dialogVisible.value = true }
const handleSubmit = async () => { const valid = await formRef.value.validate().catch(() => false); if (!valid) return; submitting.value = true; try { if (form.id) { await updatePromotion(form.id, form) } else { await createPromotion(form) }; ElMessage.success('保存成功'); dialogVisible.value = false; loadData() } catch (e) { ElMessage.error('操作失败') } finally { submitting.value = false } }
const handleActivate = async (row) => { await activatePromotion(row.id); ElMessage.success('已启动'); loadData() }
const handlePause = async (row) => { await pausePromotion(row.id); ElMessage.success('已暂停'); loadData() }
const handleResume = async (row) => { await activatePromotion(row.id); ElMessage.success('已恢复'); loadData() }
const handleEnd = async (row) => { await ElMessageBox.confirm('确定结束该活动？', '提示'); await endPromotion(row.id); ElMessage.success('已结束'); loadData() }

onMounted(() => { loadData(); loadDrugList() })
</script>

<style scoped>
.promotion { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
