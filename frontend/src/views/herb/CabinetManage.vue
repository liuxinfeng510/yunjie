<template>
  <div class="cabinet-manage-container">
    <el-card shadow="never">
      <el-row :gutter="20">
        <!-- 左侧药柜列表 -->
        <el-col :span="4">
          <div class="cabinet-list-section">
            <div class="section-header">
              <h3>药柜列表</h3>
              <el-button 
                type="primary" 
                size="small" 
                @click="handleAddCabinet"
                style="margin-top: 10px; width: 100%"
              >
                <el-icon><Plus /></el-icon>
                新增药柜
              </el-button>
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
                  <span>{{ cabinet.rowCount }}行 × {{ cabinet.columnCount }}列</span>
                  <span v-if="cabinet.columnStartNumber > 1">列号: {{ cabinet.columnStartNumber }}~{{ cabinet.columnStartNumber + cabinet.columnCount - 1 }}</span>
                  <span>格位: {{ getCabinetTotalCells(cabinet) }}</span>
                </div>
                <div class="cabinet-actions">
                  <el-button 
                    type="primary" 
                    link 
                    size="small" 
                    @click.stop="handleEditCabinet(cabinet)"
                  >
                    编辑
                  </el-button>
                  <el-button 
                    v-if="!boundCabinetIds.has(cabinet.id)"
                    type="danger" 
                    link 
                    size="small" 
                    @click.stop="handleDeleteCabinet(cabinet)"
                  >
                    删除
                  </el-button>
                </div>
              </div>

              <el-empty
                v-if="!cabinetList.length && !cabinetLoading"
                description="暂无药柜数据，请先添加药柜"
              />
            </div>
          </div>
        </el-col>

        <!-- 中间药柜格子可视化 -->
        <el-col :span="quickAssignMode ? 14 : 20">
          <div class="cabinet-grid-section">
            <div class="section-header">
              <div class="section-header-left">
                <h3>{{ currentCabinet?.name || '请选择药柜' }}</h3>
                <div v-if="currentCabinet" class="cabinet-desc">
                  {{ currentCabinet.rowCount }}行 × {{ currentCabinet.columnCount }}列，
                  位置: {{ currentCabinet.location || '未设置' }}
                </div>
              </div>
              <div class="section-header-right">
                <el-switch
                  v-model="quickAssignMode"
                  active-text="快速分配"
                  inactive-text=""
                  @change="onQuickAssignToggle"
                />
              </div>
            </div>

            <div v-if="currentCabinet" v-loading="cellLoading" class="grid-container">
              <!-- 按行渲染 -->
              <div
                v-for="row in currentCabinet.rowCount"
                :key="row"
                class="cabinet-row"
              >
                <div class="row-label">第{{ row }}行</div>
                <div class="row-drawers">
                  <!-- 每行内按列渲染每个斗 -->
                  <div
                    v-for="col in currentCabinet.columnCount"
                    :key="col"
                    class="drawer-box"
                  >
                    <div class="drawer-label">{{ row }}-{{ col + (currentCabinet.columnStartNumber || 1) - 1 }}</div>
                    <div
                      class="drawer-cells"
                      :class="'cells-' + getDrawerSubCount(row, col)"
                    >
                      <!-- 斗内子格 -->
                      <div
                        v-for="cellData in getDrawerCells(row, col)"
                        :key="cellData.cell.id"
                        :class="['sub-cell', getCellStatus(cellData), { 'quick-assign-target': quickAssignMode && !cellData.herb && selectedHerbForAssign, 'quick-assign-removable': quickAssignMode && cellData.herb }]"
                        @click="handleCellClick(cellData)"
                      >
                        <div class="sub-label">{{ subIndexToLetter(cellData.cell.subIndex) }}</div>
                        <div v-if="cellData.herb" class="cell-herb">{{ cellData.herb.name }}</div>
                        <div v-if="cellData.cell.currentStock" class="cell-stock">{{ cellData.cell.currentStock }}g</div>
                        <div v-if="!cellData.herb" class="cell-empty">空</div>
                      </div>
                    </div>
                  </div>
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

        <!-- 右侧快速分配面板 -->
        <el-col v-if="quickAssignMode" :span="6">
          <div class="quick-assign-panel">
            <div class="panel-header">
              <h3>快速分配</h3>
              <div class="assign-stats">
                <el-tag type="success" size="small">已分配 {{ assignStats.assigned }}</el-tag>
                <el-tag type="info" size="small">未分配 {{ assignStats.unassigned }}</el-tag>
              </div>
            </div>

            <!-- 批量操作按钮 -->
            <div class="batch-actions" v-if="currentCabinet">
              <el-button size="small" :icon="Download" @click="handleDownloadTemplate">下载模板</el-button>
              <el-button size="small" type="primary" :icon="Upload" @click="batchImportDialogVisible = true">批量导入</el-button>
            </div>

            <!-- 搜索框 -->
            <el-input
              v-model="herbSearchKeyword"
              placeholder="搜索药材名称/拼音"
              clearable
              size="small"
              style="margin-bottom: 8px"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>

            <!-- 筛选标签 -->
            <el-radio-group v-model="herbFilterTab" size="small" style="margin-bottom: 8px">
              <el-radio-button value="unassigned">未分配</el-radio-button>
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button value="assigned">已分配</el-radio-button>
            </el-radio-group>

            <!-- 默认值设置 -->
            <div class="default-values">
              <span class="default-label">默认值:</span>
              <el-input-number v-model="defaultMinStock" :min="0" size="small" style="width: 80px" />
              <span class="default-sep">/</span>
              <el-input-number v-model="defaultCurrentStock" :min="0" size="small" style="width: 80px" />
              <span class="default-unit">g (最小/当前)</span>
            </div>

            <!-- 药材列表 -->
            <div class="herb-list-scroll">
              <div
                v-for="herb in filteredHerbList"
                :key="herb.id"
                :class="['herb-item', { selected: selectedHerbForAssign?.id === herb.id, assigned: assignedHerbNames.has(herb.name) }]"
                @click="handleSelectHerbForAssign(herb)"
              >
                <div class="herb-name">{{ herb.name }}</div>
                <div class="herb-meta">
                  <span v-if="herb.category" class="herb-category">{{ herb.category }}</span>
                  <el-icon v-if="assignedHerbNames.has(herb.name)" class="assigned-icon" color="#67c23a"><Check /></el-icon>
                </div>
              </div>
              <el-empty
                v-if="!filteredHerbList.length"
                description="无匹配药材"
                :image-size="60"
              />
            </div>

            <!-- 操作提示 -->
            <div class="assign-tip">
              <template v-if="selectedHerbForAssign">
                已选: <strong>{{ selectedHerbForAssign.name }}</strong>，点击空格子完成分配
                <el-button type="danger" size="small" link @click="selectedHerbForAssign = null" style="margin-left: 8px">取消选中</el-button>
              </template>
              <template v-else>
                请先选择右侧药材，再点击空格子分配
              </template>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 药柜编辑对话框 -->
    <el-dialog
      v-model="cabinetDialogVisible"
      :title="editCabinetForm.id ? '编辑药柜' : '新增药柜'"
      width="600px"
    >
      <el-form 
        ref="cabinetFormRef" 
        :model="editCabinetForm" 
        :rules="cabinetRules" 
        label-width="100px"
      >
        <el-form-item label="药柜名称" prop="name">
          <el-input v-model="editCabinetForm.name" placeholder="请输入药柜名称" />
        </el-form-item>
        <el-form-item label="行数" prop="rowCount">
          <el-input-number 
            v-model="editCabinetForm.rowCount" 
            :min="1" 
            :max="20" 
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="列数" prop="columnCount">
          <el-input-number 
            v-model="editCabinetForm.columnCount" 
            :min="1" 
            :max="20" 
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="起始列号">
          <el-input-number 
            v-model="editCabinetForm.columnStartNumber" 
            :min="1" 
            :max="999" 
            style="width: 200px"
          />
          <span style="margin-left: 12px; font-size: 12px; color: #909399">
            多柜连续编号时，设为上一柜末列号+1
          </span>
        </el-form-item>
        
        <!-- 每行子格数配置 -->
        <el-form-item label="每行格数">
          <div class="row-config-area">
            <div
              v-for="row in editCabinetForm.rowCount"
              :key="row"
              class="row-config-item"
            >
              <span class="row-config-label">第{{ row }}行:</span>
              <el-select
                v-model="rowCellConfigEdit[row]"
                style="width: 120px"
                size="small"
              >
                <el-option :label="'1格'" :value="1" />
                <el-option :label="'2格'" :value="2" />
                <el-option :label="'3格'" :value="3" />
                <el-option :label="'4格'" :value="4" />
              </el-select>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item label="位置" prop="location">
          <el-input v-model="editCabinetForm.location" placeholder="请输入药柜位置" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="editCabinetForm.status" style="width: 100%">
            <el-option label="启用" value="ENABLED" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="cabinetDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveCabinet">保存</el-button>
      </template>
    </el-dialog>

    <!-- 格子详情/分配药材对话框 -->
    <el-dialog
      v-model="cellDialogVisible"
      :title="`格子详情 - ${currentCell?.cell?.label || ''}`"
      width="600px"
    >
      <el-descriptions v-if="currentCell" :column="2" border>
        <el-descriptions-item label="位置编号">
          {{ currentCell.cell.label }}
        </el-descriptions-item>
        <el-descriptions-item label="当前药材" :span="2">
          {{ currentCell.herb?.name || '未分配' }}
        </el-descriptions-item>
        <el-descriptions-item label="当前库存">
          {{ currentCell.cell.currentStock || 0 }}g
        </el-descriptions-item>
        <el-descriptions-item label="最小库存">
          {{ currentCell.cell.minStock || 0 }}g
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
            v-if="!currentCell.herb"
            type="primary"
            @click="handleAssignHerb"
          >
            分配药材
          </el-button>
          <el-button
            v-if="currentCell.herb"
            type="warning"
            @click="handleRefill"
          >
            补货
          </el-button>
          <el-button
            v-if="currentCell.herb"
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
              :label="`${herb.name} (${herb.category || ''})`"
              :value="herb.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="最小库存(g)" prop="minStock">
          <el-input-number
            v-model="assignForm.minStock"
            :min="0"
            :max="9999"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="当前库存(g)" prop="currentStock">
          <el-input-number
            v-model="assignForm.currentStock"
            :min="0"
            :max="9999"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <el-dialog
      v-model="batchImportDialogVisible"
      title="批量导入格位分配"
      width="500px"
      @close="handleImportDialogClose"
    >
      <template v-if="!importResult">
        <el-upload
          ref="importUploadRef"
          drag
          :auto-upload="false"
          :limit="1"
          accept=".xlsx,.xls"
          :on-change="handleImportFileChange"
          :on-remove="() => importFile = null"
        >
          <el-icon style="font-size: 40px; color: #909399"><Upload /></el-icon>
          <div style="margin-top: 8px">拖拽Excel文件到此处，或<em>点击上传</em></div>
          <template #tip>
            <div style="color: #909399; font-size: 12px">支持 .xlsx/.xls 格式，请先下载模板填写</div>
          </template>
        </el-upload>
        <el-checkbox v-model="importOverwrite" style="margin-top: 12px">覆盖已分配的格位</el-checkbox>
      </template>

      <template v-else>
        <el-result :icon="importResult.fail > 0 ? 'warning' : 'success'" :title="'导入完成'">
          <template #sub-title>
            <div style="display: flex; gap: 16px; justify-content: center; margin-top: 8px">
              <el-tag>总计 {{ importResult.total }}</el-tag>
              <el-tag type="success">成功 {{ importResult.success }}</el-tag>
              <el-tag type="info">跳过 {{ importResult.skip }}</el-tag>
              <el-tag type="danger">失败 {{ importResult.fail }}</el-tag>
            </div>
          </template>
        </el-result>
        <div v-if="importResult.errors && importResult.errors.length" style="max-height: 200px; overflow-y: auto; margin-top: 8px; padding: 8px; background: #fafafa; border-radius: 4px; font-size: 12px; color: #f56c6c">
          <div v-for="(err, idx) in importResult.errors" :key="idx">{{ err }}</div>
        </div>
      </template>

      <template #footer>
        <template v-if="!importResult">
          <el-button @click="batchImportDialogVisible = false">取消</el-button>
          <el-button type="primary" :disabled="!importFile" :loading="importLoading" @click="handleBatchImportSubmit">开始导入</el-button>
        </template>
        <template v-else>
          <el-button type="primary" @click="batchImportDialogVisible = false">完成</el-button>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Check, Download, Upload } from '@element-plus/icons-vue'
