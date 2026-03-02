<template>
  <div class="cabinet-manage-container">
    <el-card shadow="never">
      <el-row :gutter="20">
        <!-- 左侧药柜列表 -->
        <el-col :span="6">
          <div class="cabinet-list-section">
            <div class="section-header">
              <h3>药柜列表</h3>
              <el-select
                v-model="selectedStoreId"
                placeholder="选择门店"
                @change="handleStoreChange"
                style="width: 100%; margin-top: 10px"
              >
                <el-option
                  v-for="store in stores"
                  :key="store.id"
                  :label="store.name"
                  :value="store.id"
                />
              </el-select>
            </div>

            <div v-loading="cabinetLoading" class="cabinet-list">
              <div
                v-for="cabinet in cabinetList"
                :key="cabinet.id"
                :class="['cabinet-item', { active: selectedCabinetId === cabinet.id }]"
                @click="handleCabinetClick(cabinet)"
              >
                <div class="cabinet-name">{{ cabinet.name }}</div>
                <div class="cabinet-info">
                  <span>{{ cabinet.rows }}行 × {{ cabinet.columns }}列</span>
                  <span>容量: {{ cabinet.capacity }}</span>
                </div>
              </div>

              <el-empty
                v-if="!cabinetList.length && !cabinetLoading"
                description="暂无药柜数据"
              />
            </div>
          </div>
        </el-col>

        <!-- 右侧药柜格子可视化 -->
        <el-col :span="18">
          <div class="cabinet-grid-section">
            <div class="section-header">
              <h3>{{ currentCabinet?.name || '请选择药柜' }}</h3>
              <div v-if="currentCabinet" class="cabinet-desc">
                {{ currentCabinet.rows }}行 × {{ currentCabinet.columns }}列，
                位置: {{ currentCabinet.location }}
              </div>
            </div>

            <div v-if="currentCabinet" v-loading="cellLoading" class="grid-container">
              <div
                class="cabinet-grid"
                :style="{
                  gridTemplateColumns: `repeat(${currentCabinet.columns}, 1fr)`,
                  gridTemplateRows: `repeat(${currentCabinet.rows}, 1fr)`
                }"
              >
                <div
                  v-for="cell in cabinetCells"
                  :key="cell.id"
                  :class="['grid-cell', getCellStatus(cell)]"
                  @click="handleCellClick(cell)"
                >
                  <div class="cell-position">{{ cell.position }}</div>
                  <div v-if="cell.herbName" class="cell-herb">
                    {{ cell.herbName }}
                  </div>
                  <div v-if="cell.currentStock" class="cell-stock">
                    库存: {{ cell.currentStock }}{{ cell.unit || 'g' }}
                  </div>
                  <div v-if="!cell.herbName" class="cell-empty">空</div>
                </div>
              </div>
            </div>

            <el-empty
              v-else
              description="请先选择左侧药柜"
              :image-size="200"
            />
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 格子详情/分配药材对话框 -->
    <el-dialog
      v-model="cellDialogVisible"
      :title="`格子详情 - ${currentCell?.position || ''}`"
      width="600px"
    >
      <el-descriptions v-if="currentCell" :column="2" border>
        <el-descriptions-item label="位置编号">
          {{ currentCell.position }}
        </el-descriptions-item>
        <el-descriptions-item label="容量">
          {{ currentCell.capacity }}{{ currentCell.unit || 'g' }}
        </el-descriptions-item>
        <el-descriptions-item label="当前药材" :span="2">
          {{ currentCell.herbName || '未分配' }}
        </el-descriptions-item>
        <el-descriptions-item label="当前库存">
          {{ currentCell.currentStock || 0 }}{{ currentCell.unit || 'g' }}
        </el-descriptions-item>
        <el-descriptions-item label="批号">
          {{ currentCell.batchNo || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="装斗日期" :span="2">
          {{ currentCell.fillDate || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态" :span="2">
          <el-tag :type="getCellStatusType(currentCell)">
            {{ getCellStatusText(currentCell) }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="currentCell" style="margin-top: 20px">
        <el-divider content-position="left">操作</el-divider>
        <el-space>
          <el-button
            v-if="!currentCell.herbName"
            type="primary"
            @click="handleAssignHerb"
          >
            分配药材
          </el-button>
          <el-button
            v-if="currentCell.herbName"
            type="warning"
            @click="handleRefill"
          >
            重新装斗
          </el-button>
          <el-button
            v-if="currentCell.herbName"
            type="danger"
            @click="handleClean"
          >
            清斗
          </el-button>
        </el-space>
      </div>

      <template #footer>
        <el-button @click="cellDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 分配药材对话框 -->
    <el-dialog
      v-model="assignDialogVisible"
      title="分配药材"
      width="500px"
      @close="handleAssignDialogClose"
    >
      <el-form ref="assignFormRef" :model="assignForm" label-width="100px">
        <el-form-item label="选择药材" prop="herbId">
          <el-select
            v-model="assignForm.herbId"
            placeholder="请选择药材"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="herb in herbOptions"
              :key="herb.id"
              :label="`${herb.name} (${herb.category})`"
              :value="herb.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="批号" prop="batchNo">
          <el-input v-model="assignForm.batchNo" placeholder="请输入批号" />
        </el-form-item>
        <el-form-item label="装斗重量(g)" prop="fillWeight">
          <el-input-number
            v-model="assignForm.fillWeight"
            :min="0"
            :max="currentCell?.capacity || 9999"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCabinetList,
  getCabinetCells,
  createFillRecord
} from '@/api/herb'

// 门店数据（示例，实际应从API获取）
const stores = ref([
  { id: 1, name: '总店' },
  { id: 2, name: '分店1' },
  { id: 3, name: '分店2' }
])

const selectedStoreId = ref(1)
const cabinetLoading = ref(false)
const cabinetList = ref([])
const selectedCabinetId = ref(null)
const currentCabinet = ref(null)

// 格子数据
const cellLoading = ref(false)
const cabinetCells = ref([])

// 格子详情对话框
const cellDialogVisible = ref(false)
const currentCell = ref(null)

// 分配药材对话框
const assignDialogVisible = ref(false)
const assignFormRef = ref(null)
const assignForm = reactive({
  herbId: null,
  batchNo: '',
  fillWeight: 0
})

// 药材选项（示例数据）
const herbOptions = ref([
  { id: 1, name: '黄芪', category: '补虚药' },
  { id: 2, name: '党参', category: '补虚药' },
  { id: 3, name: '麦冬', category: '补虚药' },
  { id: 4, name: '金银花', category: '清热药' },
  { id: 5, name: '连翘', category: '清热药' }
])

// 获取药柜列表
const fetchCabinetList = async () => {
  cabinetLoading.value = true
  try {
    const res = await getCabinetList(selectedStoreId.value)
    if (res.code === 200) {
      cabinetList.value = res.data || []
      if (cabinetList.value.length > 0) {
        handleCabinetClick(cabinetList.value[0])
      }
    }
  } catch (error) {
    ElMessage.error('获取药柜列表失败')
  } finally {
    cabinetLoading.value = false
  }
}

// 获取药柜格子数据
const fetchCabinetCells = async (cabinetId) => {
  cellLoading.value = true
  try {
    const res = await getCabinetCells(cabinetId)
    if (res.code === 200) {
      cabinetCells.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('获取格子数据失败')
  } finally {
    cellLoading.value = false
  }
}

// 门店切换
const handleStoreChange = () => {
  selectedCabinetId.value = null
  currentCabinet.value = null
  cabinetCells.value = []
  fetchCabinetList()
}

// 药柜点击
const handleCabinetClick = (cabinet) => {
  selectedCabinetId.value = cabinet.id
  currentCabinet.value = cabinet
  fetchCabinetCells(cabinet.id)
}

// 格子点击
const handleCellClick = (cell) => {
  currentCell.value = cell
  cellDialogVisible.value = true
}

// 获取格子状态类名
const getCellStatus = (cell) => {
  if (!cell.herbName) return 'empty'
  if (cell.currentStock < cell.capacity * 0.2) return 'low-stock'
  if (cell.currentStock >= cell.capacity) return 'full'
  return 'normal'
}

// 获取格子状态类型
const getCellStatusType = (cell) => {
  if (!cell.herbName) return 'info'
  if (cell.currentStock < cell.capacity * 0.2) return 'warning'
  if (cell.currentStock >= cell.capacity) return 'success'
  return 'primary'
}

// 获取格子状态文本
const getCellStatusText = (cell) => {
  if (!cell.herbName) return '空置'
  if (cell.currentStock < cell.capacity * 0.2) return '库存不足'
  if (cell.currentStock >= cell.capacity) return '已满'
  return '正常'
}

// 分配药材
const handleAssignHerb = () => {
  assignDialogVisible.value = true
}

// 重新装斗
const handleRefill = () => {
  ElMessage.info('重新装斗功能开发中')
}

// 清斗
const handleClean = async () => {
  try {
    await ElMessageBox.confirm('确认清斗该格子吗？', '提示', {
      type: 'warning'
    })
    ElMessage.success('清斗成功')
    fetchCabinetCells(selectedCabinetId.value)
    cellDialogVisible.value = false
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清斗失败')
    }
  }
}

// 分配药材提交
const handleAssignSubmit = async () => {
  if (!assignFormRef.value) return

  if (!assignForm.herbId) {
    ElMessage.warning('请选择药材')
    return
  }

  try {
    const data = {
      cabinetId: selectedCabinetId.value,
      cellId: currentCell.value.id,
      ...assignForm
    }
    const res = await createFillRecord(data)
    if (res.code === 200) {
      ElMessage.success('分配成功')
      assignDialogVisible.value = false
      cellDialogVisible.value = false
      fetchCabinetCells(selectedCabinetId.value)
    }
  } catch (error) {
    ElMessage.error('分配失败')
  }
}

// 关闭分配对话框
const handleAssignDialogClose = () => {
  assignForm.herbId = null
  assignForm.batchNo = ''
  assignForm.fillWeight = 0
}

onMounted(() => {
  fetchCabinetList()
})
</script>

<style scoped lang="scss">
.cabinet-manage-container {
  padding: 20px;

  .cabinet-list-section {
    .section-header {
      margin-bottom: 16px;

      h3 {
        margin: 0 0 10px 0;
        font-size: 16px;
        font-weight: 600;
      }
    }

    .cabinet-list {
      max-height: 70vh;
      overflow-y: auto;

      .cabinet-item {
        padding: 12px;
        margin-bottom: 8px;
        border: 1px solid #e4e7ed;
        border-radius: 4px;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          border-color: #409eff;
          background-color: #ecf5ff;
        }

        &.active {
          border-color: #409eff;
          background-color: #ecf5ff;
        }

        .cabinet-name {
          font-weight: 600;
          margin-bottom: 8px;
          color: #303133;
        }

        .cabinet-info {
          font-size: 12px;
          color: #909399;
          display: flex;
          justify-content: space-between;
        }
      }
    }
  }

  .cabinet-grid-section {
    .section-header {
      margin-bottom: 20px;

      h3 {
        margin: 0 0 8px 0;
        font-size: 18px;
        font-weight: 600;
      }

      .cabinet-desc {
        color: #606266;
        font-size: 14px;
      }
    }

    .grid-container {
      .cabinet-grid {
        display: grid;
        gap: 8px;
        padding: 16px;
        background-color: #f5f7fa;
        border-radius: 4px;
        max-height: 65vh;
        overflow: auto;

        .grid-cell {
          min-height: 80px;
          padding: 8px;
          border: 2px solid #dcdfe6;
          border-radius: 4px;
          background-color: #fff;
          cursor: pointer;
          transition: all 0.3s;
          display: flex;
          flex-direction: column;
          justify-content: center;
          align-items: center;
          text-align: center;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
          }

          &.empty {
            background-color: #f5f7fa;
            border-color: #e4e7ed;
          }

          &.normal {
            border-color: #67c23a;
            background-color: #f0f9ff;
          }

          &.low-stock {
            border-color: #e6a23c;
            background-color: #fdf6ec;
          }

          &.full {
            border-color: #409eff;
            background-color: #ecf5ff;
          }

          .cell-position {
            font-size: 12px;
            font-weight: 600;
            color: #909399;
            margin-bottom: 4px;
          }

          .cell-herb {
            font-size: 14px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 4px;
          }

          .cell-stock {
            font-size: 12px;
            color: #606266;
          }

          .cell-empty {
            font-size: 12px;
            color: #c0c4cc;
          }
        }
      }
    }
  }
}
</style>
