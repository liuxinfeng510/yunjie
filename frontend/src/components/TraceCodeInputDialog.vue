<template>
  <el-dialog
    :model-value="visible"
    :title="`追溯码录入 — ${drugName}${batchNo ? ' (批号: ' + batchNo + ')' : ''}`"
    width="600px"
    @update:model-value="$emit('update:visible', $event)"
    @open="handleOpen"
  >
    <!-- 统计 -->
    <div class="trace-summary">
      <span>已录入：</span>
      <span :class="summaryClass">{{ codeList.length }}</span>
      <span v-if="expectedQuantity > 0"> / {{ expectedQuantity }}</span>
      <span v-if="expectedQuantity > 0 && diffText" :class="summaryClass" style="margin-left: 8px;">
        {{ diffText }}
      </span>
    </div>

    <!-- 扫码输入 -->
    <div class="scan-input-area">
      <el-input
        ref="scanInputRef"
        v-model="scanValue"
        placeholder="扫码枪扫码或手动输入，按回车添加"
        :class="{ 'scan-success': flashSuccess, 'scan-error': flashError }"
        @keyup.enter="handleScanEnter"
      >
        <template #prefix>
          <el-icon><Aim /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 批量粘贴 -->
    <el-collapse v-model="batchExpanded" class="batch-collapse">
      <el-collapse-item title="批量粘贴" name="batch">
        <el-input
          v-model="batchText"
          type="textarea"
          :rows="4"
          placeholder="每行一个追溯码，支持从 Excel 或其他系统粘贴"
        />
        <div style="margin-top: 8px; text-align: right;">
          <el-button type="primary" size="small" @click="handleBatchAdd">添加</el-button>
        </div>
      </el-collapse-item>
    </el-collapse>

    <!-- 已录入列表 -->
    <el-table :data="codeList" border size="small" max-height="300" style="margin-top: 12px;">
      <el-table-column type="index" label="#" width="50" align="center" />
      <el-table-column prop="code" label="追溯码" />
      <el-table-column label="" width="60" align="center">
        <template #default="{ $index }">
          <el-button link type="danger" size="small" @click="handleRemove($index)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Aim, Delete } from '@element-plus/icons-vue'

const props = defineProps({
  visible: Boolean,
  traceCodes: { type: Array, default: () => [] },
  expectedQuantity: { type: Number, default: 0 },
  drugName: { type: String, default: '' },
  batchNo: { type: String, default: '' }
})

const emit = defineEmits(['update:visible', 'confirm'])

const scanInputRef = ref(null)
const scanValue = ref('')
const batchText = ref('')
const batchExpanded = ref([])
const flashSuccess = ref(false)
const flashError = ref(false)

// 内部码列表，对象数组方便 el-table 渲染
const codeList = ref([])
// 用 Set 做快速去重
const codeSet = ref(new Set())

// 统计样式
const summaryClass = computed(() => {
  if (props.expectedQuantity <= 0) return ''
  if (codeList.value.length === props.expectedQuantity) return 'text-success'
  if (codeList.value.length > props.expectedQuantity) return 'text-danger'
  return 'text-warning'
})

const diffText = computed(() => {
  if (props.expectedQuantity <= 0) return ''
  const diff = codeList.value.length - props.expectedQuantity
  if (diff === 0) return '✓'
  if (diff > 0) return `超出 ${diff} 个`
  return `还差 ${-diff} 个`
})

// 对话框打开时初始化
const handleOpen = () => {
  codeList.value = (props.traceCodes || []).map(c => ({ code: c }))
  codeSet.value = new Set(props.traceCodes || [])
  scanValue.value = ''
  batchText.value = ''
  batchExpanded.value = []
  nextTick(() => {
    scanInputRef.value?.focus()
  })
}

// 扫码/回车添加
const handleScanEnter = () => {
  const code = scanValue.value.trim()
  if (!code) return

  if (codeSet.value.has(code)) {
    flashErr()
    ElMessage.warning(`追溯码 ${code} 已存在`)
    scanValue.value = ''
    return
  }

  codeSet.value.add(code)
  codeList.value.push({ code })
  scanValue.value = ''
  flashOk()
  nextTick(() => {
    scanInputRef.value?.focus()
  })
}

// 批量粘贴添加
const handleBatchAdd = () => {
  const lines = batchText.value.split('\n').map(l => l.trim()).filter(l => l)
  if (lines.length === 0) {
    ElMessage.warning('请输入追溯码')
    return
  }
  let added = 0
  let skipped = 0
  for (const code of lines) {
    if (codeSet.value.has(code)) {
      skipped++
    } else {
      codeSet.value.add(code)
      codeList.value.push({ code })
      added++
    }
  }
  batchText.value = ''
  ElMessage.success(`成功添加 ${added} 个${skipped > 0 ? `，跳过 ${skipped} 个重复` : ''}`)
}

// 删除
const handleRemove = (index) => {
  const code = codeList.value[index].code
  codeSet.value.delete(code)
  codeList.value.splice(index, 1)
}

// 确认
const handleConfirm = () => {
  emit('confirm', codeList.value.map(item => item.code))
  emit('update:visible', false)
}

// 闪烁反馈
const flashOk = () => {
  flashSuccess.value = true
  setTimeout(() => { flashSuccess.value = false }, 300)
}
const flashErr = () => {
  flashError.value = true
  setTimeout(() => { flashError.value = false }, 300)
}
</script>

<style scoped>
.trace-summary {
  margin-bottom: 12px;
  font-size: 14px;
}
.text-success { color: #67c23a; font-weight: 600; }
.text-warning { color: #e6a23c; font-weight: 600; }
.text-danger { color: #f56c6c; font-weight: 600; }

.scan-input-area {
  margin-bottom: 12px;
}
.scan-success :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #67c23a inset;
  transition: box-shadow 0.2s;
}
.scan-error :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #f56c6c inset;
  transition: box-shadow 0.2s;
}

.batch-collapse {
  border: none;
}
.batch-collapse :deep(.el-collapse-item__header) {
  height: 32px;
  line-height: 32px;
  font-size: 13px;
  color: #606266;
}
</style>