import {
  getCabinetList,
  getCabinetCells,
  createCabinet,
  updateCabinet,
  deleteCabinet,
  assignCabinetCell,
  refillCabinetCell,
  cleanCabinetCell,
  getHerbDrugList,
  getAssignedHerbIds,
  downloadCabinetImportTemplate,
  batchImportCabinetAssignment,
  getBoundCabinetIds
} from '@/api/herb'

// 药柜数据
const cabinetLoading = ref(false)
const cabinetList = ref([])
const selectedCabinetId = ref(null)
const currentCabinet = ref(null)
const boundCabinetIds = ref(new Set())

// 格子数据
const cellLoading = ref(false)
const cabinetCells = ref([])

// 药材选项（从API加载）
const herbOptions = ref([])

// 快速分配模式
const quickAssignMode = ref(false)
const selectedHerbForAssign = ref(null)
const assignedHerbNames = ref(new Set())
const herbSearchKeyword = ref('')
const herbFilterTab = ref('unassigned')
const defaultMinStock = ref(100)
const defaultCurrentStock = ref(200)

// 快速分配 - 筛选后的药材列表
const filteredHerbList = computed(() => {
  let list = herbOptions.value
  // 按分配状态筛选（按名称匹配）
  if (herbFilterTab.value === 'unassigned') {
    list = list.filter(h => !assignedHerbNames.value.has(h.name))
  } else if (herbFilterTab.value === 'assigned') {
    list = list.filter(h => assignedHerbNames.value.has(h.name))
  }
  // 按关键词搜索
  const kw = herbSearchKeyword.value.trim().toLowerCase()
  if (kw) {
    list = list.filter(h =>
      (h.name && h.name.includes(kw)) ||
      (h.pinyin && h.pinyin.toLowerCase().includes(kw)) ||
      (h.pinyinShort && h.pinyinShort.toLowerCase().includes(kw))
    )
  }
  return list
})

