<template>
  <div class="member-list-container">
    <el-card shadow="never">
      <!-- 搜索和操作区 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="姓名/手机">
          <el-input v-model="searchForm.keyword" placeholder="请输入姓名或手机号" clearable @keydown.enter.prevent="handleSearch" @keydown.up.prevent="handleArrowUp" @keydown.down.prevent="handleArrowDown" />
        </el-form-item>
        <el-form-item label="会员等级">
          <el-select v-model="searchForm.levelId" placeholder="请选择" clearable>
            <el-option label="普通会员" :value="1" />
            <el-option label="银卡会员" :value="2" />
            <el-option label="金卡会员" :value="3" />
            <el-option label="钻石会员" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增会员</el-button>
          <el-button type="warning" @click="importDialogVisible = true">批量导入</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        border
        highlight-current-row
      >
        <el-table-column prop="memberNo" label="会员号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="gender" label="性别" width="60" align="center">
          <template #default="{ row }">
            {{ row.gender === 'MALE' ? '男' : row.gender === 'FEMALE' ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="levelName" label="会员等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.levelId)">{{ row.levelName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="100" align="right">
          <template #default="{ row }">
            <span style="color: #ff6b00; font-weight: 600">{{ row.points || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalConsumption" label="累计消费" width="120" align="right">
          <template #default="{ row }">
            ¥{{ (row.totalConsumption || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showDetail(row)">详情</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handlePoints(row)">积分</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="handleSearch"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="formData.gender">
            <el-radio value="MALE">男</el-radio>
            <el-radio value="FEMALE">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期" prop="birthday">
          <el-date-picker
            v-model="formData.birthday"
            type="date"
            placeholder="请选择出生日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="formData.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="会员等级" prop="levelId">
          <el-select v-model="formData.levelId" placeholder="请选择" style="width: 100%">
            <el-option label="普通会员" :value="1" />
            <el-option label="银卡会员" :value="2" />
            <el-option label="金卡会员" :value="3" />
            <el-option label="钻石会员" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="过敏信息" prop="allergyInfo">
          <el-input
            v-model="formData.allergyInfo"
            type="textarea"
            :rows="3"
            placeholder="请输入过敏信息"
          />
        </el-form-item>
        <el-form-item label="慢性病史" prop="chronicDisease">
          <el-input
            v-model="formData.chronicDisease"
            type="textarea"
            :rows="3"
            placeholder="请输入慢性病史"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      title="会员详情"
      size="50%"
      destroy-on-close
    >
      <div v-if="currentMember" class="member-detail">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="会员号">{{ currentMember.memberNo }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentMember.name }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentMember.phone }}</el-descriptions-item>
          <el-descriptions-item label="性别">
            {{ currentMember.gender === 'MALE' ? '男' : currentMember.gender === 'FEMALE' ? '女' : '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="出生日期">{{ currentMember.birthday }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ currentMember.idCard }}</el-descriptions-item>
          <el-descriptions-item label="会员等级" :span="2">
            <el-tag :type="getLevelTagType(currentMember.levelId)">{{ currentMember.levelName }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="积分">
            <span style="color: #ff6b00; font-weight: 600">{{ currentMember.points || 0 }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="累计消费">
            ¥{{ (currentMember.totalConsumption || 0).toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="过敏信息" :span="2">
            {{ currentMember.allergyInfo || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="慢性病史" :span="2">
            {{ currentMember.chronicDisease || '无' }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 积分记录 -->
        <div class="points-log">
          <h3 style="margin: 20px 0 10px 0">积分记录</h3>
          <el-table :data="pointsLog" border max-height="300">
            <el-table-column prop="changeType" label="类型" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.changeType === 'EARN' ? 'success' : 'danger'">
                  {{ row.changeType === 'EARN' ? '获得' : '消费' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="points" label="积分" width="100" align="right">
              <template #default="{ row }">
                <span :style="{ color: row.changeType === 'EARN' ? '#67c23a' : '#f56c6c' }">
                  {{ row.changeType === 'EARN' ? '+' : '-' }}{{ row.points }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="balance" label="余额" width="100" align="right" />
            <el-table-column prop="remark" label="备注" min-width="150" />
            <el-table-column prop="createTime" label="时间" width="160" />
          </el-table>
        </div>

        <!-- 消费统计 -->
        <div class="consumption-stats">
          <h3 style="margin: 20px 0 10px 0">消费统计</h3>
          <el-row :gutter="16">
            <el-col :span="8">
              <el-statistic title="累计消费" :value="currentMember.totalConsumption || 0" :precision="2" prefix="¥" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="消费次数" :value="currentMember.purchaseCount || 0" suffix="次" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="平均消费" :value="currentMember.avgConsumption || 0" :precision="2" prefix="¥" />
            </el-col>
          </el-row>
        </div>
      </div>
    </el-drawer>

    <!-- 积分操作对话框 -->
    <el-dialog
      v-model="pointsDialogVisible"
      title="积分操作"
      width="400px"
      destroy-on-close
    >
      <el-form :model="pointsForm" label-width="80px">
        <el-form-item label="操作类型">
          <el-radio-group v-model="pointsForm.type">
            <el-radio value="add">增加</el-radio>
            <el-radio value="deduct">扣减</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="积分">
          <el-input-number v-model="pointsForm.points" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="pointsForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="pointsDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePointsSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <BatchImportDialog
      v-model:visible="importDialogVisible"
      title="批量导入会员"
      :parse-api="parseMemberImport"
      :execute-api="executeMemberImport"
      @success="handleSearch"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMemberPage, getMember, createMember, updateMember, getMemberPointsLog, addPoints, deductPoints, parseMemberImport, executeMemberImport } from '@/api/member'
import BatchImportDialog from '@/components/BatchImportDialog.vue'
import { useTableKeyboardNav } from '@/composables/useTableKeyboardNav'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  levelId: null
})

// 表格数据
const tableData = ref([])
const loading = ref(false)
const { tableRef, handleArrowUp, handleArrowDown, selectFirstRow } = useTableKeyboardNav(tableData)

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增会员')
const formRef = ref(null)
const formData = reactive({
  name: '',
  phone: '',
  gender: 'MALE',
  birthday: '',
  idCard: '',
  levelId: 1,
  allergyInfo: '',
  chronicDisease: ''
})

const formRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  levelId: [{ required: true, message: '请选择会员等级', trigger: 'change' }]
}

// 详情抽屉
const drawerVisible = ref(false)
const currentMember = ref(null)
const pointsLog = ref([])

// 积分对话框
const pointsDialogVisible = ref(false)
const pointsForm = reactive({
  type: 'add',
  points: 0,
  remark: ''
})
const currentMemberId = ref(null)

// 导入对话框
const importDialogVisible = ref(false)

// 等级标签类型
const getLevelTagType = (levelId) => {
  const typeMap = { 1: '', 2: 'info', 3: 'warning', 4: 'danger' }
  return typeMap[levelId] || ''
}

// 查询会员列表
const handleSearch = async () => {
  try {
    loading.value = true
    const params = {
      ...searchForm,
      current: pagination.current,
      size: pagination.size
    }

    const res = await getMemberPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
      selectFirstRow()
    }
  } catch (error) {
    ElMessage.error('查询失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 重置搜索
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.levelId = null
  pagination.current = 1
  handleSearch()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增会员'
  Object.assign(formData, {
    id: null,
    name: '',
    phone: '',
    gender: 'MALE',
    birthday: '',
    idCard: '',
    levelId: 1,
    allergyInfo: '',
    chronicDisease: ''
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑会员'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    const apiCall = formData.id ? updateMember(formData.id, formData) : createMember(formData)
    const res = await apiCall
    
    if (res.code === 200) {
      ElMessage.success(formData.id ? '编辑成功' : '新增成功')
      dialogVisible.value = false
      handleSearch()
    }
  } catch (error) {
    if (error !== false) { // 忽略表单验证错误
      ElMessage.error('操作失败：' + error.message)
    }
  }
}

// 查看详情
const showDetail = async (row) => {
  try {
    const [memberRes, pointsRes] = await Promise.all([
      getMember(row.id),
      getMemberPointsLog(row.id, { current: 1, size: 20 })
    ])

    if (memberRes.code === 200) {
      currentMember.value = memberRes.data
    }
    if (pointsRes.code === 200) {
      pointsLog.value = pointsRes.data.records || []
    }

    drawerVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败：' + error.message)
  }
}

// 积分操作
const handlePoints = (row) => {
  currentMemberId.value = row.id
  pointsForm.type = 'add'
  pointsForm.points = 0
  pointsForm.remark = ''
  pointsDialogVisible.value = true
}

// 提交积分操作
const handlePointsSubmit = async () => {
  try {
    const apiCall = pointsForm.type === 'add' 
      ? addPoints(currentMemberId.value, { points: pointsForm.points, remark: pointsForm.remark })
      : deductPoints(currentMemberId.value, { points: pointsForm.points, remark: pointsForm.remark })

    const res = await apiCall
    if (res.code === 200) {
      ElMessage.success('操作成功')
      pointsDialogVisible.value = false
      handleSearch()
    }
  } catch (error) {
    ElMessage.error('操作失败：' + error.message)
  }
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped lang="scss">
.member-list-container {
  padding: 16px;

  .search-form {
    margin-bottom: 16px;
  }

  .member-detail {
    .points-log,
    .consumption-stats {
      margin-top: 20px;
    }
  }
}
</style>
