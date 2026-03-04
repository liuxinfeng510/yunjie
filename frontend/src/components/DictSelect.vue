<template>
  <div class="dict-select-wrapper" @contextmenu.prevent="showContextMenu">
    <el-select
      :model-value="modelValue"
      :placeholder="placeholder"
      clearable
      filterable
      style="width: 100%;"
      @update:model-value="handleChange"
    >
      <el-option
        v-for="item in options"
        :key="item.id"
        :label="item.itemValue"
        :value="item.itemValue"
      />
    </el-select>
    
    <!-- 右键菜单 -->
    <div
      v-if="contextMenuVisible"
      class="context-menu"
      :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }"
    >
      <div class="menu-item" @click="handleAddItem">新增选项</div>
      <div class="menu-item" @click="handleEditItem" v-if="modelValue">修改当前</div>
    </div>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="400px"
      append-to-body
      @close="handleDialogClose"
    >
      <el-form :model="itemForm" ref="itemFormRef" label-width="80px">
        <el-form-item label="名称" prop="itemValue" :rules="[{ required: true, message: '请输入名称' }]">
          <el-input v-model="itemForm.itemValue" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="itemForm.sortOrder" :min="0" :max="9999" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitItem" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDictList, createDictItem, updateDictItem } from '@/api/drug'

const props = defineProps({
  modelValue: String,
  dictType: {
    type: String,
    required: true
  },
  placeholder: {
    type: String,
    default: '请选择'
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

// 选项列表
const options = ref([])

// 右键菜单
const contextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增选项')
const itemFormRef = ref(null)
const submitLoading = ref(false)
const editingItem = ref(null)

const itemForm = reactive({
  itemValue: '',
  sortOrder: 0
})

// 加载字典数据
const loadOptions = async () => {
  try {
    const res = await getDictList(props.dictType)
    if (res.code === 200) {
      options.value = res.data || []
    }
  } catch (error) {
    console.error('加载字典数据失败', error)
  }
}

// 值变化
const handleChange = (val) => {
  emit('update:modelValue', val)
  emit('change', val)
}

// 显示右键菜单
const showContextMenu = (e) => {
  contextMenuX.value = e.clientX
  contextMenuY.value = e.clientY
  contextMenuVisible.value = true
}

// 隐藏右键菜单
const hideContextMenu = () => {
  contextMenuVisible.value = false
}

// 新增选项
const handleAddItem = () => {
  hideContextMenu()
  dialogTitle.value = '新增选项'
  editingItem.value = null
  itemForm.itemValue = ''
  itemForm.sortOrder = options.value.length
  dialogVisible.value = true
}

// 修改选项
const handleEditItem = () => {
  hideContextMenu()
  const currentItem = options.value.find(item => item.itemValue === props.modelValue)
  if (!currentItem) return
  
  if (currentItem.isPreset) {
    ElMessage.warning('预置数据只能修改排序')
  }
  
  dialogTitle.value = '修改选项'
  editingItem.value = currentItem
  itemForm.itemValue = currentItem.itemValue
  itemForm.sortOrder = currentItem.sortOrder || 0
  dialogVisible.value = true
}

// 提交
const handleSubmitItem = async () => {
  if (!itemFormRef.value) return
  
  await itemFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      if (editingItem.value) {
        // 修改
        await updateDictItem(editingItem.value.id, {
          itemValue: editingItem.value.isPreset ? editingItem.value.itemValue : itemForm.itemValue,
          sortOrder: itemForm.sortOrder
        })
        ElMessage.success('修改成功')
      } else {
        // 新增
        await createDictItem({
          dictType: props.dictType,
          itemValue: itemForm.itemValue,
          sortOrder: itemForm.sortOrder
        })
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      await loadOptions()
      
      // 如果是新增，自动选中新增的值
      if (!editingItem.value) {
        emit('update:modelValue', itemForm.itemValue)
      }
    } catch (error) {
      ElMessage.error(editingItem.value ? '修改失败' : '新增失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 对话框关闭
const handleDialogClose = () => {
  itemFormRef.value?.resetFields()
  editingItem.value = null
}

// 点击其他区域关闭菜单
onMounted(() => {
  loadOptions()
  document.addEventListener('click', hideContextMenu)
})

onUnmounted(() => {
  document.removeEventListener('click', hideContextMenu)
})

// 暴露刷新方法
defineExpose({ loadOptions })
</script>

<style scoped>
.dict-select-wrapper {
  position: relative;
  width: 100%;
}

.context-menu {
  position: fixed;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 9999;
  min-width: 100px;
}

.menu-item {
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
}

.menu-item:hover {
  background: #f5f7fa;
  color: #409eff;
}
</style>
