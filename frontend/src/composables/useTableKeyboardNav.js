import { ref, nextTick } from 'vue'

export function useTableKeyboardNav(dataList) {
  const tableRef = ref(null)
  const selectedIndex = ref(-1)

  const handleArrowUp = () => {
    if (!dataList.value || dataList.value.length === 0) return
    if (selectedIndex.value > 0) {
      selectedIndex.value--
      tableRef.value?.setCurrentRow(dataList.value[selectedIndex.value])
    }
  }

  const handleArrowDown = () => {
    if (!dataList.value || dataList.value.length === 0) return
    if (selectedIndex.value < dataList.value.length - 1) {
      selectedIndex.value++
      tableRef.value?.setCurrentRow(dataList.value[selectedIndex.value])
    }
  }

  const selectFirstRow = () => {
    if (dataList.value && dataList.value.length > 0) {
      selectedIndex.value = 0
      nextTick(() => tableRef.value?.setCurrentRow(dataList.value[0]))
    } else {
      selectedIndex.value = -1
    }
  }

  const resetSelection = () => {
    selectedIndex.value = -1
    tableRef.value?.setCurrentRow(null)
  }

  return { tableRef, selectedIndex, handleArrowUp, handleArrowDown, selectFirstRow, resetSelection }
}
