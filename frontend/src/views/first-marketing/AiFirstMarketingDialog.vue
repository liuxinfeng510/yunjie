<template>
  <el-dialog v-model="visible" title="AI智能建档" width="700px" top="8vh" @close="handleClose" destroy-on-close>
    <!-- Step 1: 上传文件 -->
    <div v-if="step === 1">
      <el-alert type="info" :closable="false" style="margin-bottom: 16px;">
        上传企业证照资料（营业执照、药品经营许可证、GSP证书等），AI将自动识别并填充表单。
      </el-alert>

      <el-upload
        drag
        multiple
        action="/api/file/upload"
        :headers="uploadHeaders"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :show-file-list="false"
        accept=".jpg,.jpeg,.png,.gif,.pdf"
      >
        <el-icon :size="40" style="color: #8c939d;"><UploadFilled /></el-icon>
        <div style="margin-top: 8px; color: #606266;">将文件拖到此处，或 <em style="color:#409eff;">点击上传</em></div>
        <div style="font-size: 12px; color: #909399; margin-top: 4px;">支持 JPG/PNG/PDF 格式</div>
      </el-upload>

      <!-- 已上传文件列表 -->
      <div v-if="fileList.length > 0" style="margin-top: 16px;">
        <div style="font-size: 14px; font-weight: 500; margin-bottom: 8px; color: #303133;">已上传文件 ({{ fileList.length }})</div>
        <div
          v-for="(file, index) in fileList"
          :key="index"
          style="display: flex; align-items: center; gap: 12px; padding: 10px; border: 1px solid #ebeef5; border-radius: 4px; margin-bottom: 8px;"
        >
          <!-- 缩略图 -->
          <div style="width: 48px; height: 48px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; background: #f5f7fa; border-radius: 4px;">
            <el-icon v-if="isPdf(file.filePath)" :size="24" color="#f56c6c"><Document /></el-icon>
            <el-image v-else :src="'/api' + file.filePath" fit="cover" style="width: 48px; height: 48px; border-radius: 4px;" />
          </div>
          <!-- 文件名 -->
          <div style="flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 13px; color: #606266;">
            {{ file.fileName }}
          </div>
          <!-- 类型选择 -->
          <el-select v-model="file.docType" placeholder="选择类型" style="width: 170px;" size="small">
            <el-option label="营业执照" value="business_license" />
            <el-option label="药品经营许可证" value="drug_license" />
            <el-option label="GSP认证证书" value="gsp_cert" />
            <el-option label="法人委托书" value="legal_auth" />
            <el-option label="身份证" value="id_card" />
            <el-option label="授权委托书" value="sales_auth" />
            <el-option label="开票资料" value="billing_info" />
          </el-select>
          <!-- 删除 -->
          <el-button :icon="Delete" type="danger" size="small" link @click="fileList.splice(index, 1)" />
        </div>
      </div>
    </div>

    <!-- Step 2: 识别中 / 结果 -->
    <div v-if="step === 2">
      <div v-if="recognizing" style="text-align: center; padding: 60px 0;">
        <el-icon :size="48" class="is-loading" color="#409eff"><Loading /></el-icon>
        <div style="margin-top: 16px; font-size: 15px; color: #606266;">AI正在识别证照信息，请稍候...</div>
        <div style="margin-top: 8px; font-size: 13px; color: #909399;">共 {{ fileList.length }} 个文件，每个文件约需3-8秒</div>
      </div>

      <div v-else>
        <el-result icon="success" title="识别完成" :sub-title="resultSummary" />

        <div v-if="recognizeResult?.warnings?.length" style="margin-top: 12px;">
          <el-alert
            v-for="(w, i) in recognizeResult.warnings"
            :key="i"
            :title="w"
            type="warning"
            :closable="false"
            style="margin-bottom: 8px;"
          />
        </div>

        <!-- 识别到的字段预览 -->
        <div v-if="recognizeResult?.formData" style="margin-top: 16px;">
          <div style="font-size: 14px; font-weight: 500; margin-bottom: 8px; color: #303133;">识别到的关键字段</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item v-if="recognizeResult.formData.supplierName" label="企业名称">{{ recognizeResult.formData.supplierName }}</el-descriptions-item>
            <el-descriptions-item v-if="recognizeResult.formData.creditCode" label="信用代码">{{ recognizeResult.formData.creditCode }}</el-descriptions-item>
            <el-descriptions-item v-if="recognizeResult.formData.legalPerson" label="法定代表人">{{ recognizeResult.formData.legalPerson }}</el-descriptions-item>
            <el-descriptions-item v-if="recognizeResult.formData.businessLicenseNo" label="执照编号">{{ recognizeResult.formData.businessLicenseNo }}</el-descriptions-item>
            <el-descriptions-item v-if="recognizeResult.formData.drugLicenseNo" label="许可证号">{{ recognizeResult.formData.drugLicenseNo }}</el-descriptions-item>
            <el-descriptions-item v-if="recognizeResult.formData.gspCertNo" label="GSP证书号">{{ recognizeResult.formData.gspCertNo }}</el-descriptions-item>
            <el-descriptions-item v-if="recognizeResult.formData.salesPersonName" label="销售人员">{{ recognizeResult.formData.salesPersonName }}</el-descriptions-item>
            <el-descriptions-item v-if="recognizeResult.formData.billingBankName" label="开户银行">{{ recognizeResult.formData.billingBankName }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </div>

    <template #footer>
      <template v-if="step === 1">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :disabled="!canStartRecognize" @click="startRecognize">
          <el-icon style="margin-right: 4px;"><MagicStick /></el-icon>开始识别
        </el-button>
      </template>
      <template v-if="step === 2 && !recognizing">
        <el-button @click="step = 1">返回修改</el-button>
        <el-button type="primary" @click="confirmResult">确认并填充表单</el-button>
      </template>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Delete, MagicStick, Loading, Document } from '@element-plus/icons-vue'
