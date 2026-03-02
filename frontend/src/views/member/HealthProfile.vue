<template>
  <div class="health-profile-container">
    <el-row :gutter="20">
      <!-- 左侧：会员搜索和基本信息 -->
      <el-col :span="8">
        <el-card class="member-card">
          <template #header>
            <span>会员信息</span>
          </template>
          
          <el-form :inline="true" @submit.prevent="searchMember">
            <el-form-item>
              <el-input v-model="searchPhone" placeholder="手机号搜索" clearable>
                <template #append>
                  <el-button @click="searchMember" :icon="Search" />
                </template>
              </el-input>
            </el-form-item>
          </el-form>

          <div v-if="currentMember" class="member-info">
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="姓名">{{ currentMember.name }}</el-descriptions-item>
              <el-descriptions-item label="手机">{{ currentMember.phone }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ currentMember.gender === 'male' ? '男' : '女' }}</el-descriptions-item>
              <el-descriptions-item label="年龄">{{ memberAge }}岁</el-descriptions-item>
              <el-descriptions-item label="会员等级">{{ currentMember.levelName || '普通会员' }}</el-descriptions-item>
              <el-descriptions-item label="累计消费">{{ currentMember.totalAmount || 0 }}元</el-descriptions-item>
            </el-descriptions>

            <div class="health-tags" v-if="healthAnalysis?.tags?.length">
              <div class="tag-title">健康标签</div>
              <el-tag v-for="tag in healthAnalysis.tags" :key="tag" 
                      :type="getTagType(tag)" size="small" class="tag-item">
                {{ tag }}
              </el-tag>
            </div>
          </div>
          <el-empty v-else description="请搜索会员" />
        </el-card>

        <!-- 用药建议 -->
        <el-card class="advice-card" v-if="medicationAdvice.length">
          <template #header>
            <span>用药建议</span>
          </template>
          <ul class="advice-list">
            <li v-for="(advice, idx) in medicationAdvice" :key="idx">{{ advice }}</li>
          </ul>
        </el-card>
      </el-col>

      <!-- 中间：健康画像 -->
      <el-col :span="8">
        <el-card class="profile-card">
          <template #header>
            <div class="card-header">
              <span>健康画像</span>
              <el-button type="primary" size="small" @click="saveProfile" :disabled="!currentMember">
                保存
              </el-button>
            </div>
          </template>

          <el-form :model="healthProfile" label-width="80px" size="small" :disabled="!currentMember">
            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item label="身高(cm)">
                  <el-input-number v-model="healthProfile.height" :min="50" :max="250" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="体重(kg)">
                  <el-input-number v-model="healthProfile.weight" :min="20" :max="200" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="BMI">
              <el-tag :type="getBmiType(healthProfile.bmi)">{{ healthProfile.bmi || '--' }}</el-tag>
              <span class="bmi-hint">{{ getBmiHint(healthProfile.bmi) }}</span>
            </el-form-item>
            <el-form-item label="血型">
              <el-select v-model="healthProfile.bloodType" placeholder="选择血型">
                <el-option label="A型" value="A" />
                <el-option label="B型" value="B" />
                <el-option label="O型" value="O" />
                <el-option label="AB型" value="AB" />
              </el-select>
            </el-form-item>
            <el-form-item label="过敏史">
              <el-input v-model="healthProfile.allergyHistory" type="textarea" :rows="2" 
                        placeholder="药物/食物/其他过敏史" />
            </el-form-item>
            <el-form-item label="家族病史">
              <el-input v-model="healthProfile.familyHistory" type="textarea" :rows="2" 
                        placeholder="家族遗传病史" />
            </el-form-item>
            <el-form-item label="饮酒">
              <el-radio-group v-model="healthProfile.drinkingStatus">
                <el-radio label="none">不饮</el-radio>
                <el-radio label="occasional">偶尔</el-radio>
                <el-radio label="frequent">经常</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="吸烟">
              <el-radio-group v-model="healthProfile.smokingStatus">
                <el-radio label="none">不吸</el-radio>
                <el-radio label="quit">已戒</el-radio>
                <el-radio label="smoking">吸烟</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="运动">
              <el-radio-group v-model="healthProfile.exerciseHabit">
                <el-radio label="none">不运动</el-radio>
                <el-radio label="occasional">偶尔</el-radio>
                <el-radio label="frequent">经常</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：购买分析和慢病 -->
      <el-col :span="8">
        <el-card class="analysis-card">
          <template #header>
            <span>购买行为分析</span>
          </template>

          <div v-if="healthAnalysis">
            <div class="stat-item">
              <span class="stat-label">近6月购买次数</span>
              <span class="stat-value">{{ healthAnalysis.purchaseCount || 0 }}</span>
            </div>
            
            <div class="frequent-drugs" v-if="healthAnalysis.frequentDrugs?.length">
              <div class="section-title">常购药品TOP5</div>
              <el-table :data="healthAnalysis.frequentDrugs" size="small" stripe>
                <el-table-column prop="drugName" label="药品名称" />
                <el-table-column prop="purchaseCount" label="购买次数" width="80" />
              </el-table>
            </div>
          </div>
          <el-empty v-else description="暂无购买记录" />
        </el-card>

        <el-card class="disease-card">
          <template #header>
            <div class="card-header">
              <span>慢病记录</span>
              <el-button type="primary" size="small" @click="showDiseaseDialog" :disabled="!currentMember">
                新增
              </el-button>
            </div>
          </template>

          <el-table :data="diseaseRecords" size="small" stripe v-if="diseaseRecords.length">
            <el-table-column prop="diseaseName" label="疾病" />
            <el-table-column prop="severityLevel" label="程度" width="60">
              <template #default="{ row }">
                <el-tag :type="getSeverityType(row.severityLevel)" size="small">
                  {{ getSeverityText(row.severityLevel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="nextCheckDate" label="复查日期" width="100" />
            <el-table-column label="操作" width="60">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="editDisease(row)">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无慢病记录" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 慢病记录对话框 -->
    <el-dialog v-model="diseaseDialogVisible" :title="diseaseForm.id ? '编辑慢病记录' : '新增慢病记录'" width="500px">
      <el-form :model="diseaseForm" label-width="100px">
        <el-form-item label="疾病类型" required>
          <el-select v-model="diseaseForm.diseaseType" @change="onDiseaseTypeChange">
            <el-option label="糖尿病" value="diabetes" />
            <el-option label="高血压" value="hypertension" />
            <el-option label="高血脂" value="hyperlipidemia" />
            <el-option label="冠心病" value="coronary_heart" />
            <el-option label="哮喘" value="asthma" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="疾病名称" required>
          <el-input v-model="diseaseForm.diseaseName" />
        </el-form-item>
        <el-form-item label="诊断日期">
          <el-date-picker v-model="diseaseForm.diagnosisDate" type="date" />
        </el-form-item>
        <el-form-item label="诊断医院">
          <el-input v-model="diseaseForm.diagnosisHospital" />
        </el-form-item>
        <el-form-item label="病情程度">
          <el-radio-group v-model="diseaseForm.severityLevel">
            <el-radio label="mild">轻度</el-radio>
            <el-radio label="moderate">中度</el-radio>
            <el-radio label="severe">重度</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="下次复查">
          <el-date-picker v-model="diseaseForm.nextCheckDate" type="date" />
        </el-form-item>
        <el-form-item label="医嘱">
          <el-input v-model="diseaseForm.doctorAdvice" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="diseaseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDisease">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { pageMemberList } from '@/api/member'
import { 
  getHealthProfile, updateHealthProfile, getHealthAnalysis, getMedicationAdvice,
  getMemberDiseases, createDiseaseRecord, updateDiseaseRecord 
} from '@/api/health'

const searchPhone = ref('')
const currentMember = ref(null)
const healthProfile = ref({})
const healthAnalysis = ref(null)
const medicationAdvice = ref([])
const diseaseRecords = ref([])
const diseaseDialogVisible = ref(false)
const diseaseForm = ref({})

const memberAge = computed(() => {
  if (!currentMember.value?.birthday) return '--'
  const birth = new Date(currentMember.value.birthday)
  const today = new Date()
  let age = today.getFullYear() - birth.getFullYear()
  if (today.getMonth() < birth.getMonth() || 
      (today.getMonth() === birth.getMonth() && today.getDate() < birth.getDate())) {
    age--
  }
  return age
})

const searchMember = async () => {
  if (!searchPhone.value) {
    ElMessage.warning('请输入手机号')
    return
  }
  try {
    const res = await pageMemberList({ phone: searchPhone.value, current: 1, size: 1 })
    if (res.data?.records?.length) {
      currentMember.value = res.data.records[0]
      loadMemberData(currentMember.value.id)
    } else {
      ElMessage.warning('未找到会员')
      currentMember.value = null
    }
  } catch (error) {
    console.error('搜索会员失败', error)
  }
}

const loadMemberData = async (memberId) => {
  try {
    const [profileRes, analysisRes, adviceRes, diseaseRes] = await Promise.all([
      getHealthProfile(memberId),
      getHealthAnalysis(memberId),
      getMedicationAdvice(memberId),
      getMemberDiseases(memberId)
    ])
    healthProfile.value = profileRes.data || {}
    healthAnalysis.value = analysisRes.data || {}
    medicationAdvice.value = adviceRes.data || []
    diseaseRecords.value = diseaseRes.data || []
  } catch (error) {
    console.error('加载会员数据失败', error)
  }
}

const saveProfile = async () => {
  try {
    healthProfile.value.memberId = currentMember.value.id
    await updateHealthProfile(healthProfile.value)
    ElMessage.success('保存成功')
    loadMemberData(currentMember.value.id)
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const getTagType = (tag) => {
  const types = {
    '高血压': 'danger',
    '糖尿病': 'warning',
    '高血脂': 'warning',
    '老年人': 'info',
    '有过敏史': 'danger'
  }
  return types[tag] || ''
}

const getBmiType = (bmi) => {
  if (!bmi) return 'info'
  if (bmi < 18.5) return 'warning'
  if (bmi < 24) return 'success'
  if (bmi < 28) return 'warning'
  return 'danger'
}

const getBmiHint = (bmi) => {
  if (!bmi) return ''
  if (bmi < 18.5) return '偏瘦'
  if (bmi < 24) return '正常'
  if (bmi < 28) return '偏胖'
  return '肥胖'
}

const getSeverityType = (level) => {
  const types = { mild: 'success', moderate: 'warning', severe: 'danger' }
  return types[level] || 'info'
}

const getSeverityText = (level) => {
  const texts = { mild: '轻', moderate: '中', severe: '重' }
  return texts[level] || '--'
}

const showDiseaseDialog = () => {
  diseaseForm.value = { memberId: currentMember.value.id }
  diseaseDialogVisible.value = true
}

const editDisease = (row) => {
  diseaseForm.value = { ...row }
  diseaseDialogVisible.value = true
}

const onDiseaseTypeChange = (type) => {
  const names = {
    diabetes: '2型糖尿病',
    hypertension: '原发性高血压',
    hyperlipidemia: '高脂血症',
    coronary_heart: '冠状动脉粥样硬化性心脏病',
    asthma: '支气管哮喘'
  }
  if (names[type]) {
    diseaseForm.value.diseaseName = names[type]
  }
}

const saveDisease = async () => {
  try {
    if (diseaseForm.value.id) {
      await updateDiseaseRecord(diseaseForm.value)
    } else {
      await createDiseaseRecord(diseaseForm.value)
    }
    ElMessage.success('保存成功')
    diseaseDialogVisible.value = false
    loadMemberData(currentMember.value.id)
  } catch (error) {
    ElMessage.error('保存失败')
  }
}
</script>

<style scoped lang="scss">
.health-profile-container {
  padding: 20px;
}

.member-card, .advice-card, .profile-card, .analysis-card, .disease-card {
  margin-bottom: 15px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.member-info {
  margin-top: 15px;
}

.health-tags {
  margin-top: 15px;
  .tag-title {
    font-size: 13px;
    color: #666;
    margin-bottom: 8px;
  }
  .tag-item {
    margin: 3px;
  }
}

.advice-list {
  padding-left: 20px;
  margin: 0;
  li {
    margin: 8px 0;
    color: #666;
    font-size: 13px;
  }
}

.bmi-hint {
  margin-left: 10px;
  font-size: 12px;
  color: #999;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
  .stat-label {
    color: #666;
  }
  .stat-value {
    font-weight: bold;
    color: #409EFF;
  }
}

.section-title {
  font-size: 13px;
  color: #333;
  margin: 15px 0 10px;
  font-weight: bold;
}

.frequent-drugs {
  margin-top: 10px;
}
</style>
