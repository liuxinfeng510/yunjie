<template>
  <div class="supplier-select-wrapper">
    <el-autocomplete
      v-model="displayValue"
      :fetch-suggestions="handleSearch"
      :placeholder="placeholder"
      :trigger-on-focus="true"
      :debounce="300"
      clearable
      style="width: 100%;"
      value-key="name"
      @select="handleSelect"
      @clear="handleClear"
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
import { searchSupplier } from '@/api/drug'

const props = defineProps({
  modelValue: String,
  supplierId: [Number, Object],
  placeholder: {
    type: String,
    default: '输入供应商名称/拼音搜索'
  }
})

const emit = defineEmits(['update:modelValue', 'update:supplierId', 'change'])

const displayValue = ref(props.modelValue || '')

watch(() => props.modelValue, (val) => {
  displayValue.value = val || ''
})

const handleSearch = async (queryString, callback) => {
  try {
    const res = await searchSupplier(queryString || '')
    callback(res.data || [])
  } catch (error) {
    callback([])
  }
}

const handleSelect = (item) => {
  displayValue.value = item.name
  emit('update:modelValue', item.name)
  emit('update:supplierId', item.id)
  emit('change', item)
}

const handleClear = () => {
  displayValue.value = ''
  emit('update:modelValue', '')
  emit('update:supplierId', null)
  emit('change', null)
}
</script>

<style scoped>
.supplier-select-wrapper {
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
