<template>
  <div class="barcode-input-wrapper">
    <!-- 条形码列表 -->
    <div class="barcode-list" v-if="barcodes.length > 0">
      <el-tag
        v-for="(item, index) in barcodes"
        :key="index"
        :type="item.isPrimary ? 'primary' : 'info'"
        @contextmenu.prevent="showContextMenu($event, index)"
        @click="handleSetPrimary(index)"
        class="barcode-tag"
      >
        <el-icon v-if="item.isPrimary" class="primary-icon"><Star /></el-icon>
        {{ item.barcode }}
      </el-tag>
    </div>
    
    <!-- 输入框 -->
    <div class="input-row">
      <el-input
        v-model="inputValue"
        :placeholder="placeholder"
        clearable
        @keyup.enter="handleAdd"
        @blur="handleAdd"
        :class="{ 'is-error': errorMsg }"
      />
    </div>
    
    <!-- 错误提示 -->
    <div class="error-msg" v-if="errorMsg">
      <el-icon><WarningFilled /></el-icon>
      {{ errorMsg }}
    </div>
    
    <div class="tips" v-if="barcodes.length > 0 && !errorMsg">
      <el-icon><InfoFilled /></el-icon>
      点击设为主码，右键删除
    </div>
    
    <!-- 右键菜单 -->
    <Teleport to="body">
      <div
        v-if="contextMenuVisible"
        class="barcode-context-menu"
        :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }"
        @click.stop
      >
        <div class="menu-item" @click="handleSetPrimaryFromMenu">
          <el-icon><Star /></el-icon> 设为主码
        </div>
        <div class="menu-item menu-item-danger" @click="handleRemoveFromMenu">
          <el-icon><Delete /></el-icon> 删除
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Star, InfoFilled, WarningFilled, Delete } from '@element-plus/icons-vue'
import { checkBarcodeDuplicate } from '@/api/drug'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  drugId: {
    type: Number,
    default: null
  },
  placeholder: {
    type: String,
    default: '输入条形码后回车添加'
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

// 条形码列表
const barcodes = ref([])

// 输入值
const inputValue = ref('')

// 错误信息
const errorMsg = ref('')

// 右键菜单
const contextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)
const contextMenuIndex = ref(-1)

// 监听外部值变化
watch(() => props.modelValue, (val) => {
  barcodes.value = val ? [...val] : []
}, { immediate: true, deep: true })

// 添加条形码（自动检查重复）
const handleAdd = async () => {
  const val = inputValue.value?.trim()
  if (!val) return
  
  errorMsg.value = ''
  
  // 检查本地是否重复
  if (barcodes.value.some(item => item.barcode === val)) {
    errorMsg.value = '该条形码已存在于当前列表'
    return
  }
  
  // 检查数据库是否重复
  try {
    const res = await checkBarcodeDuplicate(val, props.drugId)
    if (res.code === 200 && res.data) {
      const drug = res.data
      errorMsg.value = `条形码已被占用：${drug.tradeName || drug.genericName}（${drug.drugCode || '无编码'}）`
      return
    }
  } catch (error) {
    console.error('检查条形码失败', error)
  }
  
  // 添加新条形码
  const newBarcode = {
    barcode: val,
    barcodeType: detectBarcodeType(val),
    isPrimary: barcodes.value.length === 0 // 第一个默认为主码
  }
  
  barcodes.value.push(newBarcode)
  inputValue.value = ''
  emitChange()
}

// 显示右键菜单
const showContextMenu = (e, index) => {
  contextMenuX.value = e.clientX
  contextMenuY.value = e.clientY
  contextMenuIndex.value = index
  contextMenuVisible.value = true
}

// 隐藏右键菜单
const hideContextMenu = () => {
  contextMenuVisible.value = false
  contextMenuIndex.value = -1
}

// 从右键菜单删除
const handleRemoveFromMenu = () => {
  const index = contextMenuIndex.value
  if (index < 0 || index >= barcodes.value.length) {
    hideContextMenu()
    return
  }
  
  const removed = barcodes.value.splice(index, 1)[0]
  
  // 如果删除的是主码，将第一个设为主码
  if (removed.isPrimary && barcodes.value.length > 0) {
    barcodes.value[0].isPrimary = true
  }
  
  hideContextMenu()
  emitChange()
  ElMessage.success('已删除')
}

// 从右键菜单设为主码
const handleSetPrimaryFromMenu = () => {
  const index = contextMenuIndex.value
  handleSetPrimary(index)
  hideContextMenu()
}

// 设为主码
const handleSetPrimary = (index) => {
  if (index < 0 || index >= barcodes.value.length) return
  
  barcodes.value.forEach((item, i) => {
    item.isPrimary = i === index
  })
  emitChange()
}

// 检测条形码类型
const detectBarcodeType = (barcode) => {
  if (!barcode) return 'CODE128'
  
  if (/^\d{13}$/.test(barcode)) return 'EAN13'
  if (/^\d{8}$/.test(barcode)) return 'EAN8'
  if (/^\d{12}$/.test(barcode)) return 'UPC_A'
  
  return 'CODE128'
}

// 触发变更
const emitChange = () => {
  emit('update:modelValue', [...barcodes.value])
  emit('change', [...barcodes.value])
}

// 点击其他区域关闭菜单
onMounted(() => {
  document.addEventListener('click', hideContextMenu)
})

onUnmounted(() => {
  document.removeEventListener('click', hideContextMenu)
})

// 暴露方法
defineExpose({
  getBarcodes: () => barcodes.value,
  clear: () => {
    barcodes.value = []
    inputValue.value = ''
    errorMsg.value = ''
    emitChange()
  }
})
</script>

<style scoped>
.barcode-input-wrapper {
  width: 100%;
}

.barcode-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.barcode-tag {
  cursor: pointer;
}

.barcode-tag:hover {
  opacity: 0.9;
}

.primary-icon {
  margin-right: 4px;
  vertical-align: middle;
}

.input-row {
  display: flex;
  align-items: center;
}

.input-row :deep(.el-input) {
  width: 100%;
}

.input-row :deep(.is-error .el-input__wrapper) {
  box-shadow: 0 0 0 1px #f56c6c inset;
}

.error-msg {
  margin-top: 8px;
  font-size: 12px;
  color: #f56c6c;
  display: flex;
  align-items: center;
  gap: 4px;
}

.tips {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>

<style>
/* 全局样式 - Teleport 到 body 的菜单 */
.barcode-context-menu {
  position: fixed;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 99999;
  min-width: 120px;
}

.barcode-context-menu .menu-item {
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 8px;
}

.barcode-context-menu .menu-item:hover {
  background: #f5f7fa;
  color: #409eff;
}

.barcode-context-menu .menu-item-danger:hover {
  background: #fef0f0;
  color: #f56c6c;
}
</style>
