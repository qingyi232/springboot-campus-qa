<template>
  <div class="activity-container">
    <el-card shadow="hover">
      <div slot="header" class="card-header">
        <span>活动管理</span>
        <el-button v-if="isAdmin" type="primary" icon="el-icon-plus" @click="handleAdd">
          发布活动
        </el-button>
      </div>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="请输入关键词" clearable></el-input>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable>
            <el-option label="讲座" value="LECTURE"></el-option>
            <el-option label="竞赛" value="COMPETITION"></el-option>
            <el-option label="文化活动" value="CULTURAL"></el-option>
            <el-option label="体育活动" value="SPORTS"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="loadData">查询</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 活动列表 -->
      <el-row :gutter="20">
        <el-col :span="8" v-for="item in tableData" :key="item.id">
          <el-card shadow="hover" class="activity-card" @click.native="handleView(item)">
            <div class="activity-image">
              <img :src="item.coverImage || 'https://via.placeholder.com/300x200'" alt="">
              <el-tag class="activity-type" :type="getTypeColor(item.type)">
                {{ getTypeLabel(item.type) }}
              </el-tag>
            </div>
            <div class="activity-info">
              <h3>{{ item.title }}</h3>
              <p class="activity-desc">{{ item.description }}</p>
              <div class="activity-meta">
                <div class="meta-item">
                  <i class="el-icon-location"></i>
                  {{ item.location }}
                </div>
                <div class="meta-item">
                  <i class="el-icon-time"></i>
                  {{ item.startTime }}
                </div>
                <div class="meta-item">
                  <i class="el-icon-user"></i>
                  {{ item.currentParticipants }}/{{ item.maxParticipants }}人
                </div>
              </div>
              <div class="activity-actions">
                <el-button v-if="!isAdmin" type="primary" size="small" @click.stop="handleRegister(item)">
                  立即报名
                </el-button>
                <el-button v-if="isAdmin" type="text" size="small" @click.stop="handleEdit(item)">
                  编辑
                </el-button>
                <el-button v-if="isAdmin" type="text" size="small" @click.stop="handleDelete(item)">
                  删除
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 分页 -->
      <el-pagination
        @current-change="handlePageChange"
        :current-page="currentPage"
        :page-size="pageSize"
        layout="total, prev, pager, next"
        :total="total"
        style="margin-top: 20px; text-align: center"
      ></el-pagination>
    </el-card>

    <!-- 发布/编辑活动对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="70%" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="活动标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入活动标题"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
                <el-option label="讲座" value="LECTURE"></el-option>
                <el-option label="竞赛" value="COMPETITION"></el-option>
                <el-option label="文化活动" value="CULTURAL"></el-option>
                <el-option label="体育活动" value="SPORTS"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="活动描述" prop="description">
          <el-input type="textarea" v-model="form.description" :rows="3" placeholder="请输入活动描述，支持HTML格式"></el-input>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            💡 支持HTML格式，添加链接示例：&lt;a href="https://www.example.com" target="_blank"&gt;点击这里&lt;/a&gt;
          </div>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="活动地点" prop="location">
              <el-input v-model="form.location" placeholder="请输入活动地点"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="主办方" prop="organizer">
              <el-input v-model="form.organizer" placeholder="请输入主办方"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                value-format="yyyy-MM-dd HH:mm:ss"
                style="width: 100%"
              ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择结束时间"
                value-format="yyyy-MM-dd HH:mm:ss"
                style="width: 100%"
              ></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="报名截止时间" prop="registrationDeadline">
              <el-date-picker
                v-model="form.registrationDeadline"
                type="datetime"
                placeholder="选择报名截止时间"
                value-format="yyyy-MM-dd HH:mm:ss"
                style="width: 100%"
              ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大参与人数" prop="maxParticipants">
              <el-input-number v-model="form.maxParticipants" :min="1" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="参与要求" prop="requirements">
          <el-input type="textarea" v-model="form.requirements" :rows="3" placeholder="请输入参与要求"></el-input>
        </el-form-item>

        <el-form-item label="封面图片">
          <el-input v-model="form.coverImage" placeholder="请输入图片URL"></el-input>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="1">已发布</el-radio>
            <el-radio :label="2">已结束</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSave">保 存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Activity',
  data() {
    return {
      loading: false,
      searchForm: {
        keyword: '',
        type: ''
      },
      tableData: [],
      currentPage: 1,
      pageSize: 9,
      total: 0,
      dialogVisible: false,
      dialogTitle: '',
      form: {
        title: '',
        type: '',
        description: '',
        location: '',
        startTime: '',
        endTime: '',
        registrationDeadline: '',
        maxParticipants: 100,
        requirements: '',
        coverImage: '',
        organizer: '',
        status: 1
      },
      rules: {
        title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
        type: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
        description: [{ required: true, message: '请输入活动描述', trigger: 'blur' }],
        location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
        startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
        endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
        registrationDeadline: [{ required: true, message: '请选择报名截止时间', trigger: 'change' }]
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
        const res = await this.$axios.get('/activity/page', {
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
      this.searchForm = { keyword: '', type: '' }
      this.currentPage = 1
      this.loadData()
    },
    handleAdd() {
      this.dialogTitle = '发布活动'
      this.form = {
        title: '',
        type: '',
        description: '',
        location: '',
        startTime: '',
        endTime: '',
        registrationDeadline: '',
        maxParticipants: 100,
        currentParticipants: 0,
        requirements: '',
        coverImage: '',
        organizer: '',
        status: 1
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
    },
    handleEdit(row) {
      this.dialogTitle = '编辑活动'
      this.form = { ...row }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
    },
    handleSave() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          try {
            if (this.form.id) {
              await this.$axios.put(`/activity/${this.form.id}`, this.form)
              this.$message.success('更新成功')
            } else {
              await this.$axios.post('/activity', this.form)
              this.$message.success('发布成功')
            }
            this.dialogVisible = false
            this.loadData()
          } catch (error) {
            console.error('保存失败:', error)
            this.$message.error('保存失败：' + (error.response?.data?.message || '请稍后重试'))
          }
        }
      })
    },
    handleView(row) {
      this.$message.info('查看活动: ' + row.title)
    },
    handleRegister(row) {
      this.$confirm('确定要报名此活动吗?', '提示', {
        type: 'info'
      }).then(async () => {
        try {
          await this.$axios.post('/activity-registration', {
            activityId: row.id,
            userId: this.$store.state.user.id,
            userName: this.$store.state.user.realName,
            studentId: this.$store.state.user.studentId,
            phone: this.$store.state.user.phone
          })
          this.$message.success('报名成功')
          this.loadData()
        } catch (error) {
          console.error('报名失败:', error)
        }
      }).catch(() => {})
    },
    handleDelete(row) {
      this.$confirm('确定要删除该活动吗?', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          await this.$axios.delete(`/activity/${row.id}`)
          this.$message.success('删除成功')
          this.loadData()
        } catch (error) {
          console.error('删除失败:', error)
        }
      }).catch(() => {})
    },
    getTypeLabel(type) {
      const map = {
        LECTURE: '讲座',
        COMPETITION: '竞赛',
        CULTURAL: '文化活动',
        SPORTS: '体育活动'
      }
      return map[type] || type
    },
    getTypeColor(type) {
      const map = {
        LECTURE: 'primary',
        COMPETITION: 'danger',
        CULTURAL: 'warning',
        SPORTS: 'success'
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
.activity-container {
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

.activity-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.activity-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.activity-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 5px;
  margin-bottom: 15px;
}

.activity-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.activity-type {
  position: absolute;
  top: 10px;
  right: 10px;
}

.activity-info h3 {
  font-size: 18px;
  margin-bottom: 10px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-desc {
  color: #606266;
  font-size: 14px;
  margin-bottom: 15px;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.activity-meta {
  margin-bottom: 15px;
}

.meta-item {
  color: #909399;
  font-size: 13px;
  margin-bottom: 5px;
  display: flex;
  align-items: center;
}

.meta-item i {
  margin-right: 5px;
}

.activity-actions {
  display: flex;
  gap: 10px;
}
</style>