// 快速分配 - 统计
const assignStats = computed(() => {
  const total = herbOptions.value.length
  const assigned = herbOptions.value.filter(h => assignedHerbNames.value.has(h.name)).length
  return { assigned, unassigned: total - assigned }
})

// 药柜编辑对话框
const cabinetDialogVisible = ref(false)
const cabinetFormRef = ref(null)
const editCabinetForm = reactive({
  id: null,
  name: '',
  rowCount: 5,
  columnCount: 8,
  columnStartNumber: 1,
  location: '',
  status: 'ENABLED'
})
// 每行格数编辑
const rowCellConfigEdit = reactive({})

const cabinetRules = {
  name: [{ required: true, message: '请输入药柜名称', trigger: 'blur' }],
  rowCount: [{ required: true, message: '请输入行数', trigger: 'blur' }],
  columnCount: [{ required: true, message: '请输入列数', trigger: 'blur' }]
}

// 格子详情对话框
const cellDialogVisible = ref(false)
const currentCell = ref(null)

// 分配药材对话框
const assignDialogVisible = ref(false)
const assignFormRef = ref(null)
const assignForm = reactive({
  herbId: null,
  minStock: 100,
  currentStock: 200
})

// 将平铺的cells按 row-col 分组
const groupedCells = computed(() => {
  const map = {}
  for (const item of cabinetCells.value) {
    const row = item.cell.rowNum
    const col = item.cell.columnNum
    const key = `${row}-${col}`
    if (!map[key]) map[key] = []
    map[key].push(item)
  }
  // 按subIndex排序
  for (const key of Object.keys(map)) {
    map[key].sort((a, b) => (a.cell.subIndex || 1) - (b.cell.subIndex || 1))
  }
  return map
})

