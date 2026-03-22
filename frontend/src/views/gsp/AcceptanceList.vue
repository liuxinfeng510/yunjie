<template>
  <div class="acceptance-list-container">
    <el-card shadow="never">
      <!-- 筛选区 + 操作 -->
      <div class="toolbar">
        <el-form :inline="true" :model="queryForm" class="search-form">
          <el-form-item label="验收时间">
            <el-date-picker v-model="queryForm.dateRange" type="daterange"
              range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期"
              value-format="YYYY-MM-DD" style="width: 260px;" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div>
          <el-button type="primary" @click="handleAdd">新增验收记录</el-button>
          <el-button @click="handlePrint"><el-icon><Printer /></el-icon>打印</el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" style="width: 100%" border>
        <el-table-column prop="drugName" label="商品名称" min-width="150" />
        <el-table-column prop="batchNo" label="批号" width="120" />
        <el-table-column prop="supplierName" label="供应商" min-width="150" show-overflow-tooltip />
        <el-table-column prop="quantity" label="数量" width="80" align="center" />
        <el-table-column prop="appearanceCheck" label="外观检查" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.appearanceCheck === 'PASS' ? 'success' : 'danger'" size="small" v-if="row.appearanceCheck">
              {{ row.appearanceCheck === 'PASS' ? '合格' : '不合格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="packageCheck" label="包装检查" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.packageCheck === 'PASS' ? 'success' : 'danger'" size="small" v-if="row.packageCheck">
              {{ row.packageCheck === 'PASS' ? '合格' : '不合格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="labelCheck" label="标签检查" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.labelCheck === 'PASS' ? 'success' : 'danger'" size="small" v-if="row.labelCheck">
              {{ row.labelCheck === 'PASS' ? '合格' : '不合格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expireCheck" label="效期检查" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.expireCheck === 'PASS' ? 'success' : 'danger'" size="small" v-if="row.expireCheck">
              {{ row.expireCheck === 'PASS' ? '合格' : '不合格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="overallResult" label="综合结论" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.overallResult === 'PASS' ? 'success' : 'danger'" size="small">
              {{ row.overallResult === 'PASS' ? '合格' : '不合格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="acceptTime" label="验收时间" width="160" />
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="handleSearch"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增对话框 -->
    <el-dialog v-model="dialogVisible" title="新增验收记录" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="商品名称" prop="drugName">
          <el-input v-model="formData.drugName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="批号" prop="batchNo">
          <el-input v-model="formData.batchNo" placeholder="请输入批号" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId">
          <SupplierSelect
            v-model="formData.supplierName"
            v-model:supplier-id="formData.supplierId"
            placeholder="输入供应商名称/拼音搜索"
          />
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="formData.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="外观检查" prop="appearanceCheck">
          <el-radio-group v-model="formData.appearanceCheck">
            <el-radio value="PASS">合格</el-radio>
            <el-radio value="FAIL">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="包装检查" prop="packageCheck">
          <el-radio-group v-model="formData.packageCheck">
            <el-radio value="PASS">合格</el-radio>
            <el-radio value="FAIL">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标签检查" prop="labelCheck">
          <el-radio-group v-model="formData.labelCheck">
            <el-radio value="PASS">合格</el-radio>
            <el-radio value="FAIL">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="效期检查" prop="expireCheck">
          <el-radio-group v-model="formData.expireCheck">
            <el-radio value="PASS">合格</el-radio>
            <el-radio value="FAIL">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="综合结论" prop="overallResult">
          <el-radio-group v-model="formData.overallResult">
            <el-radio value="PASS">合格</el-radio>
            <el-radio value="FAIL">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="formData.overallResult === 'FAIL'" label="不合格原因" prop="rejectReason">
          <el-input v-model="formData.rejectReason" type="textarea" :rows="3" placeholder="请输入不合格原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Printer } from '@element-plus/icons-vue'
import { getAcceptances, createAcceptance } from '@/api/gsp'
import SupplierSelect from '@/components/SupplierSelect.vue'

const tableData = ref([])
const loading = ref(false)

// 当月起止
const now = new Date()
const monthStart = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`
const monthEnd = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate()}`

const queryForm = reactive({ dateRange: [monthStart, monthEnd] })
const pagination = reactive({ current: 1, size: 20, total: 0 })

// 对话框
const dialogVisible = ref(false)
const formRef = ref(null)
const formData = reactive({
  drugName: '', batchNo: '', supplierId: null, supplierName: '',
  quantity: 1, appearanceCheck: 'PASS', packageCheck: 'PASS',
  labelCheck: 'PASS', expireCheck: 'PASS', overallResult: 'PASS', rejectReason: ''
})
const formRules = {
  drugName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  batchNo: [{ required: true, message: '请输入批号', trigger: 'blur' }],
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  appearanceCheck: [{ required: true, message: '请选择', trigger: 'change' }],
  packageCheck: [{ required: true, message: '请选择', trigger: 'change' }],
  labelCheck: [{ required: true, message: '请选择', trigger: 'change' }],
  expireCheck: [{ required: true, message: '请选择', trigger: 'change' }],
  overallResult: [{ required: true, message: '请选择', trigger: 'change' }]
}

const handleSearch = async () => {
  loading.value = true
  try {
    const params = { pageNum: pagination.current, pageSize: pagination.size }
    if (queryForm.dateRange && queryForm.dateRange.length === 2) {
      params.startTime = queryForm.dateRange[0] + 'T00:00:00'
      params.endTime = queryForm.dateRange[1] + 'T23:59:59'
    }
    const res = await getAcceptances(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (e) { ElMessage.error('查询失败') }
  finally { loading.value = false }
}

const resetQuery = () => {
  queryForm.dateRange = [monthStart, monthEnd]
  pagination.current = 1
  handleSearch()
}

const handleAdd = () => {
  Object.assign(formData, {
    drugName: '', batchNo: '', supplierId: null, supplierName: '',
    quantity: 1, appearanceCheck: 'PASS', packageCheck: 'PASS',
    labelCheck: 'PASS', expireCheck: 'PASS', overallResult: 'PASS', rejectReason: ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    const submitData = {
      drugName: formData.drugName, batchNo: formData.batchNo,
      supplierId: formData.supplierId, quantity: formData.quantity,
      appearanceCheck: formData.appearanceCheck, packageCheck: formData.packageCheck,
      labelCheck: formData.labelCheck, expireCheck: formData.expireCheck,
      overallResult: formData.overallResult, rejectReason: formData.rejectReason || null
    }
    const res = await createAcceptance(submitData)
    if (res.code === 200) {
      ElMessage.success('新增成功')
      dialogVisible.value = false
      handleSearch()
    }
  } catch (e) { if (e !== false) ElMessage.error('操作失败') }
}

const checkText = v => v === 'PASS' ? '合格' : '不合格'

const handlePrint = async () => {
  // 获取全部数据（大页）
  let allData = tableData.value
  try {
    const params = { pageNum: 1, pageSize: 9999 }
    if (queryForm.dateRange && queryForm.dateRange.length === 2) {
      params.startTime = queryForm.dateRange[0] + 'T00:00:00'
      params.endTime = queryForm.dateRange[1] + 'T23:59:59'
    }
    const res = await getAcceptances(params)
    if (res.code === 200) allData = res.data.records || allData
  } catch {}

  const dateLabel = queryForm.dateRange?.length === 2
    ? `${queryForm.dateRange[0]} 至 ${queryForm.dateRange[1]}` : ''

  const rows = allData.map((row, idx) => `<tr>
    <td style="text-align:center">${idx + 1}</td>
    <td>${row.drugName || ''}</td>
    <td>${row.batchNo || ''}</td>
    <td>${row.supplierName || ''}</td>
    <td style="text-align:center">${row.quantity ?? ''}</td>
    <td style="text-align:center">${checkText(row.appearanceCheck)}</td>
    <td style="text-align:center">${checkText(row.packageCheck)}</td>
    <td style="text-align:center">${checkText(row.labelCheck)}</td>
    <td style="text-align:center">${checkText(row.expireCheck)}</td>
    <td style="text-align:center">${checkText(row.overallResult)}</td>
    <td style="text-align:center">${(row.acceptTime || '').substring(0, 10)}</td>
  </tr>`).join('')

  const w = window.open('', '_blank')
  w.document.write(`<!DOCTYPE html><html><head><title>药品验收记录</title>
<style>
  @page { size: A4 portrait; margin: 10mm 8mm; }
  * { margin:0; padding:0; box-sizing:border-box; }
  body { font-family: SimSun, serif; font-size: 10px; }
  h2 { text-align:center; font-size:16px; margin:4px 0 2px; }
  .sub { text-align:center; color:#333; font-size:10px; margin-bottom:4px; }
  table { width:100%; border-collapse:collapse; table-layout:fixed; }
  th, td { border:1px solid #000; padding:2px 3px; word-break:break-all; line-height:1.3; }
  th { background:#e8e8e8; font-weight:bold; font-size:10px; }
  td { font-size:9px; }
  .footer { display:flex; justify-content:space-between; font-size:10px; margin-top:6px; padding:0 4px; }
</style></head><body>
<h2>药品验收记录</h2>
<div class="sub">时间范围：${dateLabel}　　　药房：云峰智慧药房</div>
<table>
  <colgroup>
    <col style="width:4%"><col style="width:15%"><col style="width:9%"><col style="width:15%">
    <col style="width:5%"><col style="width:7%"><col style="width:7%"><col style="width:7%">
    <col style="width:7%"><col style="width:7%"><col style="width:9%">
  </colgroup>
  <thead><tr>
    <th>序号</th><th>商品名称</th><th>批号</th><th>供应商</th><th>数量</th>
    <th>外观</th><th>包装</th><th>标签</th><th>效期</th><th>结论</th><th>验收日期</th>
  </tr></thead>
  <tbody>${rows || '<tr><td colspan="11" style="text-align:center">暂无数据</td></tr>'}</tbody>
</table>
<div class="footer">
  <span>验收人：____________</span>
  <span>审核人：____________</span>
  <span>打印日期：${new Date().toLocaleDateString('zh-CN')}</span>
</div>
</body></html>`)
  w.document.close()
  w.onload = () => { w.print() }
}

onMounted(() => { handleSearch() })
</script>

<style scoped lang="scss">
.acceptance-list-container {
  padding: 16px;
  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    flex-wrap: wrap;
    gap: 12px;
  }
  .search-form { margin-bottom: 0; }
}
</style>
