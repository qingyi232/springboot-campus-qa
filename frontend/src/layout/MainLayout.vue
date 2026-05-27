<template>
  <el-container style="height: 100vh">
    <!-- 侧边栏 -->
    <el-aside width="200px" style="background-color: #304156">
      <div class="logo">
        <h2>校园问答系统</h2>
      </div>
      <el-menu
        :default-active="$route.path"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/admin/home">
          <i class="el-icon-s-home"></i>
          <span slot="title">首页</span>
        </el-menu-item>
        <el-menu-item index="/admin/qa">
          <i class="el-icon-chat-dot-round"></i>
          <span slot="title">智能问答</span>
        </el-menu-item>
        <el-menu-item index="/admin/content">
          <i class="el-icon-document"></i>
          <span slot="title">内容管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/activity">
          <i class="el-icon-trophy"></i>
          <span slot="title">活动管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/news">
          <i class="el-icon-news"></i>
          <span slot="title">校园资讯</span>
        </el-menu-item>
        <el-menu-item index="/admin/customer-service">
          <i class="el-icon-service"></i>
          <span slot="title">人工客服</span>
        </el-menu-item>
        <el-menu-item index="/admin/feedback">
          <i class="el-icon-chat-line-round"></i>
          <span slot="title">问题反馈</span>
        </el-menu-item>
        <el-menu-item index="/admin/registration-management">
          <i class="el-icon-s-claim"></i>
          <span>报名管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/registration-statistics">
          <i class="el-icon-data-analysis"></i>
          <span slot="title">报名统计</span>
        </el-menu-item>
        <el-submenu v-if="isAdmin" index="system">
          <template slot="title">
            <i class="el-icon-setting"></i>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/admin/user">
            <i class="el-icon-user"></i>
            <span slot="title">用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/log">
            <i class="el-icon-document-copy"></i>
            <span slot="title">操作日志</span>
          </el-menu-item>
        </el-submenu>
        <el-menu-item index="/admin/notification">
          <i class="el-icon-bell"></i>
          <span slot="title">消息通知</span>
          <el-badge v-if="unreadCount > 0" :value="unreadCount" class="menu-badge"></el-badge>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header style="background-color: #fff; border-bottom: 1px solid #e6e6e6">
        <div class="header-content">
          <div class="header-title">{{ $route.meta.title || '首页' }}</div>
          <div class="header-right">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
              <i class="el-icon-bell" style="font-size: 20px; cursor: pointer" @click="showNotifications"></i>
            </el-badge>
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <i class="el-icon-user-solid"></i>
                {{ currentUser.realName || currentUser.username }}
                <i class="el-icon-arrow-down el-icon--right"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="profile">
                  <i class="el-icon-user"></i> 个人信息
                </el-dropdown-item>
                <el-dropdown-item command="notification">
                  <i class="el-icon-bell"></i> 消息通知
                  <el-badge v-if="unreadCount > 0" :value="unreadCount" style="margin-left: 5px"></el-badge>
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <i class="el-icon-switch-button"></i> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <!-- 主体内容 -->
      <el-main>
        <router-view/>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { mapGetters, mapState } from 'vuex'

export default {
  name: 'MainLayout',
  computed: {
    ...mapGetters(['isAdmin', 'currentUser']),
    ...mapState(['unreadCount'])
  },
  methods: {
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
        if (this.$route.path !== '/admin/profile') {
          this.$router.push('/admin/profile').catch(() => {})
        }
      } else if (command === 'notification') {
        if (this.$route.path !== '/admin/notification') {
          this.$router.push('/admin/notification').catch(() => {})
        }
      }
    },
    showNotifications() {
      if (this.$route.path !== '/admin/notification') {
        this.$router.push('/admin/notification').catch(() => {})
      }
    },
    async loadUnreadCount() {
      try {
        const res = await this.$axios.get(`/notification/unread/${this.currentUser.id}`)
        if (res.data) {
          this.$store.commit('SET_UNREAD_COUNT', res.data.length)
        }
      } catch (error) {
        console.error('获取未读消息失败:', error)
      }
    }
  },
  mounted() {
    this.loadUnreadCount()
    // 定时刷新未读消息数（每30秒）
    this.timer = setInterval(() => {
      this.loadUnreadCount()
    }, 30000)
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer)
    }
  }
}
</script>

<style scoped>
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  border-bottom: 1px solid #1f2d3d;
}

.logo h2 {
  font-size: 18px;
  font-weight: 500;
}

.el-menu {
  border-right: none;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.header-title {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.notification-badge {
  margin-right: 10px;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}

.menu-badge {
  position: absolute;
  top: 10px;
  right: 10px;
}
</style>

