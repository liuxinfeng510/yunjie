<template>
  <div class="role-manage">
    <el-row :gutter="20">
      <!-- 左侧：角色列表 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>角色列表</span>
              <el-button type="primary" size="small" @click="showAddRole">
                <el-icon><Plus /></el-icon> 新增
              </el-button>
            </div>
          </template>
          <el-table :data="roles" highlight-current-row @current-change="selectRole" style="width: 100%;">
            <el-table-column prop="name" label="角色名称" />
            <el-table-column prop="code" label="角色编码" width="100" />
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button v-if="!row.isSystem" type="danger" link size="small" @click.stop="deleteRole(row)">
                  删除
                </el-button>
                <span v-else style="color: #909399; font-size: 12px;">系统</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：权限配置 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>权限配置 {{ currentRole ? `- ${currentRole.name}` : '' }}</span>
              <el-button v-if="currentRole" type="primary" size="small" @click="savePermissions" :loading="saving">
                保存权限
              </el-button>
            </div>
          </template>
          
          <div v-if="!currentRole" style="text-align: center; padding: 40px; color: #909399;">
            请选择左侧角色进行权限配置
          </div>
          
          <div v-else>
            <el-alert type="info" :closable="false" style="margin-bottom: 16px;">
              勾选角色可访问的功能模块，未勾选的模块将在菜单中隐藏
            </el-alert>
            
            <el-tree
              ref="treeRef"
              :data="menuTree"
              show-checkbox
              node-key="id"
              :default-checked-keys="checkedKeys"
              :props="{ label: 'name', children: 'children' }"
              @check="handleCheck"
            >
              <template #default="{ node, data }">
                <span>
                  <el-icon v-if="data.icon" style="margin-right: 4px;"><component :is="data.icon" /></el-icon>
                  {{ node.label }}
                </span>
              </template>
            </el-tree>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增角色对话框 -->
    <el-dialog v-model="addDialogVisible" title="新增角色" width="400px">
      <el-form ref="roleFormRef" :model="roleForm" :rules="roleRules" label-width="80px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" placeholder="如：店长、药师" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="roleForm.code" placeholder="如：manager、pharmacist" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="roleForm.description" type="textarea" rows="2" placeholder="角色描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRole">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const treeRef = ref()
const roleFormRef = ref()
const saving = ref(false)
const addDialogVisible = ref(false)
const currentRole = ref(null)
const checkedKeys = ref([])

const roles = ref([
  { id: 1, name: '管理员', code: 'admin', isSystem: true },
  { id: 2, name: '店长', code: 'manager', isSystem: true },
  { id: 3, name: '药师', code: 'pharmacist', isSystem: true },
  { id: 4, name: '店员', code: 'clerk', isSystem: true }
])

const roleForm = ref({ name: '', code: '', description: '' })
const roleRules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