// 解析当前药柜的rowCellConfig
const currentRowCellConfig = computed(() => {
  if (!currentCabinet.value?.rowCellConfig) return {}
  try {
    return JSON.parse(currentCabinet.value.rowCellConfig)
  } catch {
    return {}
  }
})

// watch rowCount变化，同步rowCellConfigEdit
watch(() => editCabinetForm.rowCount, (newVal) => {
  for (let i = 1; i <= 20; i++) {
    if (i > newVal) {
      delete rowCellConfigEdit[i]
    } else if (!rowCellConfigEdit[i]) {
      rowCellConfigEdit[i] = 3 // 默认3格
    }
  }
})

// 获取某斗的子格数据
function getDrawerCells(row, col) {
  return groupedCells.value[`${row}-${col}`] || []
}

// 获取某斗的子格数量
function getDrawerSubCount(row, col) {
  const cells = getDrawerCells(row, col)
  if (cells.length > 0) return cells.length
  // 从配置获取
  const config = currentRowCellConfig.value
  return config[String(row)] || 1
}

// 子格序号转字母
function subIndexToLetter(idx) {
  const letters = ['A', 'B', 'C', 'D']
  return letters[(idx || 1) - 1] || String(idx)
}

// 计算药柜总格位数
function getCabinetTotalCells(cabinet) {
  if (!cabinet.rowCellConfig) {
    return cabinet.rowCount * cabinet.columnCount
  }
  try {
    const config = JSON.parse(cabinet.rowCellConfig)
    let total = 0
    for (let row = 1; row <= cabinet.rowCount; row++) {
      const subCount = config[String(row)] || 1
      total += subCount * cabinet.columnCount
    }
    return total
  } catch {
    return cabinet.rowCount * cabinet.columnCount
  }
}

