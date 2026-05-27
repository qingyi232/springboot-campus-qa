<template>
  <div class="login-container">
    <!-- 动态背景 -->
    <div class="background-animation">
      <div class="circles">
        <div v-for="i in 10" :key="i" :class="`circle circle-${i}`"></div>
      </div>
      <div class="grid-overlay"></div>
    </div>

    <!-- 主登录区域 -->
    <div class="login-wrapper">
      <!-- 左侧信息面板 -->
      <div class="info-panel">
        <div class="logo-section">
          <div class="logo-icon">
            <i class="el-icon-s-promotion"></i>
          </div>
          <h1 class="system-title">校园智能问答系统</h1>
          <p class="system-subtitle">Campus Intelligent QA System</p>
        </div>
        <div class="feature-list">
          <div class="feature-item" v-for="(feature, index) in features" :key="index">
            <i :class="feature.icon"></i>
            <div class="feature-content">
              <h3>{{ feature.title }}</h3>
              <p>{{ feature.desc }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-box">
        <div class="glass-effect"></div>
        <div class="login-content">
          <div class="login-header">
            <h2>欢迎登录</h2>
            <p>Welcome Back</p>
          </div>
          
          <!-- 角色切换 -->
          <div class="role-switch">
            <div 
              :class="['role-item', { active: activeTab === 'student' }]"
              @click="activeTab = 'student'"
            >
              <i class="el-icon-user"></i>
              <span>学生登录</span>
            </div>
            <div 
              :class="['role-item', { active: activeTab === 'admin' }]"
              @click="activeTab = 'admin'"
            >
              <i class="el-icon-s-custom"></i>
              <span>管理员登录</span>
            </div>
          </div>

          <!-- 登录表单 -->
          <el-form 
            :model="loginForm" 
            :rules="rules" 
            :ref="activeTab === 'student' ? 'loginForm' : 'adminForm'" 
            class="login-form"
          >
            <el-form-item prop="username">
              <div class="input-wrapper">
                <i class="el-icon-user input-icon"></i>
                <el-input
                  v-model="loginForm.username"
                  :placeholder="activeTab === 'student' ? '请输入学生用户名' : '请输入管理员用户名'"
                  clearable
                ></el-input>
              </div>
            </el-form-item>
            <el-form-item prop="password">
              <div class="input-wrapper">
                <i class="el-icon-lock input-icon"></i>
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  :placeholder="activeTab === 'student' ? '请输入密码' : '请输入管理员密码'"
                  show-password
                  @keyup.enter.native="handleLogin"
                ></el-input>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                class="login-button"
                @click="handleLogin"
              >
                <span v-if="!loading">立即登录</span>
                <span v-else>登录中...</span>
              </el-button>
            </el-form-item>
          </el-form>

          <div class="login-footer" v-if="activeTab === 'student'">
            <span>还没有账号？</span>
            <a href="javascript:;" @click="showRegister = true">立即注册</a>
          </div>
        </div>
      </div>
    </div>

    <!-- 注册对话框 -->
    <el-dialog title="用户注册" :visible.sync="showRegister" width="450px">
      <el-form :model="registerForm" :rules="registerRules" ref="registerForm" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="registerForm.realName" placeholder="请输入真实姓名"></el-input>
        </el-form-item>
        <el-form-item label="学号" prop="studentId">
          <el-input v-model="registerForm.studentId" placeholder="请输入学号"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showRegister = false">取 消</el-button>
        <el-button type="primary" @click="handleRegister" :loading="registerLoading">注 册</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.registerForm.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }

    return {
      activeTab: 'student',
      loading: false,
      registerLoading: false,
      showRegister: false,
      features: [
        { icon: 'el-icon-chat-dot-round', title: 'AI智能问答', desc: 'Rasa框架驱动的智能对话' },
        { icon: 'el-icon-reading', title: '知识管理', desc: '海量校园信息一键查询' },
        { icon: 'el-icon-trophy', title: '活动中心', desc: '精彩活动随时参与' },
        { icon: 'el-icon-news', title: '资讯中心', desc: '校园动态实时更新' }
      ],
      loginForm: {
        username: '',
        password: ''
      },
      registerForm: {
        username: '',
        password: '',
        confirmPassword: '',
        realName: '',
        studentId: '',
        phone: '',
        email: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ]
      },
      registerRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ],
        realName: [
          { required: true, message: '请输入真实姓名', trigger: 'blur' }
        ],
        studentId: [
          { required: true, message: '请输入学号', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    handleLogin() {
      const formRef = this.activeTab === 'student' ? 'loginForm' : 'adminForm'
      this.$refs[formRef].validate(async valid => {
        if (valid) {
          this.loading = true
          try {
            const res = await this.$axios.post('/auth/login', this.loginForm)
            
            // 确保数据结构正确
            if (res.data && res.data.token && res.data.user) {
              this.$store.dispatch('login', res.data)
              // 从后端同步最新的用户信息
              await this.$store.dispatch('syncUser')
              this.$message.success('登录成功')
              this.$router.push('/')
            } else {
              this.$message.error('登录数据格式错误')
            }
          } catch (error) {
            console.error('登录失败:', error)
            this.$message.error(error.message || '登录失败')
          } finally {
            this.loading = false
          }
        }
      })
    },
    handleRegister() {
      this.$refs.registerForm.validate(async valid => {
        if (valid) {
          this.registerLoading = true
          try {
            await this.$axios.post('/auth/register', {
              ...this.registerForm,
              role: 'STUDENT'
            })
            this.$message.success('注册成功，请登录')
            this.showRegister = false
            this.$refs.registerForm.resetFields()
          } catch (error) {
            console.error('注册失败:', error)
          } finally {
            this.registerLoading = false
          }
        }
      })
    }
  }
}
</script>

