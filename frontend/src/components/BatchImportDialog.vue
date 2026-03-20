<template>
  <el-dialog
    :model-value="visible"
    :title="title"
    width="800px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
    @closed="handleClosed"
  >
    <!-- 步骤条 -->
    <el-steps :active="step" finish-status="success" style="margin-bottom: 24px;">
      <el-step title="上传文件" />
      <el-step title="字段映射" />
      <el-step title="导入结果" />
    </el-steps>

    <!-- Step 0: 上传文件 -->
    <div v-if="step === 0">
      <el-upload
        ref="uploadRef"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
      >
        <el-icon style="font-size: 48px; color: #909399;"><UploadFilled /></el-icon>
        <div style="margin-top: 8px;">拖拽 Excel 文件到此处，或<em>点击上传</em></div>
        <template #tip>
          <div style="color: #909399; font-size: 12px; margin-top: 8px;">
            支持 .xlsx / .xls 格式，第一行为表头
          </div>
        </template>
      </el-upload>
    </div>

    <!-- Step 1: 字段映射 + 预览 -->
    <div v-if="step === 1">
      <!-- 必填字段未映射警告 -->
      <el-alert
        v-if="unmappedRequired.length > 0"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 16px;"
      >
        <template #title>
          以下必填字段未映射：{{ unmappedRequired.map(f => f.entityFieldLabel).join('、') }}
        </template>
      </el-alert>

      <!-- 映射表格 -->
      <el-table :data="fieldMappings" border size="small" max-height="280">
        <el-table-column prop="excelHeader" label="Excel列名" width="180">
          <template #default="{ row }">
            <span>{{ row.excelHeader || '(空)' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="" width="50" align="center">
          <template #default>
            <el-icon><Right /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="映射到字段" min-width="220">
          <template #default="{ row, $index }">
            <el-select
              v-model="row.entityField"
              placeholder="(不导入)"
              clearable
              style="width: 100%;"
              @change="handleMappingChange($index, $event)"
            >
              <el-option label="(不导入)" value="" />
              <el-option
                v-for="field in availableFields"
                :key="field.entityField"
                :label="field.entityFieldLabel + (field.required ? ' *' : '')"
                :value="field.entityField"
                :disabled="isFieldUsed(field.entityField, $index)"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.entityField && row._autoMapped" type="success" size="small">自动识别</el-tag>
            <el-tag v-else-if="row.entityField" type="primary" size="small">手动设置</el-tag>
            <el-tag v-else type="info" size="small">未映射</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 数据预览 -->
      <div style="margin-top: 16px;">
        <div style="font-weight: 600; margin-bottom: 8px; color: #303133;">
          数据预览（前{{ previewData.length }}行，共{{ totalRows }}条）
        </div>
        <el-table :data="mappedPreviewData" border size="small" max-height="200" style="width: 100%;">
          <el-table-column
            v-for="col in previewColumns"
            :key="col.field"
            :prop="col.field"
            :label="col.label"
            min-width="120"
            show-overflow-tooltip
          />
        </el-table>
      </div>

      <!-- 选项 -->
      <div style="margin-top: 16px; display: flex; align-items: center; gap: 16px;">
        <el-checkbox v-model="skipDuplicate">跳过重复记录</el-checkbox>
      </div>
    </div>

    <!-- Step 2: 导入结果 -->
    <div v-if="step === 2">
      <el-result
        :icon="importResult.fail > 0 ? 'warning' : 'success'"
        :title="importResult.fail > 0 ? '导入完成（部分失败）' : '导入完成'"
      >
        <template #sub-title>
          <div style="font-size: 14px; line-height: 2;">
            总计：<b>{{ importResult.total }}</b> 条 &nbsp;
            <span style="color: #67c23a;">成功：<b>{{ importResult.success }}</b></span> &nbsp;
            <span style="color: #909399;">跳过：<b>{{ importResult.skip }}</b></span> &nbsp;
            <span style="color: #f56c6c;">失败：<b>{{ importResult.fail }}</b></span>
          </div>
        </template>
      </el-result>

      <!-- 错误详情 -->
      <div v-if="importResult.errors && importResult.errors.length > 0" style="margin-top: 16px;">
        <div style="font-weight: 600; margin-bottom: 8px; color: #909399;">导入详情（共 {{ importResult.errors.length }} 条记录）：</div>
        <el-scrollbar max-height="200">
          <div
            v-for="(err, i) in importResult.errors"
            :key="i"
            style="font-size: 13px; color: #606266; line-height: 1.8; padding: 0 8px;"
          >
            {{ err }}
          </div>
        </el-scrollbar>
      </div>
    </div>

    <!-- 底部按钮 -->
    <template #footer>
      <div style="display: flex; justify-content: space-between;">
        <div>
          <el-button v-if="step === 1" @click="step = 0">上一步</el-button>
        </div>
        <div>
          <el-button @click="handleClose">{{ step === 2 ? '关闭' : '取消' }}</el-button>
          <el-button
            v-if="step === 0"
            type="primary"
            :loading="parseLoading"
            :disabled="!selectedFile"
            @click="handleParse"
          >
            下一步
          </el-button>
          <el-button
            v-if="step === 1"
            type="primary"
            :loading="executeLoading"
            :disabled="unmappedRequired.length > 0"
            @click="handleExecute"
          >
            开始导入
          </el-button>
          <el-button
            v-if="step === 2"
            type="primary"
            @click="handleDone"
          >
            完成
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Right } from '@element-plus/icons-vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, default: '批量导入' },
  parseApi: { type: Function, required: true },
  executeApi: { type: Function, required: true }
})

