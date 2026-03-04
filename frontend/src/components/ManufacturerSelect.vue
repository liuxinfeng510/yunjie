<template>
  <div class="manufacturer-select-wrapper">
    <el-autocomplete
      v-model="displayValue"
      :fetch-suggestions="handleSearch"
      :placeholder="placeholder"
      clearable
      style="width: 100%;"
      value-key="name"
      @select="handleSelect"
      @clear="handleClear"
      @blur="handleBlur"
    >
      <template #default="{ item }">
        <div class="suggestion-item">
          <span class="name">{{ item.name }}</span>
          <span class="short-name" v-if="item.shortName">{{ item.shortName }}</span>
        </div>
      </template>
    </el-autocomplete>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { searchManufacturer, getOrCreateManufacturer } from '@/api/drug'

const props = defineProps({
  modelValue: String,
  manufacturerId: Number,
  placeholder: {
    type: String,
    default: '输入企业名称搜索'
  }
})

const emit = defineEmits(['update:modelValue', 'update:manufacturerId', 'change'])

// 显示值
const displayValue = ref(props.modelValue || '')

// 监听外部值变化
watch(() => props.modelValue, (val) => {
  displayValue.value = val || ''
})

// 搜索建议
const handleSearch = async (queryString, callback) => {
  if (!queryString) {
    callback([])
    return
  }
  
  try {
    const res = await searchManufacturer(queryString)
    if (res.code === 200) {
      callback(res.data || [])
    } else {
      callback([])
    }
  } catch (error) {
    console.error('搜索生产企业失败', error)
    callback([])
  }
}

// 选择建议项
const handleSelect = (item) => {
  displayValue.value = item.name
  emit('update:modelValue', item.name)
  emit('update:manufacturerId', item.id)
  emit('change', item)
}

// 清空
const handleClear = () => {
  displayValue.value = ''
  emit('update:modelValue', '')
  emit('update:manufacturerId', null)
  emit('change', null)
}

// 失去焦点时自动创建
const handleBlur = async () => {
  const val = displayValue.value?.trim()
  if (!val) return
  
  // 如果输入值与当前值不同，尝试获取或创建
  if (val !== props.modelValue) {
    try {
      const res = await getOrCreateManufacturer(val)
      if (res.code === 200 && res.data) {
        emit('update:modelValue', res.data.name)
        emit('update:manufacturerId', res.data.id)
        emit('change', res.data)
      }
    } catch (error) {
      console.error('创建生产企业失败', error)
    }
  }
}
</script>

<style scoped>
.manufacturer-select-wrapper {
  width: 100%;
}

.suggestion-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.suggestion-item .name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.suggestion-item .short-name {
  color: #909399;
  font-size: 12px;
  margin-left: 8px;
}
</style>
