<template>
  <div class="log-container">
    <el-card shadow="hover">
      <div slot="header" class="card-header">
        <span>操作日志</span>
      </div>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="操作模块">
          <el-select v-model="searchForm.module" placeholder="请选择模块" clearable>
            <el-option label="用户管理" value="USER"></el-option>
            <el-option label="内容管理" value="CONTENT"></el-option>
            <el-option label="活动管理" value="ACTIVITY"></el-option>
            <el-option label="资讯管理" value="NEWS"></el-option>
            <el-option label="智能问答" value="QA"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" placeholder="请选择类型" clearable>
            <el-option label="新增" value="CREATE"></el-option>
            <el-option label="修改" value="UPDATE"></el-option>
            <el-option label="删除" value="DELETE"></el-option>
            <el-option label="查询" value="SELECT"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="searchForm.username" placeholder="请输入操作人" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="loadData">查询</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="username" label="操作人" width="120"></el-table-column>
        <el-table-column prop="module" label="操作模块" width="120">
          <template slot-scope="scope">
            <el-tag :type="getModuleType(scope.row.module)">
              {{ getModuleLabel(scope.row.module) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template slot-scope="scope">
            <el-tag :type="getOperationType(scope.row.operationType)" size="small">
              {{ getOperationLabel(scope.row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="操作描述" min-width="200"></el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" width="140"></el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180"></el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleView(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        @current-change="handlePageChange"
        :current-page="currentPage"
        :page-size="pageSize"
        layout="total, prev, pager, next, jumper"
        :total="total"
        style="margin-top: 20px; text-align: center"
      ></el-pagination>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog title="日志详情" :visible.sync="detailVisible" width="50%">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作人">{{ detailData.username }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ getModuleLabel(detailData.module) }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ getOperationLabel(detailData.operationType) }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ detailData.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detailData.duration }}ms</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ detailData.description }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre style="max-height: 200px; overflow-y: auto; background: #f5f7fa; padding: 10px; border-radius: 4px;">{{ detailData.requestParams || '无' }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'OperationLog',
  data() {
    return {
      loading: false,
      searchForm: {
        module: '',
        operationType: '',
        username: ''
      },
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      detailVisible: false,
      detailData: {}
    }
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await this.$axios.get('/log/page', {
          params: {
            current: this.currentPage,
            size: this.pageSize,
            ...this.searchForm
          }
        })
        this.tableData = res.data.records || []
        this.total = res.data.total || 0
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        this.loading = false
      }
    },
    handlePageChange(page) {
      this.currentPage = page
      this.loadData()
    },
    handleReset() {
      this.searchForm = { module: '', operationType: '', username: '' }
      this.currentPage = 1
      this.loadData()
    },
    async handleView(row) {
      try {
        const res = await this.$axios.get(`/log/${row.id}`)
        this.detailData = res.data
        this.detailVisible = true
      } catch (error) {
        console.error('获取详情失败:', error)
      }
    },
    getModuleLabel(module) {
      const map = {
        USER: '用户管理',
        CONTENT: '内容管理',
        ACTIVITY: '活动管理',
        NEWS: '资讯管理',
        QA: '智能问答'
      }
      return map[module] || module
    },
    getModuleType(module) {
      const map = {
        USER: 'danger',
        CONTENT: 'primary',
        ACTIVITY: 'warning',
        NEWS: 'success',
        QA: 'info'
      }
      return map[module] || 'info'
    },
    getOperationLabel(type) {
      const map = {
        CREATE: '新增',
        UPDATE: '修改',
        DELETE: '删除',
        SELECT: '查询'
      }
      return map[type] || type
    },
    getOperationType(type) {
      const map = {
        CREATE: 'success',
        UPDATE: 'primary',
        DELETE: 'danger',
        SELECT: 'info'
      }
      return map[type] || 'info'
    }
  },
  mounted() {
    this.loadData()
  }
}
</script>

<style scoped>
.log-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

pre {
  margin: 0;
  font-size: 12px;
  font-family: 'Courier New', monospace;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>


