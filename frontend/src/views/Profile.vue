<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <!-- 左侧个人信息卡片 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="profile-header">
            <div class="avatar-container">
              <el-avatar :size="100" :src="getAvatarUrl(userInfo.avatar)" icon="el-icon-user-solid"></el-avatar>
              <el-upload
                class="avatar-uploader"
                :action="uploadUrl"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
              >
                <div class="upload-overlay">
                  <i class="el-icon-camera"></i>
                  <span>更换头像</span>
                </div>
              </el-upload>
            </div>
            <h2>{{ userInfo.realName }}</h2>
            <p class="role-tag">
              <el-tag :type="userInfo.role === 'ADMIN' ? 'danger' : 'primary'">
                {{ userInfo.role === 'ADMIN' ? '管理员' : '学生' }}
              </el-tag>
            </p>
          </div>
          <el-divider></el-divider>
          <div class="profile-info">
            <div class="info-item">
              <span class="label">用户名：</span>
              <span class="value">{{ userInfo.username }}</span>
            </div>
            <div class="info-item">
              <span class="label">学号/工号：</span>
              <span class="value">{{ userInfo.studentId }}</span>
            </div>
            <div class="info-item">
              <span class="label">性别：</span>
              <span class="value">{{ userInfo.gender === 1 ? '男' : '女' }}</span>
            </div>
            <div class="info-item">
              <span class="label">学院/部门：</span>
              <span class="value">{{ userInfo.department }}</span>
            </div>
            <div class="info-item">
              <span class="label">专业/职位：</span>
              <span class="value">{{ userInfo.major }}</span>
            </div>
            <div class="info-item">
              <span class="label">注册时间：</span>
              <span class="value">{{ userInfo.createTime }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧编辑信息 -->
      <el-col :span="16">
        <el-card shadow="hover">
          <div slot="header" class="card-header">
            <span>编辑个人信息</span>
          </div>
          <el-form :model="form" :rules="rules" ref="form" label-width="120px">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入姓名"></el-input>
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="学院/部门">
              <el-input v-model="form.department" placeholder="请输入学院或部门"></el-input>
            </el-form-item>
            <el-form-item label="专业/职位">
              <el-input v-model="form.major" placeholder="请输入专业或职位"></el-input>
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号"></el-input>
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave">保存修改</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 修改密码 -->
        <el-card shadow="hover" style="margin-top: 20px">
          <div slot="header" class="card-header">
            <span>修改密码</span>
          </div>
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="120px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码"></el-input>
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码"></el-input>
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
              <el-button @click="handleResetPassword">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'Profile',
  data() {
    const validatePassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入新密码'))
      } else if (value.length < 6) {
        callback(new Error('密码长度不能小于6位'))
      } else {
        if (this.passwordForm.confirmPassword !== '') {
          this.$refs.passwordForm.validateField('confirmPassword')
        }
        callback()
      }
    }
    const validateConfirmPassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入新密码'))
      } else if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }

    return {
      userInfo: {},
      uploadUrl: '/api/user/avatar',
      uploadHeaders: {
        'Authorization': 'Bearer ' + localStorage.getItem('token')
      },
      form: {
        realName: '',
        gender: 1,
        department: '',
        major: '',
        phone: '',
        email: ''
      },
      rules: {
        realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ]
      },
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassword: [{ validator: validatePassword, trigger: 'blur' }],
        confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
      }
    }
  },
  computed: {
    ...mapState(['user'])
  },
  methods: {
    async loadUserInfo() {
      try {
        const res = await this.$axios.get(`/user/${this.user.id}`)
        this.userInfo = res.data
        this.form = {
          realName: res.data.realName,
          gender: res.data.gender,
          department: res.data.department,
          major: res.data.major,
          phone: res.data.phone,
          email: res.data.email
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    },
    handleSave() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          try {
            await this.$axios.put(`/user/${this.user.id}`, this.form)
            this.$message.success('保存成功')
            // 更新本地用户信息
            this.$store.commit('SET_USER', { ...this.user, ...this.form })
            this.loadUserInfo()
          } catch (error) {
            console.error('保存失败:', error)
            this.$message.error('保存失败')
          }
        }
      })
    },
    handleReset() {
      this.loadUserInfo()
    },
    handleChangePassword() {
      this.$refs.passwordForm.validate(async valid => {
        if (valid) {
          try {
            await this.$axios.put(`/user/${this.user.id}/password`, {
              oldPassword: this.passwordForm.oldPassword,
              newPassword: this.passwordForm.newPassword
            })
            this.$message.success('密码修改成功，请重新登录')
            this.passwordForm = {
              oldPassword: '',
              newPassword: '',
              confirmPassword: ''
            }
            // 退出登录
            setTimeout(() => {
              this.$store.dispatch('logout')
              this.$router.push('/login')
            }, 1500)
          } catch (error) {
            console.error('修改密码失败:', error)
            this.$message.error(error.response?.data?.message || '修改密码失败')
          }
        }
      })
    },
    handleResetPassword() {
      this.passwordForm = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      this.$refs.passwordForm.clearValidate()
    },
    handleAvatarSuccess(res) {
      if (res.code === 200) {
        const avatarUrl = res.data
        this.$message.success('头像上传成功')
        // 后端已自动更新数据库，只需更新本地显示
        this.userInfo.avatar = avatarUrl
        // 更新store和localStorage
        const updatedUser = { ...this.user, avatar: avatarUrl }
        this.$store.commit('SET_USER', updatedUser)
        localStorage.setItem('user', JSON.stringify(updatedUser))
        // 强制刷新头像显示
        this.$forceUpdate()
      } else {
        this.$message.error(res.message || '上传失败')
      }
    },
    beforeAvatarUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isImage) {
        this.$message.error('只能上传图片文件!')
        return false
      }
      if (!isLt2M) {
        this.$message.error('图片大小不能超过 2MB!')
        return false
      }
      return true
    },
    getAvatarUrl(avatar) {
      // 如果没有头像,返回null让el-avatar显示默认图标
      if (!avatar) return null
      // 如果已经是完整URL,直接返回
      if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
        return avatar
      }
      // 否则拼接API基础URL
      return `${this.$axios.defaults.baseURL}${avatar}`
    }
  },
  mounted() {
    this.loadUserInfo()
  }
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-header {
  text-align: center;
  padding: 20px 0;
}

.avatar-container {
  position: relative;
  display: inline-block;
  cursor: pointer;
}

.avatar-uploader {
  position: absolute;
  top: 0;
  left: 0;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
}

.upload-overlay {
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  opacity: 0;
  transition: opacity 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
}

.avatar-container:hover .upload-overlay {
  opacity: 1;
}

.upload-overlay i {
  font-size: 24px;
  margin-bottom: 5px;
}

.profile-header h2 {
  margin: 15px 0 10px;
  color: #303133;
}

.role-tag {
  margin: 5px 0;
}

.profile-info {
  padding: 10px 0;
}

.info-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #EBEEF5;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  width: 120px;
  color: #909399;
  font-size: 14px;
}

.info-item .value {
  flex: 1;
  color: #606266;
  font-size: 14px;
}

.card-header {
  font-weight: 500;
  font-size: 16px;
}
</style>

