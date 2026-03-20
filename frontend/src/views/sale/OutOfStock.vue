<template>
  <div class="out-of-stock">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>缺货登记</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>登记缺货</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="商品名称"><el-input v-model="queryForm.drugName" placeholder="请输入商品名称" clearable @keydown.enter.prevent="loadData" @keydown.up.prevent="handleArrowUp" @keydown.down.prevent="handleArrowDown" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="待处理" value="pending" />
            <el-option label="采购中" value="purchasing" />
            <el-option label="已到货" value="arrived" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="registerNo" label="登记编号" width="130" />
        <el-table-column prop="drugName" label="商品名称" min-width="150" />
        <el-table-column prop="specification" label="规格" width="100" />
        <el-table-column prop="requestQuantity" label="需求数量" width="100" />
        <el-table-column prop="memberName" label="登记会员" width="100" />
        <el-table-column prop="memberPhone" label="联系电话" width="120" />
        <el-table-column prop="registerDate" label="登记日期" width="110" />
        <el-table-column prop="expectedDate" label="预计到货" width="110" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }"><el-tag :type="getStatusColor(row.status)">{{ getStatusName(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'pending'" type="success" link @click="handleStartPurchase(row)">开始采购</el-button>
            <el-button v-if="row.status === 'purchasing'" type="warning" link @click="handleArrived(row)">确认到货</el-button>
            <el-button v-if="row.status === 'arrived'" type="info" link @click="handleNotify(row)">通知会员</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total" :page-sizes="[20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" @size-change="loadData" @current-change="loadData" style="margin-top: 16px; justify-content: flex-end;" />
    </el-card>

    <el-dialog v-model="dialogVisible" title="登记缺货" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="药品" prop="drugId">
          <el-select v-model="form.drugId" placeholder="请选择药品" filterable style="width: 100%;">
            <el-option v-for="d in drugList" :key="d.id" :label="`${d.name} - ${d.specification}`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="需求数量" prop="requestQuantity"><el-input-number v-model="form.requestQuantity" :min="1" style="width: 100%;" /></el-form-item>
        <el-form-item label="登记会员" prop="memberId">
          <el-select v-model="form.memberId" placeholder="请选择会员" filterable remote :remote-method="searchMember" style="width: 100%;">
            <el-option v-for="m in memberList" :key="m.id" :label="`${m.name} - ${m.phone}`" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
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
import { getOutOfStockPage, createOutOfStock, startPurchase, confirmArrived, notifyMember } from '@/api/sale'
import { getDrugList } from '@/api/drug'
import { searchMembers } from '@/api/member'
import { useTableKeyboardNav } from '@/composables/useTableKeyboardNav'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const { tableRef, handleArrowUp, handleArrowDown, selectFirstRow } = useTableKeyboardNav(tableData)
const drugList = ref([])
const memberList = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)

const queryForm = reactive({ drugName: '', status: '' })
const pagination = reactive({ current: 1, size: 20, total: 0 })
const form = reactive({ drugId: null, requestQuantity: 1, memberId: null, remark: '' })
const rules = { drugId: [{ required: true, message: '请选择药品', trigger: 'change' }], requestQuantity: [{ required: true, message: '请输入需求数量', trigger: 'blur' }] }

const statusMap = { pending: '待处理', purchasing: '采购中', arrived: '已到货', cancelled: '已取消' }
const statusColorMap = { pending: 'warning', purchasing: 'primary', arrived: 'success', cancelled: 'info' }
const getStatusName = (s) => statusMap[s] || s
const getStatusColor = (s) => statusColorMap[s] || 'info'

const loadData = async () => { loading.value = true; try { const res = await getOutOfStockPage({ current: pagination.current, size: pagination.size, ...queryForm }); tableData.value = res.data.records; pagination.total = res.data.total; selectFirstRow() } catch (e) { ElMessage.error('加载失败') } finally { loading.value = false } }
const loadDrugList = async () => { try { const res = await getDrugList(); drugList.value = res.data } catch (e) { console.error(e) } }
const searchMember = async (q) => { if (q) { try { const res = await searchMembers(q); memberList.value = res.data } catch (e) { console.error(e) } } }
const resetQuery = () => { queryForm.drugName = ''; queryForm.status = ''; pagination.current = 1; loadData() }
const handleAdd = () => { Object.assign(form, { drugId: null, requestQuantity: 1, memberId: null, remark: '' }); dialogVisible.value = true }
const handleSubmit = async () => { const valid = await formRef.value.validate().catch(() => false); if (!valid) return; submitting.value = true; try { await createOutOfStock(form); ElMessage.success('登记成功'); dialogVisible.value = false; loadData() } catch (e) { ElMessage.error('操作失败') } finally { submitting.value = false } }
const handleDetail = (row) => ElMessage.info('详情功能开发中')
const handleStartPurchase = async (row) => { await startPurchase(row.id); ElMessage.success('已开始采购'); loadData() }
const handleArrived = async (row) => { await confirmArrived(row.id); ElMessage.success('已确认到货'); loadData() }
const handleNotify = async (row) => { await notifyMember(row.id); ElMessage.success('已通知会员'); loadData() }

onMounted(() => { loadData(); loadDrugList() })
</script>

<style scoped>
.out-of-stock { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 16px; }
</style>