<style scoped>
/* ========== 容器和背景 ========== */
.login-container {
  width: 100%;
  height: 100vh;
  overflow: hidden;
  position: relative;
  background: linear-gradient(135deg, #e3f2fd, #f5f7fa, #ffffff);
}

/* 动态背景动画 */
.background-animation {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.circles {
  position: absolute;
  width: 100%;
  height: 100%;
}

.circles .circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(45deg, rgba(66, 153, 225, 0.15), rgba(49, 130, 206, 0.15));
  animation: float 20s infinite ease-in-out;
  filter: blur(60px);
}

.circle-1 { width: 300px; height: 300px; top: 10%; left: 10%; animation-delay: 0s; }
.circle-2 { width: 200px; height: 200px; top: 60%; left: 80%; animation-delay: 2s; }
.circle-3 { width: 250px; height: 250px; top: 80%; left: 20%; animation-delay: 4s; }
.circle-4 { width: 180px; height: 180px; top: 20%; left: 70%; animation-delay: 1s; }
.circle-5 { width: 220px; height: 220px; top: 50%; left: 50%; animation-delay: 3s; }
.circle-6 { width: 160px; height: 160px; top: 30%; left: 40%; animation-delay: 5s; }
.circle-7 { width: 280px; height: 280px; top: 70%; left: 60%; animation-delay: 2.5s; }
.circle-8 { width: 190px; height: 190px; top: 40%; left: 15%; animation-delay: 4.5s; }
.circle-9 { width: 240px; height: 240px; top: 15%; left: 85%; animation-delay: 1.5s; }
.circle-10 { width: 210px; height: 210px; top: 85%; left: 75%; animation-delay: 3.5s; }

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); opacity: 0.5; }
  25% { transform: translate(30px, -30px) scale(1.1); opacity: 0.7; }
  50% { transform: translate(-20px, 20px) scale(0.9); opacity: 0.4; }
  75% { transform: translate(20px, 30px) scale(1.05); opacity: 0.6; }
}

.grid-overlay {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(66, 153, 225, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(66, 153, 225, 0.08) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridMove 20s linear infinite;
}

@keyframes gridMove {
  0% { transform: translate(0, 0); }
  100% { transform: translate(50px, 50px); }
}

/* ========== 主登录区域 ========== */
.login-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 60px;
  padding: 40px;
  z-index: 10;
}

/* ========== 左侧信息面板 ========== */
.info-panel {
  width: 450px;
  color: #2d3748;
  animation: slideInLeft 0.8s ease-out;
}

@keyframes slideInLeft {
  from { opacity: 0; transform: translateX(-50px); }
  to { opacity: 1; transform: translateX(0); }
}

.logo-section {
  margin-bottom: 50px;
}

.logo-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #4299e1, #3182ce);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  margin-bottom: 20px;
  color: #fff;
  box-shadow: 0 10px 30px rgba(66, 153, 225, 0.3);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); box-shadow: 0 10px 30px rgba(66, 153, 225, 0.3); }
  50% { transform: scale(1.05); box-shadow: 0 15px 40px rgba(66, 153, 225, 0.5); }
}

