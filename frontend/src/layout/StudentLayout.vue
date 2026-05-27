<template>
  <div class="student-layout">
    <!-- 顶部导航栏 -->
    <header class="top-navbar">
      <div class="navbar-container">
        <!-- Logo和标题 -->
        <div class="navbar-brand">
          <i class="el-icon-school"></i>
          <span class="brand-name">校园智能问答系统</span>
        </div>

        <!-- 导航菜单 -->
        <nav class="navbar-menu">
          <router-link to="/student/home" class="nav-item">
            <i class="el-icon-s-home"></i>
            <span>首页</span>
          </router-link>
          <router-link to="/student/qa" class="nav-item">
            <i class="el-icon-chat-dot-round"></i>
            <span>智能问答</span>
          </router-link>
          <router-link to="/student/news" class="nav-item">
            <i class="el-icon-news"></i>
            <span>校园资讯</span>
          </router-link>
          <router-link to="/student/activity" class="nav-item">
            <i class="el-icon-trophy"></i>
            <span>活动中心</span>
          </router-link>
          <router-link to="/student/my-registrations" class="nav-item">
            <i class="el-icon-tickets"></i>
            <span>我的报名</span>
          </router-link>
          <router-link to="/student/customer-service" class="nav-item">
            <i class="el-icon-service"></i>
            <span>人工客服</span>
          </router-link>
          <router-link to="/student/feedback" class="nav-item">
            <i class="el-icon-edit-outline"></i>
            <span>问题反馈</span>
          </router-link>
          <router-link to="/student/content" class="nav-item">
            <i class="el-icon-reading"></i>
            <span>内容中心</span>
          </router-link>
        </nav>

        <!-- 右侧用户信息 -->
        <div class="navbar-right">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
            <i class="el-icon-bell" @click="goToNotification"></i>
          </el-badge>
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-avatar">
              <i class="el-icon-user-solid"></i>
              <span>{{ currentUser.realName || currentUser.username }}</span>
              <i class="el-icon-arrow-down"></i>
            </div>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="profile">
                <i class="el-icon-user"></i> 个人信息
              </el-dropdown-item>
              <el-dropdown-item command="notification">
                <i class="el-icon-bell"></i> 消息通知
                <el-badge v-if="unreadCount > 0" :value="unreadCount" style="margin-left: 10px"></el-badge>
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <i class="el-icon-switch-button"></i> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="site-footer">
      <div class="footer-container">
        <div class="footer-info">
          <p>© 2025 校园智能问答系统 - 让校园生活更智能</p>
          <p>技术支持：SpringBoot + Vue.js + Rasa</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script>
import { mapState, mapGetters } from 'vuex'

export default {
  name: 'StudentLayout',
  data() {
    return {
      unreadCount: 0
    }
  },
  computed: {
    ...mapGetters(['currentUser'])
  },
  mounted() {
    this.loadUnreadCount()
    // 每30秒刷新一次未读消息数
    this.timer = setInterval(() => {
      this.loadUnreadCount()
    }, 30000)
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
    }
  },
  methods: {
    async loadUnreadCount() {
      try {
        const res = await this.$axios.get(`/notification/unread/${this.currentUser.id}`)
        if (res.data) {
          this.unreadCount = res.data.length
        }
      } catch (error) {
        console.error('获取未读消息失败:', error)
      }
    },
    goToNotification() {
      if (this.$route.path !== '/student/notification') {
        this.$router.push('/student/notification').catch(() => {})
      }
    },
    handleCommand(command) {
      if (command === 'logout') {
        this.$confirm('确定要退出登录吗?', '提示', {
          type: 'warning'
        }).then(() => {
          this.$store.dispatch('logout')
          this.$router.push('/login')
          this.$message.success('退出成功')
        }).catch(() => {})
      } else if (command === 'profile') {
        if (this.$route.path !== '/student/profile') {
          this.$router.push('/student/profile').catch(() => {})
        }
      } else if (command === 'notification') {
        if (this.$route.path !== '/student/notification') {
          this.$router.push('/student/notification').catch(() => {})
        }
      }
    }
  }
}
</script>

<style scoped>
.student-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
}

/* 顶部导航栏 - 玻璃拟态科技感 */
.top-navbar {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 0;
  z-index: 1000;
  color: #202124;
}

.navbar-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.navbar-brand {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: 600;
  cursor: pointer;
  color: #202124;
  letter-spacing: -0.3px;
}

.navbar-brand i {
  font-size: 28px;
  margin-right: 10px;
  background: linear-gradient(135deg, #4285f4, #1967d2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.brand-name {
  color: #202124;
}

.navbar-menu {
  display: flex;
  gap: 8px;
  flex: 1;
  justify-content: center;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 6px;
  color: #5f6368;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.2s, background 0.2s;
  position: relative;
}

.nav-item i {
  font-size: 16px;
}

.nav-item:hover {
  background: #f1f3f4;
  color: #202124;
}

.nav-item.router-link-active {
  background: linear-gradient(135deg, rgba(66, 133, 244, 0.08), rgba(25, 103, 210, 0.08));
  color: #1967d2;
}

.nav-item.router-link-active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(to right, #4285f4, #1967d2);
  border-radius: 2px 2px 0 0;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 25px;
}

.notification-badge {
  cursor: pointer;
}

.notification-badge i {
  font-size: 20px;
  color: #5f6368;
  transition: color 0.2s;
}

.notification-badge i:hover {
  color: #202124;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  background: #f1f3f4;
  transition: background 0.2s;
}

.user-avatar:hover {
  background: #e8eaed;
}

.user-avatar i:first-child {
  font-size: 18px;
  color: #5f6368;
}

.user-avatar span {
  font-size: 14px;
  font-weight: 500;
  color: #202124;
}

.user-avatar i:last-child {
  font-size: 12px;
  color: #5f6368;
}

/* 主内容区 */
.main-content {
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 30px;
  box-sizing: border-box;
}

/* 页脚 */
.site-footer {
  background: #f8f9fa;
  border-top: 1px solid #e8eaed;
  color: #5f6368;
  padding: 24px 0;
  margin-top: 60px;
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
}

.footer-info {
  text-align: center;
}

.footer-info p {
  margin: 6px 0;
  font-size: 13px;
}

.footer-info p:first-child {
  font-size: 14px;
  font-weight: 500;
  color: #202124;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .navbar-container {
    max-width: 100%;
  }
  
  .main-content {
    max-width: 100%;
  }
}

@media (max-width: 768px) {
  .navbar-menu {
    display: none;
  }
  
  .brand-name {
    font-size: 18px;
  }
  
  .main-content {
    padding: 20px 15px;
  }
}
</style>

