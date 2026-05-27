<template>
  <div class="content-container">
    <el-card shadow="hover">
      <div slot="header" class="card-header">
        <span>内容管理</span>
        <el-button v-if="isAdmin" type="primary" icon="el-icon-plus" @click="handleAdd">
          新增内容
        </el-button>
      </div>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="请输入关键词" clearable></el-input>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类" clearable>
            <el-option label="新生报到指引" value="FRESHMAN_GUIDE"></el-option>
            <el-option label="在线学习资源" value="LEARNING_RESOURCE"></el-option>
            <el-option label="校园生活百科" value="CAMPUS_LIFE"></el-option>
            <el-option label="校规校纪" value="SCHOOL_RULES"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="loadData">查询</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="200"></el-table-column>
        <el-table-column prop="category" label="分类" width="150">
          <template slot-scope="scope">
            <el-tag :type="getCategoryType(scope.row.category)">
              {{ getCategoryLabel(scope.row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="summary" label="摘要" min-width="250" show-overflow-tooltip></el-table-column>
        <el-table-column prop="viewCount" label="浏览次数" width="100"></el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button v-if="isAdmin" type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button v-if="isAdmin" type="text" size="small" style="color: #F56C6C" @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="60%" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题"></el-input>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="新生报到指引" value="FRESHMAN_GUIDE"></el-option>
            <el-option label="在线学习资源" value="LEARNING_RESOURCE"></el-option>
            <el-option label="校园生活百科" value="CAMPUS_LIFE"></el-option>
            <el-option label="校规校纪" value="SCHOOL_RULES"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="摘要" prop="summary">
          <el-input type="textarea" v-model="form.summary" :rows="3" placeholder="请输入摘要"></el-input>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input type="textarea" v-model="form.content" :rows="8" placeholder="请输入内容，支持HTML格式"></el-input>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            💡 支持HTML格式，添加链接示例：&lt;a href="https://www.example.com" target="_blank"&gt;点击这里&lt;/a&gt;
          </div>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="1">发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSave">保 存</el-button>
      </div>
    </el-dialog>

    <!-- 查看对话框 -->
    <el-dialog title="内容详情" :visible.sync="viewDialogVisible" width="60%">
      <div class="content-detail">
        <h2>{{ viewData.title }}</h2>
        <div class="meta-info">
          <el-tag :type="getCategoryType(viewData.category)">{{ getCategoryLabel(viewData.category) }}</el-tag>
          <span class="meta-item">浏览次数: {{ viewData.viewCount }}</span>
          <span class="meta-item">发布时间: {{ viewData.createTime }}</span>
        </div>
        <div class="summary">{{ viewData.summary }}</div>
        <div class="content">{{ viewData.content }}</div>
        <div v-if="viewData.tags" class="tags">
          <el-tag v-for="tag in viewData.tags.split(',')" :key="tag" size="small" style="margin-right: 5px">
            {{ tag }}
          </el-tag>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Content',
  data() {
    return {
      loading: false,
      searchForm: {
        keyword: '',
        category: ''
      },
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      dialogTitle: '',
      viewDialogVisible: false,
      viewData: {},
      form: {
        title: '',
        category: '',
        summary: '',
        content: '',
        tags: '',
        status: 1
      },
      rules: {
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        category: [{ required: true, message: '请选择分类', trigger: 'change' }],
        summary: [{ required: true, message: '请输入摘要', trigger: 'blur' }],
        content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
      }
    }
  },
  computed: {
    ...mapGetters(['isAdmin'])
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await this.$axios.get('/content/page', {
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
      this.searchForm = { keyword: '', category: '' }
      this.currentPage = 1
      this.loadData()
    },
    handleAdd() {
      this.dialogTitle = '新增内容'
      this.form = {
        title: '',
        category: '',
        summary: '',
        content: '',
        tags: '',
        status: 1
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑内容'
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleView(row) {
      this.viewData = row
      this.viewDialogVisible = true
      // 增加浏览次数
      this.$axios.get(`/content/${row.id}`)
    },
    async handleSave() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          try {
            if (this.form.id) {
              await this.$axios.put(`/content/${this.form.id}`, this.form)
              this.$message.success('更新成功')
            } else {
              await this.$axios.post('/content', this.form)
              this.$message.success('添加成功')
            }
            this.dialogVisible = false
            this.loadData()
          } catch (error) {
            console.error('保存失败:', error)
          }
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确定要删除该内容吗?', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await this.$axios.delete(`/content/${row.id}`)
          this.$message.success('删除成功')
          this.loadData()
        } catch (error) {
          console.error('删除失败:', error)
        }
      }).catch(() => {})
    },
    getCategoryLabel(category) {
      const map = {
        FRESHMAN_GUIDE: '新生报到指引',
        LEARNING_RESOURCE: '在线学习资源',
        CAMPUS_LIFE: '校园生活百科',
        SCHOOL_RULES: '校规校纪'
      }
      return map[category] || category
    },
    getCategoryType(category) {
      const map = {
        FRESHMAN_GUIDE: 'success',
        LEARNING_RESOURCE: 'primary',
        CAMPUS_LIFE: 'warning',
        SCHOOL_RULES: 'danger'
      }
      return map[category] || 'info'
    }
  },
  mounted() {
    this.loadData()
  }
}
</script>

<style scoped>
.content-container {
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

.content-detail {
  padding: 20px;
}

.content-detail h2 {
  margin-bottom: 15px;
  color: #303133;
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #EBEEF5;
}

.meta-item {
  color: #909399;
  font-size: 14px;
}

.summary {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 5px;
  margin-bottom: 20px;
  color: #606266;
  line-height: 1.8;
}

.content {
  margin-bottom: 20px;
  color: #303133;
  line-height: 2;
  white-space: pre-wrap;
}

.tags {
  padding-top: 15px;
  border-top: 1px solid #EBEEF5;
}
</style>