const menuTree = ref([
  {
    id: 'drug',
    name: '药品管理',
    icon: 'FirstAidKit',
    children: [
      { id: 'drug:list', name: '药品列表' },
      { id: 'drug:category', name: '药品分类' },
      { id: 'drug:supplier', name: '供应商管理' }
    ]
  },
  {
    id: 'herb',
    name: '中药管理',
    icon: 'Cherry',
    children: [
      { id: 'herb:list', name: '中药饮片' },
      { id: 'herb:cabinet', name: '斗谱管理' },
      { id: 'herb:prescription', name: '中药处方' },
      { id: 'herb:incompatibility', name: '配伍禁忌' }
    ]
  },
  {
    id: 'inventory',
    name: '库存管理',
    icon: 'Box',
    children: [
      { id: 'inventory:query', name: '库存查询' },
      { id: 'inventory:in', name: '入库管理' },
      { id: 'inventory:out', name: '出库管理' },
      { id: 'inventory:check', name: '盘点管理' },
      { id: 'inventory:warning', name: '库存预警' }
    ]
  },
  {
    id: 'sale',
    name: '销售管理',
    icon: 'ShoppingCart',
    children: [
      { id: 'sale:pos', name: '收银台' },
      { id: 'sale:orders', name: '销售记录' },
      { id: 'sale:refund', name: '退货管理' },
      { id: 'sale:promotion', name: '促销管理' }
    ]
  },
  {
    id: 'member',
    name: '会员管理',
    icon: 'User',
    children: [
      { id: 'member:list', name: '会员列表' },
      { id: 'member:level', name: '等级管理' },
      { id: 'member:health', name: '健康画像' }
    ]
  },
  {
    id: 'gsp',
    name: 'GSP合规',
    icon: 'Document',
    children: [
      { id: 'gsp:acceptance', name: '验收记录' },
      { id: 'gsp:maintenance', name: '养护记录' },
      { id: 'gsp:temperature', name: '温湿度监控' },
      { id: 'gsp:training', name: '员工培训' }
    ]
  },
  {
    id: 'system',
    name: '系统设置',
    icon: 'Setting',
    children: [
      { id: 'system:config', name: '系统配置' },
      { id: 'system:store', name: '门店管理' },
      { id: 'system:staff', name: '员工管理' },
      { id: 'system:role', name: '角色权限' }
    ]
  }
])

// 角色默认权限配置
const rolePermissions = {
  admin: ['drug', 'herb', 'inventory', 'sale', 'member', 'gsp', 'system'],
  manager: ['drug', 'herb', 'inventory', 'sale', 'member', 'gsp', 'system:config', 'system:store', 'system:staff'],
  pharmacist: ['drug', 'herb', 'inventory:query', 'sale:pos', 'sale:orders', 'member:list', 'gsp:acceptance', 'gsp:maintenance'],
  clerk: ['sale:pos', 'sale:orders', 'member:list', 'inventory:query']
}

function selectRole(role) {
  currentRole.value = role
  if (role) {
    // 加载角色权限
    const perms = rolePermissions[role.code] || []
    checkedKeys.value = expandPermissions(perms)
  }
}

function expandPermissions(perms) {
  const result = []
  perms.forEach(p => {
    result.push(p)
    // 如果是父级，展开所有子级
    const parent = menuTree.value.find(m => m.id === p)
    if (parent && parent.children) {
      parent.children.forEach(c => result.push(c.id))
    }
  })
  return result
}

function handleCheck(data, { checkedKeys: keys }) {
  checkedKeys.value = keys
}

async function savePermissions() {
  if (!currentRole.value) return
  saving.value = true
  try {
    // 保存权限到后端
    await request.put(`/role/${currentRole.value.id}/permissions`, {
      permissions: checkedKeys.value
    })
    ElMessage.success('权限保存成功')
    rolePermissions[currentRole.value.code] = [...checkedKeys.value]
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

function showAddRole() {
  roleForm.value = { name: '', code: '', description: '' }
  addDialogVisible.value = true
}

async function submitRole() {
  const valid = await roleFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  roles.value.push({
    id: Date.now(),
    name: roleForm.value.name,
    code: roleForm.value.code,
    isSystem: false
  })
  rolePermissions[roleForm.value.code] = []
  ElMessage.success('角色添加成功')
  addDialogVisible.value = false
}

async function deleteRole(row) {
  try {
    await ElMessageBox.confirm(`确定删除角色"${row.name}"吗?`, '提示', { type: 'warning' })
    const idx = roles.value.findIndex(r => r.id === row.id)
    if (idx > -1) roles.value.splice(idx, 1)
    if (currentRole.value?.id === row.id) {
      currentRole.value = null
    }
    ElMessage.success('删除成功')
  } catch (e) {}
}

onMounted(() => {
  // 默认选中第一个角色
  if (roles.value.length > 0) {
    selectRole(roles.value[0])
  }
})
</script>

<style scoped>
.role-manage {
  height: calc(100vh - 180px);
}
</style>