// 获取药柜列表
const fetchCabinetList = async () => {
  cabinetLoading.value = true
  try {
    const res = await getCabinetList(1)
    if (res.code === 200) {
      cabinetList.value = Array.isArray(res.data) ? res.data : []
      if (cabinetList.value.length > 0 && !selectedCabinetId.value) {
        handleCabinetClick(cabinetList.value[0])
      }
    }
    // 加载已绑定药柜ID
    const boundRes = await getBoundCabinetIds()
    if (boundRes.code === 200) {
      boundCabinetIds.value = new Set(boundRes.data || [])
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
      cabinetCells.value = Array.isArray(res.data) ? res.data : []
    }
  } catch (error) {
    ElMessage.error('获取格子数据失败')
  } finally {
    cellLoading.value = false
  }
}

// 加载药材列表
const fetchHerbOptions = async () => {
  try {
    const res = await getHerbDrugList()
    if (res.code === 200) {
      herbOptions.value = Array.isArray(res.data) ? res.data : []
    }
  } catch (error) {
    console.warn('获取药材列表失败', error)
  }
}

// 新增药柜
const handleAddCabinet = () => {
  Object.assign(editCabinetForm, {
    id: null,
    name: '',
    rowCount: 5,
    columnCount: 8,
    columnStartNumber: 1,
    location: '',
    status: 'ENABLED'
  })
  // 初始化每行格数配置，默认3格
  for (let i = 1; i <= 20; i++) {
    if (i <= 5) {
      rowCellConfigEdit[i] = 3
    } else {
      delete rowCellConfigEdit[i]
    }
  }
  cabinetDialogVisible.value = true
}

// 编辑药柜
const handleEditCabinet = (cabinet) => {
  Object.assign(editCabinetForm, {
    id: cabinet.id,
    name: cabinet.name,
    rowCount: cabinet.rowCount,
    columnCount: cabinet.columnCount,
    columnStartNumber: cabinet.columnStartNumber || 1,
    location: cabinet.location,
    status: cabinet.status
  })
  // 还原每行格数配置
  let config = {}
  if (cabinet.rowCellConfig) {
    try { config = JSON.parse(cabinet.rowCellConfig) } catch {}
  }
  for (let i = 1; i <= 20; i++) {
    if (i <= cabinet.rowCount) {
      rowCellConfigEdit[i] = config[String(i)] || 1
    } else {
      delete rowCellConfigEdit[i]
    }
  }
  cabinetDialogVisible.value = true
}