import { aiRecognizeSupplierDocs } from '@/api/firstMarketing'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue', 'complete'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const step = ref(1)
const fileList = ref([])
const recognizing = ref(false)
const recognizeResult = ref(null)

const uploadHeaders = { Authorization: 'Bearer ' + (localStorage.getItem('token') || '') }

const isPdf = (path) => path && path.toLowerCase().endsWith('.pdf')

// 根据文件名智能推荐文件类型
const guessDocType = (fileName) => {
  const name = (fileName || '').toLowerCase()
  if (name.includes('执照') || name.includes('营业')) return 'business_license'
  if (name.includes('许可') || name.includes('药品经营')) return 'drug_license'
  if (name.includes('gsp') || name.includes('认证') || name.includes('质量管理')) return 'gsp_cert'
  if (name.includes('身份证')) return 'id_card'
  if (name.includes('委托') || name.includes('授权')) return 'legal_auth'
  if (name.includes('开票') || name.includes('银行') || name.includes('纳税')) return 'billing_info'
  return ''
}

const handleUploadSuccess = (response, uploadFile) => {
  if (response.code === 200) {
    fileList.value.push({
      fileName: uploadFile.name,
      filePath: response.data,
      docType: guessDocType(uploadFile.name)
    })
  } else {
    ElMessage.error('上传失败: ' + (response.message || '未知错误'))
  }
}

const handleUploadError = () => {
  ElMessage.error('文件上传失败')
}

const canStartRecognize = computed(() => {
  return fileList.value.length > 0 && fileList.value.every(f => f.docType)
})

const resultSummary = computed(() => {
  if (!recognizeResult.value?.formData) return ''
  const fieldCount = Object.keys(recognizeResult.value.formData).length
  const successCount = recognizeResult.value.fileResults?.filter(r => r.success).length || 0
  const totalCount = recognizeResult.value.fileResults?.length || 0
  return `从 ${totalCount} 个文件中成功识别 ${successCount} 个，共提取 ${fieldCount} 个字段`
})

const startRecognize = async () => {
  step.value = 2
  recognizing.value = true
  recognizeResult.value = null

  try {
    const res = await aiRecognizeSupplierDocs({
      files: fileList.value.map(f => ({ filePath: f.filePath, docType: f.docType }))
    })
    if (res.code === 200) {
      recognizeResult.value = res.data
    } else {
      ElMessage.error(res.message || 'AI识别失败')
      step.value = 1
    }
  } catch (e) {
    ElMessage.error('AI识别请求失败，请稍后重试')
    step.value = 1
  } finally {
    recognizing.value = false
  }
}

const confirmResult = () => {
  if (recognizeResult.value?.formData) {
    emit('complete', recognizeResult.value.formData)
  }
  visible.value = false
}

const handleClose = () => {
  step.value = 1
  fileList.value = []
  recognizing.value = false
  recognizeResult.value = null
}
</script>
