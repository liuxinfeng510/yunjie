<template>
  <div class="gsp-report">
    <el-card>
      <template #header><span>GSP报表中心</span></template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="验收记录报表" name="acceptance">
          <el-form :inline="true" :model="acceptanceQuery" class="search-form">
            <el-form-item label="日期范围">
              <el-date-picker v-model="acceptanceQuery.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadAcceptanceReport">查询</el-button>
              <el-button type="success" @click="exportAcceptanceExcel">导出Excel</el-button>
              <el-button type="warning" @click="exportAcceptancePdf">导出PDF</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="acceptanceData" v-loading="loading" stripe>
            <el-table-column prop="drugName" label="商品名称" min-width="150" />
            <el-table-column prop="batchNo" label="批号" width="120" />
            <el-table-column prop="supplier" label="供应商" width="150" />
            <el-table-column prop="quantity" label="验收数量" width="100" />
            <el-table-column prop="acceptanceDate" label="验收日期" width="110" />
            <el-table-column prop="inspector" label="验收员" width="90" />
            <el-table-column prop="result" label="验收结论" width="100">
              <template #default="{ row }"><el-tag :type="row.result === 'qualified' ? 'success' : 'danger'">{{ row.result === 'qualified' ? '合格' : '不合格' }}</el-tag></template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="养护记录报表" name="maintenance">
          <el-form :inline="true" :model="maintenanceQuery" class="search-form">
            <el-form-item label="日期范围">
              <el-date-picker v-model="maintenanceQuery.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadMaintenanceReport">查询</el-button>
              <el-button type="success" @click="exportMaintenanceExcel">导出Excel</el-button>
              <el-button type="warning" @click="exportMaintenancePdf">导出PDF</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="maintenanceData" v-loading="loading" stripe>
            <el-table-column prop="drugName" label="商品名称" min-width="150" />
            <el-table-column prop="batchNo" label="批号" width="120" />
            <el-table-column prop="maintenanceDate" label="养护日期" width="110" />
            <el-table-column prop="maintainer" label="养护员" width="90" />
            <el-table-column prop="method" label="养护方式" width="100" />
            <el-table-column prop="result" label="养护结果" width="100" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="温湿度记录报表" name="temperature">
          <el-form :inline="true" :model="temperatureQuery" class="search-form">
            <el-form-item label="日期范围">
              <el-date-picker v-model="temperatureQuery.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
            </el-form-item>
            <el-form-item label="存储区域">
              <el-select v-model="temperatureQuery.area" placeholder="请选择" clearable>
                <el-option label="常温区" value="normal" />
                <el-option label="阴凉区" value="cool" />
                <el-option label="冷藏区" value="cold" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadTemperatureReport">查询</el-button>
              <el-button type="success" @click="exportTemperatureExcel">导出Excel</el-button>
              <el-button type="warning" @click="exportTemperaturePdf">导出PDF</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="temperatureData" v-loading="loading" stripe>
            <el-table-column prop="recordTime" label="记录时间" width="170" />
            <el-table-column prop="area" label="存储区域" width="100" />
            <el-table-column prop="temperature" label="温度(℃)" width="100" />
            <el-table-column prop="humidity" label="湿度(%)" width="100" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }"><el-tag :type="row.status === 'normal' ? 'success' : 'danger'">{{ row.status === 'normal' ? '正常' : '异常' }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="recorder" label="记录人" width="90" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="培训记录报表" name="training">
          <el-form :inline="true" :model="trainingQuery" class="search-form">
            <el-form-item label="日期范围">
              <el-date-picker v-model="trainingQuery.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadTrainingReport">查询</el-button>
              <el-button type="success" @click="exportTrainingExcel">导出Excel</el-button>
              <el-button type="warning" @click="exportTrainingPdf">导出PDF</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="trainingData" v-loading="loading" stripe>
            <el-table-column prop="title" label="培训主题" min-width="150" />
            <el-table-column prop="type" label="培训类型" width="100" />
            <el-table-column prop="trainer" label="培训讲师" width="100" />
            <el-table-column prop="trainingDate" label="培训日期" width="110" />
            <el-table-column prop="duration" label="时长(h)" width="80" />
            <el-table-column prop="participantCount" label="参训人数" width="100" />
            <el-table-column prop="passRate" label="通过率" width="80" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { exportAcceptanceReport, exportMaintenanceReport, exportTemperatureReport, exportTrainingReport } from '@/api/gsp'

const loading = ref(false)
const activeTab = ref('acceptance')
const acceptanceData = ref([])
const maintenanceData = ref([])
const temperatureData = ref([])
const trainingData = ref([])

const acceptanceQuery = reactive({ dateRange: [] })
const maintenanceQuery = reactive({ dateRange: [] })
const temperatureQuery = reactive({ dateRange: [], area: '' })
const trainingQuery = reactive({ dateRange: [] })

const loadAcceptanceReport = async () => { loading.value = true; try { const res = await exportAcceptanceReport({ ...acceptanceQuery, format: 'json' }); acceptanceData.value = res.data } catch (e) { ElMessage.error('加载失败') } finally { loading.value = false } }
const loadMaintenanceReport = async () => { loading.value = true; try { const res = await exportMaintenanceReport({ ...maintenanceQuery, format: 'json' }); maintenanceData.value = res.data } catch (e) { ElMessage.error('加载失败') } finally { loading.value = false } }
const loadTemperatureReport = async () => { loading.value = true; try { const res = await exportTemperatureReport({ ...temperatureQuery, format: 'json' }); temperatureData.value = res.data } catch (e) { ElMessage.error('加载失败') } finally { loading.value = false } }
const loadTrainingReport = async () => { loading.value = true; try { const res = await exportTrainingReport({ ...trainingQuery, format: 'json' }); trainingData.value = res.data } catch (e) { ElMessage.error('加载失败') } finally { loading.value = false } }

const exportAcceptanceExcel = () => ElMessage.info('正在导出Excel...')
const exportAcceptancePdf = () => ElMessage.info('正在导出PDF...')
const exportMaintenanceExcel = () => ElMessage.info('正在导出Excel...')
const exportMaintenancePdf = () => ElMessage.info('正在导出PDF...')
const exportTemperatureExcel = () => ElMessage.info('正在导出Excel...')
const exportTemperaturePdf = () => ElMessage.info('正在导出PDF...')
const exportTrainingExcel = () => ElMessage.info('正在导出Excel...')
const exportTrainingPdf = () => ElMessage.info('正在导出PDF...')
</script>

<style scoped>
.gsp-report { padding: 20px; }
.search-form { margin-bottom: 16px; }
</style>