// 保存药柜
const handleSaveCabinet = async () => {
  if (!cabinetFormRef.value) return
  
  try {
    await cabinetFormRef.value.validate()
    
    // 构造rowCellConfig JSON
    const configObj = {}
    for (let i = 1; i <= editCabinetForm.rowCount; i++) {
      configObj[String(i)] = rowCellConfigEdit[i] || 3
    }
    
    const data = {
      ...editCabinetForm,
      storeId: 1,
      rowCellConfig: JSON.stringify(configObj)
    }
    
    let res
    if (editCabinetForm.id) {
      res = await updateCabinet(editCabinetForm.id, data)
    } else {
      res = await createCabinet(data)
    }
    
    if (res.code === 200) {
      ElMessage.success(editCabinetForm.id ? '更新成功' : '新增成功')
      cabinetDialogVisible.value = false
      await fetchCabinetList()
      // 如果编辑的是当前选中药柜，刷新格子
      if (editCabinetForm.id && selectedCabinetId.value === editCabinetForm.id) {
        const updated = cabinetList.value.find(c => c.id === editCabinetForm.id)
        if (updated) {
          currentCabinet.value = updated
          fetchCabinetCells(updated.id)
        }
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(editCabinetForm.id ? '更新失败' : '新增失败')
    }
  }
}

// 删除药柜
const handleDeleteCabinet = async (cabinet) => {
  try {
    await ElMessageBox.confirm(`确认删除药柜"${cabinet.name}"吗？`, '提示', {
      type: 'warning'
    })
    
    const res = await deleteCabinet(cabinet.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      if (selectedCabinetId.value === cabinet.id) {
        selectedCabinetId.value = null
        currentCabinet.value = null
        cabinetCells.value = []
      }
      fetchCabinetList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 药柜点击
const handleCabinetClick = (cabinet) => {
  selectedCabinetId.value = cabinet.id
  currentCabinet.value = cabinet
  fetchCabinetCells(cabinet.id)
}

// 格子点击
const handleCellClick = async (cell) => {
  if (quickAssignMode.value) {
    // 快速分配模式：点击已分配格子 = 取消分配（清斗）
    if (cell.herb) {
      try {
        await ElMessageBox.confirm(`确认取消分配"${cell.herb.name}"吗？`, '取消分配', { type: 'warning' })
        const res = await cleanCabinetCell(cell.cell.id)
        if (res.code === 200) {
          ElMessage.success(`已取消: ${cell.herb.name}`)
          fetchAssignedHerbNames()
          fetchCabinetCells(selectedCabinetId.value)
        }
      } catch (error) {
        if (error !== 'cancel') ElMessage.error('取消分配失败')
      }
      return
    }
    // 快速分配模式：选中药材 + 点击空格子 = 直接分配
    if (selectedHerbForAssign.value && !cell.herb) {
      try {
        const res = await assignCabinetCell(cell.cell.id, {
          herbId: selectedHerbForAssign.value.id,
          minStock: defaultMinStock.value,
          currentStock: defaultCurrentStock.value
        })
        if (res.code === 200) {
          ElMessage.success(`已分配: ${selectedHerbForAssign.value.name}`)
          assignedHerbNames.value.add(selectedHerbForAssign.value.name)
          assignedHerbNames.value = new Set(assignedHerbNames.value)
          selectedHerbForAssign.value = null
          fetchCabinetCells(selectedCabinetId.value)
        }
      } catch (error) {
        ElMessage.error('分配失败')
      }
      return
    }
    return
  }
  // 普通模式：打开格子详情对话框
  currentCell.value = cell
  cellDialogVisible.value = true
}

// 获取格子状态类名
const getCellStatus = (cell) => {
  if (!cell.herb) return 'empty'
  if (cell.cell.currentStock < (cell.cell.minStock || 50)) return 'low-stock'
  return 'normal'
}

// 获取格子状态类型
const getCellStatusType = (cell) => {
  if (!cell.herb) return 'info'
  if (cell.cell.currentStock < (cell.cell.minStock || 50)) return 'warning'
  return 'primary'
}

// 获取格子状态文本
const getCellStatusText = (cell) => {
  if (!cell.herb) return '空置'
  if (cell.cell.currentStock < (cell.cell.minStock || 50)) return '库存不足'
  return '正常'
}

// 分配药材
const handleAssignHerb = () => {
  assignForm.herbId = null
  assignForm.minStock = 100
  assignForm.currentStock = 200
  assignDialogVisible.value = true
}

// 补货
const handleRefill = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入补货量(克)', '补货', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^\d+(\.\d+)?$/,
      inputErrorMessage: '请输入有效数字'
    })
    
    const amount = parseFloat(value)
    if (amount <= 0) {
      ElMessage.warning('补货量必须大于0')
      return
    }
    
    const res = await refillCabinetCell(currentCell.value.cell.id, { amount })
    if (res.code === 200) {
      ElMessage.success('补货成功')
      cellDialogVisible.value = false
      fetchCabinetCells(selectedCabinetId.value)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('补货失败')
    }
  }
}

// 清斗
const handleClean = async () => {
  try {
    await ElMessageBox.confirm('确认清斗该格子吗？清斗后药材分配将被清除。', '提示', {
      type: 'warning'
    })
    
    const res = await cleanCabinetCell(currentCell.value.cell.id)
    if (res.code === 200) {
      ElMessage.success('清斗成功')
      cellDialogVisible.value = false
      fetchCabinetCells(selectedCabinetId.value)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清斗失败')
    }
  }
}

