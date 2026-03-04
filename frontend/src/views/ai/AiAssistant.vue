<template>
  <div class="ai-assistant-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 中药识别 -->
      <el-tab-pane label="中药识别" name="herb">
        <el-card shadow="never">
          <template #header>
            <span>中药图片识别</span>
          </template>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-upload
                ref="herbUpload"
                class="upload-area"
                drag
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleHerbImageChange"
                accept="image/*"
              >
                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                <div class="el-upload__text">拖拽图片到此处，或 <em>点击上传</em></div>
              </el-upload>

              <div v-if="herbImagePreview" style="margin-top: 16px">
                <img :src="herbImagePreview" style="max-width: 100%; max-height: 300px" />
              </div>

              <el-button
                type="primary"
                style="margin-top: 16px"
                :loading="herbLoading"
                @click="recognizeHerb"
              >
                识别中药
              </el-button>
            </el-col>

            <el-col :span="12">
              <el-card v-if="herbResult" shadow="never">
                <template #header>识别结果</template>
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="状态">
                    <el-tag :type="herbResult.success ? 'success' : 'danger'">
                      {{ herbResult.success ? '识别成功' : '识别失败' }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="中药名称">
                    {{ herbResult.primaryResult || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="置信度">
                    {{ herbResult.confidence ? herbResult.confidence.toFixed(1) + '%' : '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="耗时">
                    {{ herbResult.duration ? herbResult.duration + 'ms' : '-' }}
                  </el-descriptions-item>
                </el-descriptions>

                <div v-if="herbResult.items && herbResult.items.length > 0" style="margin-top: 16px">
                  <h4>详细信息</h4>
                  <el-descriptions :column="1" border size="small">
                    <el-descriptions-item
                      v-for="(val, key) in herbResult.items[0].attributes"
                      :key="key"
                      :label="attributeLabels[key] || key"
                    >
                      {{ val }}
                    </el-descriptions-item>
                  </el-descriptions>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-tab-pane>

      <!-- OCR识别 -->
      <el-tab-pane label="OCR识别" name="ocr">
        <el-card shadow="never">
          <template #header>
            <el-radio-group v-model="ocrType" style="margin-right: 20px">
              <el-radio-button value="prescription">处方单</el-radio-button>
              <el-radio-button value="invoice">发票</el-radio-button>
              <el-radio-button value="drugPackage">药品包装</el-radio-button>
            </el-radio-group>
          </template>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-upload
                class="upload-area"
                drag
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleOcrImageChange"
                accept="image/*"
              >
                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                <div class="el-upload__text">拖拽图片到此处，或 <em>点击上传</em></div>
              </el-upload>

              <div v-if="ocrImagePreview" style="margin-top: 16px">
                <img :src="ocrImagePreview" style="max-width: 100%; max-height: 300px" />
              </div>

              <el-button
                type="primary"
                style="margin-top: 16px"
                :loading="ocrLoading"
                @click="performOcr"
              >
                开始识别
              </el-button>
            </el-col>

            <el-col :span="12">
              <el-card v-if="ocrResult" shadow="never">
                <template #header>识别结果</template>
                <el-tag :type="ocrResult.success ? 'success' : 'danger'" style="margin-bottom: 12px">
                  {{ ocrResult.success ? '识别成功' : '识别失败' }}
                </el-tag>
                <pre style="white-space: pre-wrap; font-size: 12px; background: #f5f7fa; padding: 12px; border-radius: 4px">{{ formatOcrResult(ocrResult) }}</pre>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-tab-pane>

      <!-- 销售助手 -->
      <el-tab-pane label="销售助手" name="sales">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card shadow="never">
              <template #header>智能问答</template>
              <el-input
                v-model="chatQuestion"
                type="textarea"
                :rows="3"
                placeholder="输入您的问题，如：感冒头疼吃什么药？"
              />
              <el-button
                type="primary"
                style="margin-top: 12px"
                :loading="chatLoading"
                @click="doChat"
              >
                提问
              </el-button>
              <div v-if="chatAnswer" style="margin-top: 16px; padding: 12px; background: #f5f7fa; border-radius: 4px">
                <strong>回答：</strong>
                <p style="white-space: pre-wrap; margin-top: 8px">{{ chatAnswer }}</p>
              </div>
            </el-card>
          </el-col>

          <el-col :span="12">
            <el-card shadow="never">
              <template #header>症状分析推荐</template>
              <el-form label-width="80px">
                <el-form-item label="症状描述">
                  <el-input
                    v-model="symptoms"
                    type="textarea"
                    :rows="2"
                    placeholder="描述症状，如：头疼、流鼻涕、轻微发烧"
                  />
                </el-form-item>
                <el-form-item label="患者信息">
                  <el-input v-model="patientInfo" placeholder="如：30岁男性，无过敏史" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="recommendLoading" @click="doRecommend">
                    获取推荐
                  </el-button>
                </el-form-item>
              </el-form>

              <div v-if="recommendResult && recommendResult.items" style="margin-top: 16px">
                <el-table :data="recommendResult.items" size="small" border>
                  <el-table-column prop="name" label="商品名称" />
                  <el-table-column label="类型">
                    <template #default="{ row }">{{ row.attributes?.type }}</template>
                  </el-table-column>
                  <el-table-column label="推荐理由">
                    <template #default="{ row }">{{ row.attributes?.reason }}</template>
                  </el-table-column>
                </el-table>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- 视觉结算 -->
      <el-tab-pane label="视觉结算" name="vision">
        <el-card shadow="never">
          <template #header>
            <span>商品视觉识别</span>
          </template>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-upload
                class="upload-area"
                drag
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleVisionImageChange"
                accept="image/*"
              >
                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                <div class="el-upload__text">拍摄或上传结算台商品图片</div>
              </el-upload>

              <div v-if="visionImagePreview" style="margin-top: 16px">
                <img :src="visionImagePreview" style="max-width: 100%; max-height: 300px" />
              </div>

              <el-button
                type="primary"
                style="margin-top: 16px"
                :loading="visionLoading"
                @click="doVisionCheckout"
              >
                识别商品
              </el-button>
            </el-col>

            <el-col :span="12">
              <el-card v-if="visionResult" shadow="never">
                <template #header>
                  识别结果
                  <el-tag style="margin-left: 8px">{{ visionResult.primaryResult }}</el-tag>
                </template>

                <el-table v-if="visionResult.items" :data="visionResult.items" size="small" border>
                  <el-table-column prop="name" label="商品名称" />
                  <el-table-column label="数量" width="80">
                    <template #default="{ row }">{{ row.attributes?.quantity || 1 }}</template>
                  </el-table-column>
                  <el-table-column label="规格">
                    <template #default="{ row }">{{ row.attributes?.specification || '-' }}</template>
                  </el-table-column>
                  <el-table-column label="置信度" width="80">
                    <template #default="{ row }">{{ row.confidence?.toFixed(0) }}%</template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  recognizeHerb as apiRecognizeHerb,
  ocrPrescription,
  ocrInvoice,
  ocrDrugPackage,
  salesChat,
  recommendBySymptoms,
  visionCheckout
} from '@/api/ai'

const activeTab = ref('herb')

// 中药识别
const herbImagePreview = ref('')
const herbImageBase64 = ref('')
const herbLoading = ref(false)
const herbResult = ref(null)

const attributeLabels = {
  quality: '品质等级',
  origin: '可能产地',
  characteristics: '外观特征',
  storageAdvice: '存储建议',
  warnings: '注意事项'
}

function handleHerbImageChange(file) {
  const reader = new FileReader()
  reader.onload = (e) => {
    herbImagePreview.value = e.target.result
    herbImageBase64.value = e.target.result.split(',')[1]
  }
  reader.readAsDataURL(file.raw)
}

async function recognizeHerb() {
  if (!herbImageBase64.value) {
    ElMessage.warning('请先上传图片')
    return
  }
  herbLoading.value = true
  try {
    const res = await apiRecognizeHerb({ image: herbImageBase64.value })
    herbResult.value = res.data
  } catch (e) {
    ElMessage.error('识别失败')
  } finally {
    herbLoading.value = false
  }
}

// OCR识别
const ocrType = ref('prescription')
const ocrImagePreview = ref('')
const ocrImageBase64 = ref('')
const ocrLoading = ref(false)
const ocrResult = ref(null)

function handleOcrImageChange(file) {
  const reader = new FileReader()
  reader.onload = (e) => {
    ocrImagePreview.value = e.target.result
    ocrImageBase64.value = e.target.result.split(',')[1]
  }
  reader.readAsDataURL(file.raw)
}

async function performOcr() {
  if (!ocrImageBase64.value) {
    ElMessage.warning('请先上传图片')
    return
  }
  ocrLoading.value = true
  try {
    const apiMap = {
      prescription: ocrPrescription,
      invoice: ocrInvoice,
      drugPackage: ocrDrugPackage
    }
    const res = await apiMap[ocrType.value]({ image: ocrImageBase64.value })
    ocrResult.value = res.data
  } catch (e) {
    ElMessage.error('识别失败')
  } finally {
    ocrLoading.value = false
  }
}

function formatOcrResult(result) {
  if (result.items && result.items[0]?.attributes) {
    return JSON.stringify(result.items[0].attributes, null, 2)
  }
  return result.primaryResult || result.rawResponse || '无结果'
}

// 销售助手
const chatQuestion = ref('')
const chatAnswer = ref('')
const chatLoading = ref(false)
const symptoms = ref('')
const patientInfo = ref('')
const recommendLoading = ref(false)
const recommendResult = ref(null)

async function doChat() {
  if (!chatQuestion.value.trim()) {
    ElMessage.warning('请输入问题')
    return
  }
  chatLoading.value = true
  try {
    const res = await salesChat(chatQuestion.value)
    chatAnswer.value = res.data
  } catch (e) {
    ElMessage.error('获取回答失败')
  } finally {
    chatLoading.value = false
  }
}

async function doRecommend() {
  if (!symptoms.value.trim()) {
    ElMessage.warning('请输入症状描述')
    return
  }
  recommendLoading.value = true
  try {
    const res = await recommendBySymptoms(symptoms.value, patientInfo.value)
    recommendResult.value = res.data
  } catch (e) {
    ElMessage.error('获取推荐失败')
  } finally {
    recommendLoading.value = false
  }
}

// 视觉结算
const visionImagePreview = ref('')
const visionImageBase64 = ref('')
const visionLoading = ref(false)
const visionResult = ref(null)

function handleVisionImageChange(file) {
  const reader = new FileReader()
  reader.onload = (e) => {
    visionImagePreview.value = e.target.result
    visionImageBase64.value = e.target.result.split(',')[1]
  }
  reader.readAsDataURL(file.raw)
}

async function doVisionCheckout() {
  if (!visionImageBase64.value) {
    ElMessage.warning('请先上传图片')
    return
  }
  visionLoading.value = true
  try {
    const res = await visionCheckout({ image: visionImageBase64.value })
    visionResult.value = res.data
  } catch (e) {
    ElMessage.error('识别失败')
  } finally {
    visionLoading.value = false
  }
}
</script>

<style scoped>
.ai-assistant-container {
  padding: 0;
}

.upload-area {
  width: 100%;
}

:deep(.el-upload-dragger) {
  width: 100%;
}
</style>