const emit = defineEmits(['update:visible', 'success'])

// 状态
const step = ref(0)
const selectedFile = ref(null)
const uploadRef = ref(null)
const parseLoading = ref(false)
const executeLoading = ref(false)

// 解析结果
const fileToken = ref('')
const fieldMappings = ref([])
const availableFields = ref([])
const previewData = ref([])
const rawPreviewData = ref([])
const totalRows = ref(0)
const skipDuplicate = ref(true)

// 导入结果
const importResult = ref({ total: 0, success: 0, fail: 0, skip: 0, errors: [] })

// 计算未映射的必填字段
const unmappedRequired = computed(() => {
  const mappedFields = new Set(fieldMappings.value.filter(fm => fm.entityField).map(fm => fm.entityField))
  return availableFields.value.filter(f => f.required && !mappedFields.has(f.entityField))
})

// 检查某字段是否已被其他行使用
const isFieldUsed = (entityField, currentIndex) => {
  if (!entityField) return false
  return fieldMappings.value.some((fm, i) => i !== currentIndex && fm.entityField === entityField)
}

// 预览表格的列定义（动态跟随映射变化）
const previewColumns = computed(() => {
  return fieldMappings.value
    .filter(fm => fm.entityField)
    .map(fm => {
      const af = availableFields.value.find(f => f.entityField === fm.entityField)
      return {
        field: fm.entityField,
        label: af ? af.entityFieldLabel : fm.entityField
      }
    })
})

// 映射后的预览数据（前端实时联动）
const mappedPreviewData = computed(() => {
  if (!rawPreviewData.value.length) return []
  return rawPreviewData.value.map(row => {
    const mapped = {}
    for (const fm of fieldMappings.value) {
      if (fm.entityField) {
        // 从原始预览数据中按 excelIndex 取值
        const key = fm.excelHeader
        mapped[fm.entityField] = row[key] || ''
      }
    }
    return mapped
  })
})

// 文件选择
const handleFileChange = (uploadFile) => {
  selectedFile.value = uploadFile.raw
}

const handleFileRemove = () => {
  selectedFile.value = null
}

// 解析文件
const handleParse = async () => {
  if (!selectedFile.value) return
  parseLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    const res = await props.parseApi(formData)
    if (res.code === 200) {
      const data = res.data
      fileToken.value = data.fileToken
      availableFields.value = data.availableFields || []
      totalRows.value = data.totalRows || 0
      previewData.value = data.previewData || []

      // 构建原始预览数据（用 excelHeader 作 key）
      const headers = data.headers || []
      rawPreviewData.value = (data.previewData || []).map((row) => {
        // previewData 可能是 entityField->value 或者需要重构
        // 后端返回的 previewData 用的是 entityField 作 key
        // 我们需要用 excelHeader 作 key 以便前端调整映射时联动
        return row
      })

      // 初始化映射（标记自动映射的）
      fieldMappings.value = (data.fieldMappings || []).map(fm => ({
        ...fm,
        _autoMapped: !!fm.entityField
      }))

      step.value = 1
    }
  } catch (error) {
    ElMessage.error('解析失败：' + (error.message || '未知错误'))
  } finally {
    parseLoading.value = false
  }
}

// 映射变更
const handleMappingChange = (index, newField) => {
  fieldMappings.value[index]._autoMapped = false
  if (newField) {
    const af = availableFields.value.find(f => f.entityField === newField)
    if (af) {
      fieldMappings.value[index].entityFieldLabel = af.entityFieldLabel
      fieldMappings.value[index].required = af.required
    }
  } else {
    fieldMappings.value[index].entityFieldLabel = ''
    fieldMappings.value[index].required = false
  }
}

// 执行导入
const handleExecute = async () => {
  executeLoading.value = true
  try {
    const requestData = {
      fileToken: fileToken.value,
      fieldMappings: fieldMappings.value
        .filter(fm => fm.entityField)
        .map(fm => ({
          excelIndex: fm.excelIndex,
          excelHeader: fm.excelHeader,
          entityField: fm.entityField,
          entityFieldLabel: fm.entityFieldLabel,
          required: fm.required
        })),
      skipDuplicate: skipDuplicate.value
    }
    const res = await props.executeApi(requestData)
    if (res.code === 200) {
      importResult.value = res.data
      step.value = 2
      if (res.data.success > 0) {
        emit('success')
      }
    }
  } catch (error) {
    ElMessage.error('导入失败：' + (error.message || '未知错误'))
  } finally {
    executeLoading.value = false
  }
}

// 完成
const handleDone = () => {
  handleClose()
}

// 关闭
const handleClose = () => {
  emit('update:visible', false)
}

// 对话框关闭后重置
const handleClosed = () => {
  step.value = 0
  selectedFile.value = null
  fileToken.value = ''
  fieldMappings.value = []
  availableFields.value = []
  previewData.value = []
  rawPreviewData.value = []
  totalRows.value = 0
  skipDuplicate.value = true
  importResult.value = { total: 0, success: 0, fail: 0, skip: 0, errors: [] }
  uploadRef.value?.clearFiles()
}
</script>