// 分配药材提交
const handleAssignSubmit = async () => {
  if (!assignForm.herbId) {
    ElMessage.warning('请选择药材')
    return
  }

  try {
    const res = await assignCabinetCell(currentCell.value.cell.id, {
      herbId: assignForm.herbId,
      minStock: assignForm.minStock,
      currentStock: assignForm.currentStock
    })
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
  assignForm.minStock = 100
  assignForm.currentStock = 200
}

// 快速分配 - 加载已分配药材名称集合
const fetchAssignedHerbNames = async () => {
  try {
    const res = await getAssignedHerbIds()
    if (res.code === 200) {
      assignedHerbNames.value = new Set(Array.isArray(res.data) ? res.data : [])
    }
  } catch (error) {
    console.warn('获取已分配药材名称失败', error)
  }
}

// 快速分配 - 切换模式
const onQuickAssignToggle = (val) => {
  if (val) {
    fetchAssignedHerbNames()
    selectedHerbForAssign.value = null
    herbSearchKeyword.value = ''
    herbFilterTab.value = 'unassigned'
  }
}

// 快速分配 - 选择药材
const handleSelectHerbForAssign = (herb) => {
  if (selectedHerbForAssign.value?.id === herb.id) {
    selectedHerbForAssign.value = null
  } else {
    selectedHerbForAssign.value = herb
  }
}

// 批量导入相关
const batchImportDialogVisible = ref(false)
const importFile = ref(null)
const importOverwrite = ref(false)
const importLoading = ref(false)
const importResult = ref(null)
const importUploadRef = ref(null)

const handleDownloadTemplate = async () => {
  if (!selectedCabinetId.value) return
  try {
    const res = await downloadCabinetImportTemplate(selectedCabinetId.value)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${currentCabinet.value?.name || '药柜'}_格位分配模板.xlsx`
    a.click()
    URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('下载模板失败')
  }
}

const handleImportFileChange = (file) => {
  importFile.value = file.raw
}

const handleBatchImportSubmit = async () => {
  if (!importFile.value || !selectedCabinetId.value) return
  importLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', importFile.value)
    formData.append('overwrite', importOverwrite.value)
    const res = await batchImportCabinetAssignment(selectedCabinetId.value, formData)
    if (res.code === 200) {
      importResult.value = res.data
      fetchCabinetCells(selectedCabinetId.value)
      fetchAssignedHerbNames()
    } else {
      ElMessage.error(res.message || '导入失败')
    }
  } catch (error) {
    ElMessage.error('导入请求失败')
  } finally {
    importLoading.value = false
  }
}

const handleImportDialogClose = () => {
  importFile.value = null
  importOverwrite.value = false
  importResult.value = null
  importLoading.value = false
  if (importUploadRef.value) {
    importUploadRef.value.clearFiles()
  }
}

onMounted(() => {
  fetchCabinetList()
  fetchHerbOptions()
})
</script>

<style scoped lang="scss">
.cabinet-manage-container {
  padding: 10px;

  .cabinet-list-section {
    .section-header {
      margin-bottom: 10px;

      h3 {
        margin: 0 0 6px 0;
        font-size: 15px;
        font-weight: 600;
      }
    }

    .cabinet-list {
      max-height: calc(100vh - 180px);
      overflow-y: auto;

      .cabinet-item {
        padding: 12px;
        margin-bottom: 8px;
        border: 1px solid #e4e7ed;
        border-radius: 4px;
        cursor: pointer;
        transition: all 0.3s;
        position: relative;

        &:hover {
          border-color: #409eff;
          background-color: #ecf5ff;
          
          .cabinet-actions {
            opacity: 1;
          }
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
          margin-bottom: 8px;
        }

        .cabinet-actions {
          opacity: 0;
          transition: opacity 0.3s;
          text-align: right;
        }
      }
    }
  }

  .cabinet-grid-section {
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 10px;

      .section-header-left {
        h3 {
          margin: 0 0 4px 0;
          font-size: 16px;
          font-weight: 600;
        }

        .cabinet-desc {
          color: #606266;
          font-size: 13px;
        }
      }
    }

    .grid-container {
      max-height: calc(100vh - 130px);
      overflow: auto;
      background-color: #f5f7fa;
      border-radius: 4px;
      padding: 8px;

      .cabinet-row {
        display: flex;
        align-items: stretch;
        margin-bottom: 4px;

        .row-label {
          width: 42px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 13px;
          color: #606266;
          font-weight: 600;
          flex-shrink: 0;
        }

        .row-drawers {
          display: flex;
          flex: 1;
          gap: 4px;
          overflow-x: auto;

          .drawer-box {
            flex: 1;
            min-width: 60px;
            border: 1px solid #c0c4cc;
            border-radius: 4px;
            background: #fff;
            padding: 2px;
            position: relative;

            .drawer-label {
              position: absolute;
              top: 2px;
              left: 4px;
              font-size: 12px;
              color: #606266;
              font-weight: 700;
            }

            .drawer-cells {
              display: flex;
              flex-wrap: wrap;
              gap: 2px;
              padding-top: 16px;
              min-height: 36px;

              &.cells-1 {
                .sub-cell { width: 100%; }
              }
              &.cells-2 {
                .sub-cell { width: calc(50% - 1px); }
              }
              &.cells-3 {
                .sub-cell { width: calc(50% - 1px); }
                .sub-cell:last-child { width: 100%; }
              }
              &.cells-4 {
                .sub-cell { width: calc(50% - 1px); }
              }

              .sub-cell {
                min-height: 30px;
                padding: 2px 3px;
                border: 1px solid #dcdfe6;
                border-radius: 2px;
                cursor: pointer;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                text-align: center;
                transition: all 0.2s;
                font-size: 12px;

                &:hover {
                  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
                }

                &.empty {
                  background-color: #fafafa;
                  border-color: #dcdfe6;
                }

                &.normal {
                  border-color: #67c23a;
                  background-color: #f0f9eb;
                }

                &.low-stock {
                  border-color: #e6a23c;
                  background-color: #fdf6ec;
                }

                .sub-label {
                  font-size: 12px;
                  font-weight: 700;
                  color: #303133;
                  line-height: 1.2;
                }

                .cell-herb {
                  font-weight: 600;
                  color: #303133;
                  font-size: 12px;
                  line-height: 1.2;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                  max-width: 100%;
                }

                .cell-stock {
                  font-size: 11px;
                  color: #303133;
                  line-height: 1.2;
                }

                .cell-empty {
                  color: #909399;
                  font-size: 11px;
                }
              }
            }
          }
        }
      }
    }
  }
}

// 每行格数配置区域
.row-config-area {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .row-config-item {
    display: flex;
    align-items: center;
    gap: 6px;

    .row-config-label {
      font-size: 13px;
      color: #606266;
      white-space: nowrap;
    }
  }
}

// 快速分配面板
.quick-assign-panel {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  height: calc(100vh - 130px);
  display: flex;
  flex-direction: column;

  .panel-header {
    margin-bottom: 10px;

    h3 {
      margin: 0 0 6px 0;
      font-size: 15px;
      font-weight: 600;
    }

    .assign-stats {
      display: flex;
      gap: 8px;
    }
  }

  .batch-actions {
    display: flex;
    gap: 8px;
    margin-bottom: 8px;
  }

  .default-values {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 8px;
    font-size: 12px;
    color: #606266;

    .default-label {
      white-space: nowrap;
    }

    .default-sep {
      color: #c0c4cc;
    }

    .default-unit {
      white-space: nowrap;
      color: #909399;
      font-size: 11px;
    }
  }

  .herb-list-scroll {
    flex: 1;
    overflow-y: auto;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    margin-bottom: 8px;

    .herb-item {
      padding: 6px 10px;
      cursor: pointer;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid #f0f0f0;
      transition: all 0.2s;

      &:hover {
        background-color: #f5f7fa;
      }

      &.selected {
        background-color: #ecf5ff;
        border-color: #b3d8ff;
      }

      &.assigned {
        .herb-name {
          color: #909399;
        }
      }

      .herb-name {
        font-size: 13px;
        color: #303133;
        font-weight: 500;
      }

      .herb-meta {
        display: flex;
        align-items: center;
        gap: 4px;

        .herb-category {
          font-size: 11px;
          color: #909399;
        }

        .assigned-icon {
          font-size: 14px;
        }
      }
    }
  }

  .assign-tip {
    font-size: 12px;
    color: #909399;
    padding: 6px 8px;
    background: #f5f7fa;
    border-radius: 4px;
    line-height: 1.4;

    strong {
      color: #409eff;
    }
  }
}

// 快速分配模式下空格子视觉提示
.sub-cell.quick-assign-target {
  border: 2px dashed #409eff !important;
  cursor: crosshair !important;
  background-color: #ecf5ff !important;

  &:hover {
    background-color: #d9ecff !important;
    box-shadow: 0 0 6px rgba(64, 158, 255, 0.3) !important;
  }
}

.sub-cell.quick-assign-removable {
  cursor: pointer !important;

  &:hover {
    border-color: #f56c6c !important;
    background-color: #fef0f0 !important;
    box-shadow: 0 0 6px rgba(245, 108, 108, 0.3) !important;
  }
}
</style>