.system-title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 10px;
  background: linear-gradient(90deg, #2b6cb0, #4299e1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.system-subtitle {
  font-size: 16px;
  color: #718096;
  letter-spacing: 2px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 15px;
  border: 1px solid rgba(66, 153, 225, 0.2);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  cursor: pointer;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(66, 153, 225, 0.5);
  transform: translateX(10px);
  box-shadow: 0 5px 20px rgba(66, 153, 225, 0.2);
}

.feature-item i {
  font-size: 32px;
  color: #4299e1;
}

.feature-content h3 {
  font-size: 18px;
  margin-bottom: 5px;
  color: #2d3748;
}

.feature-content p {
  font-size: 14px;
  color: #718096;
}

/* ========== 右侧登录框 ========== */
.login-box {
  width: 450px;
  position: relative;
  animation: slideInRight 0.8s ease-out;
}

@keyframes slideInRight {
  from { opacity: 0; transform: translateX(50px); }
  to { opacity: 1; transform: translateX(0); }
}

.glass-effect {
  position: absolute;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 25px;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(66, 153, 225, 0.2);
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.1),
    inset 0 0 60px rgba(66, 153, 225, 0.05);
}

.login-content {
  position: relative;
  padding: 50px 40px;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h2 {
  font-size: 32px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 10px;
  text-shadow: 0 2px 10px rgba(66, 153, 225, 0.2);
}

.login-header p {
  font-size: 14px;
  color: #718096;
  letter-spacing: 1px;
}

/* ========== 角色切换 ========== */
.role-switch {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
  background: rgba(226, 232, 240, 0.5);
  padding: 8px;
  border-radius: 15px;
  border: 1px solid rgba(203, 213, 225, 0.8);
}

.role-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 12px;
  border-radius: 10px;
  color: #718096;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.role-item i {
  font-size: 18px;
}

.role-item.active {
  background: linear-gradient(135deg, #4299e1, #3182ce);
  color: #fff;
  box-shadow: 0 5px 15px rgba(66, 153, 225, 0.3);
}

.role-item:not(.active):hover {
  background: rgba(226, 232, 240, 0.8);
  color: #2d3748;
}

/* ========== 表单样式 ========== */
.login-form {
  margin-bottom: 20px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(247, 250, 252, 0.8);
  border-radius: 12px;
  border: 1px solid rgba(203, 213, 225, 0.8);
  padding: 0 15px;
  transition: all 0.3s ease;
}

.input-wrapper:hover {
  background: rgba(241, 245, 249, 1);
  border-color: rgba(66, 153, 225, 0.5);
}

.input-wrapper:focus-within {
  background: #fff;
  border-color: #4299e1;
  box-shadow: 0 0 20px rgba(66, 153, 225, 0.2);
}

.input-icon {
  color: #a0aec0;
  font-size: 18px;
  margin-right: 10px;
}

.input-wrapper >>> .el-input__inner {
  background: transparent !important;
  border: none !important;
  color: #2d3748 !important;
  padding-left: 0;
  font-size: 15px;
}

.input-wrapper >>> .el-input__inner::placeholder {
  color: #a0aec0;
}

.input-wrapper >>> .el-input__suffix {
  color: #a0aec0;
}

.input-wrapper >>> .el-input__clear {
  color: #a0aec0;
}

.input-wrapper >>> .el-input__clear:hover {
  color: #4299e1;
}

.login-form >>> .el-form-item {
  margin-bottom: 20px;
}

.login-form >>> .el-form-item__error {
  color: #ff6b9d;
  font-size: 12px;
}

/* ========== 登录按钮 ========== */
.login-button {
  width: 100%;
  height: 50px;
  background: linear-gradient(135deg, #4299e1, #3182ce);
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 10px 25px rgba(66, 153, 225, 0.3);
  position: relative;
  overflow: hidden;
}

.login-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.login-button:hover::before {
  left: 100%;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 35px rgba(66, 153, 225, 0.4);
}

.login-button:active {
  transform: translateY(0);
}

/* ========== 页脚 ========== */
.login-footer {
  text-align: center;
  color: #718096;
  font-size: 14px;
  margin-top: 25px;
}

.login-footer a {
  color: #4299e1;
  text-decoration: none;
  margin-left: 5px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.login-footer a:hover {
  color: #2b6cb0;
  text-shadow: 0 0 10px rgba(66, 153, 225, 0.3);
}

/* ========== 响应式设计 ========== */
@media (max-width: 1200px) {
  .login-wrapper {
    flex-direction: column;
    gap: 30px;
  }
  
  .info-panel {
    width: 90%;
    max-width: 500px;
  }
  
  .login-box {
    width: 90%;
    max-width: 450px;
  }
}
</style>

