<template>
  <div class="category-manage-container">
    <el-card shadow="never">
      <el-row :gutter="16">
        <!-- 左侧分类树 -->
        <el-col :span="10">
          <div class="tree-panel">
            <div class="panel-header">
              <span style="font-weight: 600; font-size: 14px;">分类树</span>
              <el-button type="primary" size="small" @click="handleAddRoot" :icon="Plus">
                添加根分类
              </el-button>
            </div>
            <div class="tree-content" v-loading="treeLoading">
              <el-tree
                ref="treeRef"
                :data="categoryTree"
                :props="{ label: 'name', children: 'children' }"
                node-key="id"
                default-expand-all
                highlight-current
                @node-click="handleNodeClick"
              >
                <template #default="{ node, data }">
                  <div class="custom-tree-node">
                    <span class="node-label">
                      {{ node.label }}
                      <el-tag v-if="data.isSystem" size="small" type="info" style="margin-left: 6px;">系统</el-tag>
                    </span>
                    <span class="node-actions">
                      <el-button
                        link
                        type="primary"
                        size="small"
                        @click.stop="handleAddChild(data)"
                        :icon="Plus"
                      >
                        添加子分类
                      </el-button>
                      <el-button
                        v-if="!data.isSystem"
                        link
                        type="danger"
                        size="small"
                        @click.stop="handleDelete(data)"
                        :icon="Delete"
                      >
                        删除
                      </el-button>
                    </span>
                  </div>
                </template>
              </el-tree>
              <el-empty v-if="!categoryTree.length" description="暂无分类数据" />
            </div>
          </div>
        </el-col>

        <!-- 右侧表单 -->
        <el-col :span="14">
          <div class="form-panel">
            <div class="panel-header">
              <span style="font-weight: 600; font-size: 14px;">{{ formTitle }}</span>
            </div>
            <el-form
              :model="formData"
              :rules="formRules"
              ref="formRef"
              label-width="100px"
              style="margin-top: 20px; max-width: 500px;"
            >
              <el-form-item label="上级分类">
                <el-tree-select
                  v-model="formData.parentId"
                  :data="categoryTree"
                  placeholder="请选择上级分类(留空为根分类)"
                  clearable
                  :disabled="isSystemCategory"
                  style="width: 100%;"
                  :props="{ label: 'name', value: 'id' }"
                  check-strictly
                />
              </el-form-item>

              <el-form-item label="分类名称" prop="name">
                <el-input
                  v-model="formData.name"
                  placeholder="请输入分类名称"
                  maxlength="50"
                  show-word-limit
                  :disabled="isSystemCategory"
                />
              </el-form-item>

              <el-form-item label="排序" prop="sortOrder">
                <el-input-number
                  v-model="formData.sortOrder"
                  :min="0"
                  :max="9999"
                  controls-position="right"
                  style="width: 100%;"
                  :disabled="isSystemCategory"
                />
              </el-form-item>

              <el-form-item label="备注">
                <el-input
                  v-model="formData.remark"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入备注信息"
                  maxlength="200"
                  show-word-limit
                  :disabled="isSystemCategory"
                />
              </el-form-item>

              <el-form-item v-if="!isSystemCategory">
                <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
                  {{ formData.id ? '更新' : '保存' }}
                </el-button>
                <el-button @click="handleReset">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import {
  getCategoryTree,
  createCategory,
  updateCategory,
  deleteCategory
} from '@/api/drug'

// 分类树
const treeRef = ref(null)
const categoryTree = ref([])
const treeLoading = ref(false)

// 表单
const formRef = ref(null)
const formTitle = ref('新增分类')
const submitLoading = ref(false)

const formData = reactive({
  id: null,
  parentId: null,
  name: '',
  sortOrder: 0,
  remark: '',
  isSystem: false
})

// 当前选中的是否为系统分类（只读模式）
const isSystemCategory = computed(() => formData.isSystem === true)

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2到50个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ]
}

// 加载分类树
const loadCategoryTree = async () => {
  treeLoading.value = true
  try {
    const res = await getCategoryTree()
    if (res.code === 200) {
      categoryTree.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载分类树失败')
  } finally {
    treeLoading.value = false
  }
}

// 树节点点击
const handleNodeClick = (data) => {
  formData.id = data.id
  formData.parentId = data.parentId || null
  formData.name = data.name
  formData.sortOrder = data.sortOrder || 0
  formData.remark = data.remark || ''
  formData.isSystem = data.isSystem || false
  formTitle.value = data.isSystem ? '查看系统分类' : '编辑分类'
}

// 添加根分类
const handleAddRoot = () => {
  resetFormData()
  formTitle.value = '新增根分类'
  formData.parentId = null
}

// 添加子分类
const handleAddChild = (data) => {
  resetFormData()
  formTitle.value = `添加【${data.name}】的子分类`
  formData.parentId = data.id
}

// 删除分类
const handleDelete = (data) => {
  if (data.isSystem) {
    ElMessage.warning('系统预置分类不允许删除')
    return
  }
  // 检查是否有子分类
  if (data.children && data.children.length > 0) {
    ElMessage.warning('该分类下有子分类,无法删除')
    return
  }

  ElMessageBox.confirm('确定要删除该分类吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteCategory(data.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadCategoryTree()
        // 如果删除的是当前编辑的分类,重置表单
        if (formData.id === data.id) {
          resetFormData()
          formTitle.value = '新增分类'
        }
      }
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const submitData = {
          name: formData.name,
          parentId: formData.parentId || null,
          sortOrder: formData.sortOrder,
          remark: formData.remark
        }

        const res = formData.id
          ? await updateCategory(formData.id, submitData)
          : await createCategory(submitData)

        if (res.code === 200) {
          ElMessage.success(formData.id ? '更新成功' : '新增成功')
          loadCategoryTree()
          if (!formData.id) {
            resetFormData()
          }
        }
      } catch (error) {
        ElMessage.error(error.message || (formData.id ? '更新失败' : '新增失败'))
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置
const handleReset = () => {
  formRef.value?.resetFields()
  resetFormData()
  formTitle.value = '新增分类'
}

// 重置表单数据
const resetFormData = () => {
  formData.id = null
  formData.parentId = null
  formData.name = ''
  formData.sortOrder = 0
  formData.remark = ''
  formData.isSystem = false
}

// 初始化
onMounted(() => {
  loadCategoryTree()
})
</script>

<style scoped>
.category-manage-container {
  padding: 16px;
}

.tree-panel,
.form-panel {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.tree-content {
  padding: 16px;
  min-height: 400px;
  max-height: 600px;
  overflow-y: auto;
}

.custom-tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 8px;
}

.node-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-actions {
  display: none;
  gap: 4px;
}

.custom-tree-node:hover .node-actions {
  display: flex;
}

.form-panel {
  padding: 16px;
}
</style>
